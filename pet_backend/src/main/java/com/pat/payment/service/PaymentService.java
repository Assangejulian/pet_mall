package com.pat.payment.service;

import com.pat.order.domain.dto.PayNotifyDTO;
import com.pat.order.domain.vo.OrderPaymentVO;
import com.pat.order.domain.entity.PurchaseOrder;

public interface PaymentService {

    OrderPaymentVO pay(PurchaseOrder order);

    void handleNotify(PayNotifyDTO dto);
}
