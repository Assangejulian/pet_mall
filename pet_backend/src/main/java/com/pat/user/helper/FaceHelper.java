package com.pat.user.helper;

import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 人脸识别共享逻辑：百度AI API调用、随机用户名生成
 */
@Slf4j
@Component
public class FaceHelper {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${face.api-key:}")
    private String faceApiKey;

    @Value("${face.secret-key:}")
    private String faceSecretKey;

    private static final String FACE_GROUP = "pet_store_users";
    private static final String CHARS = "abcdefghjkmnpqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    /** 获取百度AI access_token */
    public String getAccessToken() {
        try {
            String url = "https://aip.baidubce.com/oauth/2.0/token"
                    + "?grant_type=client_credentials"
                    + "&client_id=" + faceApiKey
                    + "&client_secret=" + faceSecretKey;
            JSONObject json = JSONUtil.parseObj(restTemplate.getForObject(url, String.class));
            return json.getStr("access_token");
        } catch (Exception e) {
            log.error("获取百度 token 失败, msg={}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FACE_DETECT_FAILED);
        }
    }

    /** 人脸检测，返回 face_token */
    public String detectFace(String accessToken, String imageBase64) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("image", imageBase64);
            body.put("image_type", "BASE64");
            body.put("face_field", "face_token");

            JSONObject json = JSONUtil.parseObj(restTemplate.postForObject(
                    "https://aip.baidubce.com/rest/2.0/face/v3/detect?access_token=" + accessToken,
                    body, String.class));

            if (json.getInt("error_code") != 0) {
                log.warn("百度人脸检测返回: {}", json.toJSONString(0));
                throw new BusinessException(ErrorCode.FACE_DETECT_FAILED);
            }
            return json.getByPath("result.face_list[0].face_token", String.class);
        } catch (Exception e) {
            log.error("百度人脸检测异常, msg={}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FACE_DETECT_FAILED);
        }
    }

    /** 搜索人脸库，返回 userId */
    public String searchFace(String accessToken, String imageBase64) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("image", imageBase64);
            body.put("image_type", "BASE64");
            body.put("group_id_list", FACE_GROUP);

            JSONObject json = JSONUtil.parseObj(restTemplate.postForObject(
                    "https://aip.baidubce.com/rest/2.0/face/v3/search?access_token=" + accessToken,
                    body, String.class));

            if (json.getInt("error_code") != 0 && json.getInt("error_code") != 222207) {
                log.warn("百度人脸搜索返回: {}", json.toJSONString(0));
                throw new BusinessException(ErrorCode.FACE_DETECT_FAILED);
            }
            if (json.getInt("error_code") == 222207) {
                return null;
            }
            Double score = json.getByPath("result.user_list[0].score", Double.class);
            if (score != null && score >= 80) {
                return json.getByPath("result.user_list[0].user_id", String.class);
            }
            return null;
        } catch (Exception e) {
            log.error("百度人脸搜索异常, msg={}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FACE_DETECT_FAILED);
        }
    }

    /** 从百度脸库删除用户人脸 */
    public void deleteFace(String accessToken, String userId) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("group_id", FACE_GROUP);
            body.put("user_id", userId);

            JSONObject json = JSONUtil.parseObj(restTemplate.postForObject(
                    "https://aip.baidubce.com/rest/2.0/face/v3/faceset/user/delete?access_token=" + accessToken,
                    body, String.class));

            log.warn("百度人脸删除返回: {}", json.toJSONString(0));
            if (json.getInt("error_code") != 0) {
                log.warn("百度人脸删除失败(可能数据已擦除): {}", json);
            } else {
                log.info("百度人脸删除成功，userId: {}", userId);
            }
        } catch (Exception e) {
            log.error("百度人脸删除异常", e);
        }
    }

    /** 注册人脸到百度库 */
    public void registerFace(String accessToken, String userId, String imageBase64) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("image", imageBase64);
            body.put("image_type", "BASE64");
            body.put("group_id", FACE_GROUP);
            body.put("user_id", userId);

            JSONObject json = JSONUtil.parseObj(restTemplate.postForObject(
                    "https://aip.baidubce.com/rest/2.0/face/v3/faceset/user/add?access_token=" + accessToken,
                    body, String.class));

            log.warn("百度人脸注册返回: {}", json.toJSONString(0));
            if (json.getInt("error_code") != 0) {
                log.error("百度人脸注册失败: {}", json);
            } else {
                log.info("百度人脸注册成功，userId: {}", userId);
            }
        } catch (Exception e) {
            log.error("百度人脸注册异常", e);
        }
    }

    /** 生成友好的随机用户名：face_ + 8位易读字符 */
    public String generateUsername() {
        StringBuilder sb = new StringBuilder("face_");
        for (int i = 0; i < 8; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}

