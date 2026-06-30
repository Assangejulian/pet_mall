package com.pat.user.service.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.user.domain.dto.LoginDTO;
import com.pat.user.domain.entity.User;
import com.pat.user.service.UserService;
import com.pat.user.helper.FaceHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 人脸识别登录
 * authType = face
 * 首次拍照自动注册，后续拍照自动识别
 */
@Slf4j
@Service("faceAuthService")
public class FaceAuthService implements AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private FaceHelper faceHelper;

    @Value("${face.provider:mock}")
    private String faceProvider;

    @Override
    public User authenticate(LoginDTO dto) {
        String imageBase64 = dto.getFaceToken();
        if (imageBase64 == null || imageBase64.isBlank()) {
            throw new BusinessException(ErrorCode.FACE_IMAGE_EMPTY);
        }
        return "baidu".equals(faceProvider)
                ? baiduFaceLogin(imageBase64)
                : findOrCreateUser(imageBase64);
    }

    /** 百度AI 人脸登录：搜索人脸库 → 找到则登录，未找到则注册 */
    private User baiduFaceLogin(String imageBase64) {
        String accessToken = faceHelper.getAccessToken();
        if (accessToken == null) {
            throw new BusinessException(ErrorCode.FACE_DETECT_FAILED);
        }

        String userId = faceHelper.searchFace(accessToken, imageBase64);
        if (userId != null) {
            User user = userService.getById(userId);
            if (user != null) return user;
        }

        String faceToken = faceHelper.detectFace(accessToken, imageBase64);
        if (faceToken == null) {
            throw new BusinessException(ErrorCode.FACE_DETECT_FAILED);
        }

        User user = new User();
        user.setUsername(faceHelper.generateUsername());
        userService.createUser(user);

        faceHelper.registerFace(accessToken, String.valueOf(user.getId()), imageBase64);
        user.setFaceId(faceToken);
        userService.updateById(user);

        log.info("新人脸注册成功，userId: {}, username: {}", user.getId(), user.getUsername());
        return user;
    }

    /** mock 模式：直接用 faceToken 当 userId */
    private User findOrCreateUser(String faceToken) {
        User user = userService.getOne(
                new LambdaQueryWrapper<User>().eq(User::getFaceId, faceToken));
        if (user == null) {
            user = new User();
            user.setUsername(faceHelper.generateUsername());
            user.setFaceId(faceToken);
            userService.createUser(user);
        }
        return user;
    }
}
