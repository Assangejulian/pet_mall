package com.pat.demo.admin.model.domain.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userAccount;
    private String userPassword;
    private String checkPassword;


}
