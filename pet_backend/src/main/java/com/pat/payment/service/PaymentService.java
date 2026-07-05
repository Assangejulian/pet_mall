package com.pat.payment.service;

import com.pat.payment.domain.dto.PaymentContext;
import com.pat.payment.domain.dto.PaymentNotify;
import com.pat.payment.domain.vo.OrderPaymentVO;

public interface PaymentService {

    /**
     * 执行支付下单。各实现类按各自策略处理。
     *
     * @param context 支付上下文（订单号、金额等）
     * @return 支付结果 VO
     */
    OrderPaymentVO pay(PaymentContext context);

    /**
     * 处理支付回调通知。
     *
     * @param notify   回调参数
     * @param callback 支付成功回调（由调用方实现，更新订单状态）
     */
    void handleNotify(PaymentNotify notify, PaymentCallback callback);
}
