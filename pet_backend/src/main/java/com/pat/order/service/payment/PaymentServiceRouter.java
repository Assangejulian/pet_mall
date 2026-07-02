package com.pat.order.service.payment;

import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 支付策略路由器 —— 根据 payMethod 分发到对应的 PaymentService
 * <p>Spring 自动注入 Map&lt;String, PaymentService&gt;，key 为 bean name</p>
 */
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
