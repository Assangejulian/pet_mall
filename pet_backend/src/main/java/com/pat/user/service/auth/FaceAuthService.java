package com.pat.user.service.auth;

import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.user.domain.dto.LoginDTO;
import com.pat.user.domain.entity.User;
import com.pat.user.service.UserService;
import com.pat.user.helper.FaceHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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


    @Override
    public User authenticate(LoginDTO dto) {
        String imageBase64 = dto.getFaceToken();
        if (imageBase64 == null || imageBase64.isBlank()) {
            throw new BusinessException(ErrorCode.FACE_IMAGE_EMPTY);
        }
        return baiduFaceLogin(imageBase64);
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
            if (user != null) {
                userService.checkUserActive(user);
                return user;
            }
            log.warn("人脸库userId={}在DB不存在，从百度脸库删除", userId);
            faceHelper.deleteFace(accessToken, userId);
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


}
