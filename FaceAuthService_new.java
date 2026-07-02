import cn.hutool.crypto.digest.DigestUtil;
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

import java.nio.charset.StandardCharsets;

@Slf4j
@Service("faceAuthService")
public class FaceAuthService implements AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private FaceHelper faceHelper;

    @Value("")
    private String faceProvider;

    @Override
    public User authenticate(LoginDTO dto) {
        String imageBase64 = dto.getFaceToken();
        if (imageBase64 == null || imageBase64.isBlank()) {
            throw new BusinessException(ErrorCode.FACE_IMAGE_EMPTY);
        }
        return "baidu".equals(faceProvider)
                ? baiduFaceLogin(imageBase64)
                : mockFaceLogin(imageBase64);
    }

    /** mock模式：用base64的哈希值作为face_id，同一张照片始终映射到同一个用户 */
    private User mockFaceLogin(String imageBase64) {
        String faceId = DigestUtil.sha256Hex(imageBase64);
        User user = userService.getOne(
                new LambdaQueryWrapper<User>().eq(User::getFaceId, faceId));
        if (user == null) {
            user = new User();
            user.setUsername(faceHelper.generateUsername());
            user.setFaceId(faceId);
            userService.createUser(user);
            log.info("mock人脸注册成功，userId: {}, faceId: {}", user.getId(), faceId);
        }
        userService.checkUserActive(user);
        return user;
    }

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
            log.warn("人脸库userId={}在DB中不存在，重新注册", userId);
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

        log.info("百度人脸注册成功，userId: {}, faceId: {}", user.getId(), faceToken);
        return user;
    }
}
