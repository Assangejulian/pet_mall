package com.pat.user.service.auth;

import java.security.SecureRandom;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pat.user.domain.dto.LoginDTO;
import com.pat.user.domain.entity.User;
import com.pat.user.mapper.UserMapper;
import com.pat.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service("faceAuthService")
public class FaceAuthService implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${face.provider:mock}")
    private String faceProvider;
    @Value("${face.api-key:}")
    private String faceApiKey;
    @Value("${face.secret-key:}")
    private String faceSecretKey;

    private static final String FACE_GROUP = "pet_store_users";

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
        return findOrCreateUser(imageBase64);
    }

    private User baiduFaceLogin(String imageBase64) {
        String accessToken = getAccessToken();
        if (accessToken == null) {
            throw new RuntimeException("百度AI 认证失败");
        }

        String userId = searchFace(accessToken, imageBase64);
        if (userId != null) {
            User user = userMapper.selectById(userId);
            if (user != null) return user;
        }

        String faceToken = detectFace(accessToken, imageBase64);
        if (faceToken == null) {
            throw new RuntimeException("未检测到人脸");
        }

        User user = new User();
        user.setUsername(generateUsername());
        userService.createUser(user);

        registerFace(accessToken, String.valueOf(user.getId()), imageBase64);
        user.setFaceId(faceToken);
        userMapper.updateById(user);

        log.info("新人脸注册成功，userId: {}, username: {}", user.getId(), user.getUsername());
        return user;
    }

    private String getAccessToken() {
        try {
            String url = "https://aip.baidubce.com/oauth/2.0/token"
                    + "?grant_type=client_credentials"
                    + "&client_id=" + faceApiKey
                    + "&client_secret=" + faceSecretKey;
            JSONObject json = JSONUtil.parseObj(restTemplate.getForObject(url, String.class));
            return json.getStr("access_token");
        } catch (Exception e) {
            log.error("获取百度 token 失败", e);
            return null;
        }
    }

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

    private User findOrCreateUser(String faceToken) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getFaceId, faceToken));
        if (user == null) {
            user = new User();
            user.setUsername(generateUsername());
            user.setFaceId(faceToken);
            userService.createUser(user);
        }
        return user;
    }

    private String generateUsername() {
        StringBuilder sb = new StringBuilder("face_");
        for (int i = 0; i < 8; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
