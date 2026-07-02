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
    /**
     * 支付宝支付（暂未接入，抛出异常提示）。
     *
     * @param order 待支付订单
     * @return 支付结果 VO
     */
    public OrderPaymentVO pay(PurchaseOrder order) {
        throw new BusinessException(ErrorCode.FARAMS_ERROR, "支付宝支付尚未接入");
    }

    @Override
    /**
     * 支付宝回调处理（暂未接入）。
     *
     * @param dto 回调参数
     */
    public void handleNotify(PayNotifyDTO dto) {
        throw new BusinessException(ErrorCode.FARAMS_ERROR, "支付宝支付尚未接入");
    }
}