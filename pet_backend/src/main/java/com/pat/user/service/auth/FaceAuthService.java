package com.pat.user.service.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pat.user.domain.dto.LoginDTO;
import com.pat.user.domain.entity.User;
import com.pat.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 人脸识别登录（存根，对接人脸 API 后补全）
 * authType = face
 */
@Service("faceAuthService")
public class FaceAuthService implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User authenticate(LoginDTO dto) {
        String faceToken = dto.getFaceToken();
        if (faceToken == null || faceToken.isBlank()) {
            throw new RuntimeException("人脸识别失败，请重试");
        }

        // TODO: 调人脸识别 API 校验 faceToken，返回 userId
        String mockUserId = "face_mock_" + faceToken;

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, mockUserId));
        if (user == null) {
            user = new User();
            user.setUsername(mockUserId);
            user.setPassword("");
            user.setRole("user");
            user.setStatus(1);
            userMapper.insert(user);
        }
        return user;
    }
}
