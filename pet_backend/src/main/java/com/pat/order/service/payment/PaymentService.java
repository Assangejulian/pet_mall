package com.pat.order.service.payment;

import com.pat.order.domain.dto.PayNotifyDTO;
import com.pat.order.domain.vo.OrderPaymentVO;
import com.pat.order.domain.entity.PurchaseOrder;

/**
 * 支付策略接口 —— 每种支付方式一个实现
 */
public interface PaymentService {

    /** 执行支付，返回支付结果 */
    OrderPaymentVO pay(PurchaseOrder order);

    /** 处理支付回调通知 */
    void handleNotify(PayNotifyDTO dto);
}
