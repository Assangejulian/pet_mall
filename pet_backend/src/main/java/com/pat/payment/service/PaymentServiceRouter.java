package com.pat.payment.service;

import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PaymentServiceRouter {

    @Autowired
    private Map<String, PaymentService> serviceMap;

    public PaymentService getService(String payMethod) {
        if (payMethod == null || payMethod.isBlank()) {
            payMethod = "mock";
        }
        PaymentService svc = serviceMap.get(payMethod + "PaymentService");
        if (svc == null) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "不支持的支付方式: " + payMethod);
        }
        return svc;
    }
}
