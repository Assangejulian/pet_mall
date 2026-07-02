package com.pat.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AuditorAuthInterceptor extends RoleAuthInterceptor {
    public AuditorAuthInterceptor(ObjectMapper objectMapper) {
        super(objectMapper, Set.of("auditor", "admin"));
    }
}
