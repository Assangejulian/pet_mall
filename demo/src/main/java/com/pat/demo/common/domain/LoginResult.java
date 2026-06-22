package com.pat.demo.common.domain;

import com.artsail.admin.model.domain.VO.UserVO;
import lombok.Data;

/**
 * @author 13372
 */
@Data
public class LoginResult {
    private String status;
    private String type;
    private String currentAuthority;
    // 创建一个UserVO来隐藏敏感信息
    private UserVO user;
    
    public static LoginResult success(UserVO user, String role) {
        LoginResult result = new LoginResult();
        result.setStatus("ok");
        result.setType("account");
        result.setCurrentAuthority(role);
        result.setUser(user);
        return result;
    }
}
