package com.pat.user.service.auth;

import java.security.SecureRandom;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pat.user.domain.dto.LoginDTO;
import com.pat.user.domain.entity.User;
import com.pat.user.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 人脸识别登录
 * authType = face
 * 使用百度AI人脸库实现注册/搜索登录
 * 首次拍照自动注册，后续拍照自动识别
 * 注册时生成友好的随机用户名
 */
@Slf4j
@Service("faceAuthService")
public class FaceAuthService implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${face.provider:mock}")
    private String faceProvider;
    @Value("${face.api-key:}")
    private String faceApiKey;
    @Value("${face.secret-key:}")
    private String faceSecretKey;

    /** 百度人脸库组名 */
    private static final String FACE_GROUP = "pet_store_users";

    /** 随机用户名生成器（去掉了易混淆的 0/o/1/l/i） */
    private static final String CHARS = "abcdefghjkmnpqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public User authenticate(LoginDTO dto) {
        String imageBase64 = dto.getFaceToken();
        if (imageBase64 == null || imageBase64.isBlank()) {
            throw new RuntimeException("人脸识别失败，未收到图片");
        }

        if ("baidu".equals(faceProvider)) {
            return baiduFaceLogin(imageBase64);
        }
        // mock 模式：直接用 faceToken 作为 userId
        return findOrCreateUser(imageBase64);
    }

    /** 百度AI 人脸登录：搜索人脸库 -> 找到则登录，未找到则注册 */
    private User baiduFaceLogin(String imageBase64) {
        String accessToken = getAccessToken();
        if (accessToken == null) {
            throw new RuntimeException("百度AI 认证失败");
        }

        // 1. 先搜索人脸库
        String userId = searchFace(accessToken, imageBase64);
        if (userId != null) {
            // 找到已有用户
            User user = userMapper.selectById(userId);
            if (user != null) return user;
        }

        // 2. 未找到 -> 检测人脸，注册新人
        String faceToken = detectFace(accessToken, imageBase64);
        if (faceToken == null) {
            throw new RuntimeException("未检测到人脸");
        }

        // 创建用户（生成友好的随机名）
        User user = new User();
        user.setUsername(generateUsername());
        user.setPassword("");
        user.setRole("user");
        user.setStatus(1);
        userMapper.insert(user);

        // 注册人脸到百度库
        registerFace(accessToken, String.valueOf(user.getId()), imageBase64);
        user.setFaceId(faceToken);
        userMapper.updateById(user);

        log.info("新人脸注册成功，userId: {}, username: {}", user.getId(), user.getUsername());
        return user;
    }

    /** 获取百度AI access_token */
    private String getAccessToken() {
        try {
            String url = "https://aip.baidubce.com/oauth/2.0/token" +
                    "?grant_type=client_credentials" +
                    "&client_id=" + faceApiKey +
                    "&client_secret=" + faceSecretKey;
            JSONObject json = JSONUtil.parseObj(restTemplate.getForObject(url, String.class));
            return json.getStr("access_token");
        } catch (Exception e) {
            log.error("获取百度 token 失败", e);
            return null;
        }
    }

    /** 人脸检测，返回 face_token */
    private String detectFace(String accessToken, String imageBase64) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("image", imageBase64);
            body.put("image_type", "BASE64");
            body.put("face_field", "face_token");

            JSONObject json = JSONUtil.parseObj(restTemplate.postForObject(
                    "https://aip.baidubce.com/rest/2.0/face/v3/detect?access_token=" + accessToken,
                    body, String.class));

            if (json.getInt("error_code") != 0) {
                log.error("百度人脸检测失败: {}", json);
                return null;
            }
            return json.getByPath("result.face_list[0].face_token", String.class);
        } catch (Exception e) {
            log.error("百度人脸检测异常", e);
            return null;
        }
    }

    /** 搜索人脸库，返回 userId */
    private String searchFace(String accessToken, String imageBase64) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("image", imageBase64);
            body.put("image_type", "BASE64");
            body.put("group_id_list", FACE_GROUP);

            JSONObject json = JSONUtil.parseObj(restTemplate.postForObject(
                    "https://aip.baidubce.com/rest/2.0/face/v3/search?access_token=" + accessToken,
                    body, String.class));

            if (json.getInt("error_code") != 0) {
                return null;
            }
            // 置信度超过 80 认为匹配成功
            Double score = json.getByPath("result.user_list[0].score", Double.class);
            if (score != null && score >= 80) {
                return json.getByPath("result.user_list[0].user_id", String.class);
            }
            return null;
        } catch (Exception e) {
            log.error("百度人脸搜索异常", e);
            return null;
        }
    }

    /** 注册人脸到百度库 */
    private void registerFace(String accessToken, String userId, String imageBase64) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("image", imageBase64);
            body.put("image_type", "BASE64");
            body.put("group_id", FACE_GROUP);
            body.put("user_id", userId);

            JSONObject json = JSONUtil.parseObj(restTemplate.postForObject(
                    "https://aip.baidubce.com/rest/2.0/face/v3/faceset/user/add?access_token=" + accessToken,
                    body, String.class));

            if (json.getInt("error_code") != 0) {
                log.error("百度人脸注册失败: {}", json);
            } else {
                log.info("百度人脸注册成功，userId: {}", userId);
            }
        } catch (Exception e) {
            log.error("百度人脸注册异常", e);
        }
    }

    /** mock 模式：直接用 faceToken 当 userId */
    private User findOrCreateUser(String faceToken) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getFaceId, faceToken));
        if (user == null) {
            user = new User();
            user.setUsername(generateUsername());
            user.setPassword("");
            user.setRole("user");
            user.setStatus(1);
            user.setFaceId(faceToken);
            userMapper.insert(user);
        }
        return user;
    }

    /** 生成友好的随机用户名：face_ + 8位易读字符 */
    private String generateUsername() {
        StringBuilder sb = new StringBuilder("face_");
        for (int i = 0; i < 8; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}