package com.pat.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class MerchantAuthInterceptor extends RoleAuthInterceptor {
    public MerchantAuthInterceptor(ObjectMapper objectMapper) {
        super(objectMapper, Set.of("merchant"));
    }
}
