package com.pat.user.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AdminAuthInterceptor extends RoleAuthInterceptor {

    public AdminAuthInterceptor(ObjectMapper objectMapper) {
        super(objectMapper, Set.of("admin"));
    }
}
