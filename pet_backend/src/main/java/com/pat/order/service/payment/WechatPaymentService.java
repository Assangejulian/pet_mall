package com.pat.order.service.payment;

import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.order.domain.dto.PayNotifyDTO;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.domain.vo.OrderPaymentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 微信支付 —— 待接入真实 SDK
 * <p>authType = WECHAT</p>
 */
@Slf4j
@Service("WECHATPaymentService")
public class WechatPaymentService implements PaymentService {

    @Override
    public OrderPaymentVO pay(PurchaseOrder order) {
        // TODO: 接入微信支付 SDK，获取支付链接/二维码
        throw new BusinessException(ErrorCode.FARAMS_ERROR, "微信支付尚未接入");
    }

    @Override
    public void handleNotify(PayNotifyDTO dto) {
        // TODO: 验证微信支付异步通知签名
        throw new BusinessException(ErrorCode.FARAMS_ERROR, "微信支付尚未接入");
    }
}
