package com.pat.user.service.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pat.user.domain.dto.LoginDTO;
import com.pat.user.domain.entity.User;
import com.pat.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 人脸识别登录
 * authType = face
 *
 * <p>当前为结构化 Mock 实现，对接真实人脸 API 后只需替换 {@link #verifyFaceToken(String)} 方法。
 * 预期集成方案：
 * <ol>
 *   <li>前端调用人脸 SDK（如 ArcSoft / 百度AI / 微信生物认证）获取 faceToken</li>
 *   <li>后端在此处调用对应 API 校验 faceToken，返回 userId</li>
 *   <li>根据 userId 查找或创建用户</li>
 * </ol>
 */
@Service("faceAuthService")
public class FaceAuthService implements AuthService {

    @Autowired
    private UserMapper userMapper;

    /** 人脸识别供应商（预留配置） */
    private static final String FACE_PROVIDER = "mock";

    @Override
    public User authenticate(LoginDTO dto) {
        String faceToken = dto.getFaceToken();
        if (faceToken == null || faceToken.isBlank()) {
            throw new RuntimeException("人脸识别失败，未收到 faceToken");
        }

        // 1. 校验 faceToken → 获取用户标识
        String userId = verifyFaceToken(faceToken);
        if (userId == null || userId.isBlank()) {
            throw new RuntimeException("人脸识别验证失败");
        }

        // 2. 查找已有用户（优先按 faceId，兼容旧数据）
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getFaceId, userId)
                        .or()
                        .eq(User::getUsername, "face_" + userId)
        );

        // 3. 不存在则自动注册
        if (user == null) {
            user = new User();
            user.setUsername("face_" + userId);
            user.setFaceId(userId);
            user.setPassword("");
            user.setRole("user");
            user.setStatus(1);
            userMapper.insert(user);
        }

        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已禁用");
        }

        return user;
    }

    /**
     * 调用人脸识别 API 校验 faceToken，返回用户唯一标识。
     *
     * <p>Mock 实现：直接以 faceToken 作为 userId。
     * 集成真实 API 时请替换此方法实现。
     *
     * @param faceToken 前端传递的人脸凭证
     * @return 用户唯一标识（如用户ID / openId）
     */
    private String verifyFaceToken(String faceToken) {
        switch (FACE_PROVIDER) {
            case "arcsoft":
                // TODO: ArcSoft 人脸识别 API 调用
                throw new UnsupportedOperationException("ArcSoft 人脸识别待集成");
            case "baidu":
                // TODO: 百度AI 人脸识别 API 调用
                throw new UnsupportedOperationException("百度AI 人脸识别待集成");
            case "mock":
            default:
                // Mock：直接信任 faceToken
                return faceToken;
        }
    }
}
