package com.pat.payment.service;

import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.order.domain.dto.PayNotifyDTO;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.domain.vo.OrderPaymentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("ALIPAYPaymentService")
public class AliPaymentService implements PaymentService {

    @Override
    public OrderPaymentVO pay(PurchaseOrder order) {
        throw new BusinessException(ErrorCode.FARAMS_ERROR, "支付宝支付尚未接入");
    }

    @Override
    public void handleNotify(PayNotifyDTO dto) {
        throw new BusinessException(ErrorCode.FARAMS_ERROR, "支付宝支付尚未接入");
    }
}
