package com.pat.user.domain.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String phone;
    private String avatar;
    private String email;
    private Integer memberLevel;
    private String realName;
    private LocalDate birthday;
    private String role;
    private Integer status;
    private LocalDateTime createTime;
}