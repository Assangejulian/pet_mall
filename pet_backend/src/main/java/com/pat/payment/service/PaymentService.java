package com.pat.payment.service;

import com.pat.order.domain.dto.PayNotifyDTO;
import com.pat.order.domain.vo.OrderPaymentVO;
import com.pat.order.domain.entity.PurchaseOrder;

public interface PaymentService {

    /**
     * 执行支付。各实现类按各自策略处理。
     *
     * @param order 待支付订单
     * @return 支付结果 VO
     */
    OrderPaymentVO pay(PurchaseOrder order);

    /**
     * 处理支付回调通知。
     *
     * @param dto 回调参数
     */
    void handleNotify(PayNotifyDTO dto);
}