package com.pat.payment.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PaymentCallback {
    void onPaymentSuccess(String orderNo, BigDecimal amount, LocalDateTime payTime);
}
