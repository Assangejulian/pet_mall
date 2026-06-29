package com.pat.user.utils;

import cn.hutool.core.util.StrUtil;

public class RegexUtils {

    public static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    public static boolean isEmailInvalid(String email) {
        return mismatch(email, EMAIL_REGEX);
    }

    private static boolean mismatch(String str, String regex) {
        if (StrUtil.isBlank(str)) {
            return true;
        }
        return !str.matches(regex);
    }
}