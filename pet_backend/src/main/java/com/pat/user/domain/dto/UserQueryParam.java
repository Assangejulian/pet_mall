package com.pat.user.domain.dto;

import lombok.Data;

@Data
public class UserQueryParam {
    private String keyword;
    private Integer status;
}