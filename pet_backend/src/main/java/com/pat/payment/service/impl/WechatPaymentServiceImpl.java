package com.pat.payment.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.pat.common.exception.BusinessException;
import com.pat.order.domain.dto.PayNotifyDTO;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.domain.enums.OrderStatus;
import com.pat.order.domain.vo.OrderPaymentVO;
import com.pat.order.helper.OrderStateMachine;
import com.pat.order.service.base.PurchaseOrderBaseService;
import com.pat.payment.config.WechatPayConfig;
import com.pat.payment.service.PaymentService;
import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.cipher.Signer;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service("WECHATPaymentService")
public class WechatPaymentServiceImpl implements PaymentService {

    private final WechatPayConfig payConfig;
    private final PurchaseOrderBaseService baseService;

    public WechatPaymentServiceImpl(WechatPayConfig payConfig,
                                    PurchaseOrderBaseService baseService) {
        this.payConfig = payConfig;
        this.baseService = baseService;
    }

    @Override
    public OrderPaymentVO pay(PurchaseOrder order) {
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.PAID.getCode());

        if (isBlank(payConfig.getMchId()) || isBlank(payConfig.getPrivateKey())) {
            log.warn("微信支付未配置完整，使用 mock prepay_id orderNo={}", order.getOrderNo());
            return mockResult(order);
        }

        try {
            Config config = new RSAAutoCertificateConfig.Builder()
                    .merchantId(payConfig.getMchId())
                    .privateKey(payConfig.getPrivateKey())
                    .merchantSerialNumber(payConfig.getMchSerialNo())
                    .apiV3Key(payConfig.getApiV3Key())
                    .build();

            JsapiService service = new JsapiService.Builder().config(config).build();

            // 组装下单请求
            PrepayRequest request = new PrepayRequest();
            request.setAppid(payConfig.getAppid());
            request.setMchid(payConfig.getMchId());
            request.setOutTradeNo(order.getOrderNo());
            request.setDescription("宠铺 - " + order.getOrderNo());
            request.setNotifyUrl(payConfig.getNotifyUrl());

            Amount amount = new Amount();
            amount.setTotal(order.getPayAmount().multiply(new BigDecimal("100")).intValue());
            amount.setCurrency("CNY");
            request.setAmount(amount);

            Payer payer = new Payer();
            payer.setOpenid("");
            request.setPayer(payer);

            PrepayResponse resp = service.prepay(request);
            String prepayId = resp.getPrepayId();

            // JSAPI 调起支付签名
            long timestamp = System.currentTimeMillis() / 1000;
            String nonceStr = IdUtil.fastSimpleUUID();
            String packageStr = "prepay_id=" + prepayId;
            String signSrc = payConfig.getAppid() + "\n" + timestamp + "\n" + nonceStr + "\n" + packageStr + "\n";

            Signer signer = config.createSigner();
            String paySign = signer.sign(signSrc).getSign();

            Map<String, String> params = new LinkedHashMap<>();
            params.put("appId", payConfig.getAppid());
            params.put("timeStamp", String.valueOf(timestamp));
            params.put("nonceStr", nonceStr);
            params.put("package", packageStr);
            params.put("signType", "RSA");
            params.put("paySign", paySign);

            log.info("微信小程序支付下单成功 orderNo={}, prepayId={}", order.getOrderNo(), prepayId);
            return new OrderPaymentVO(order.getId(), order.getOrderNo(),
                    OrderStatus.PENDING_PAY.getCode(), order.getPayAmount(), null,
                    JSONUtil.toJsonStr(params), null);

        } catch (Exception e) {
            log.error("微信支付下单异常 orderNo={}", order.getOrderNo(), e);
            throw new BusinessException(500, "微信支付下单失败: " + e.getMessage(), null);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleNotify(PayNotifyDTO dto) {
        PurchaseOrder order = baseService.lambdaQuery()
                .eq(PurchaseOrder::getOrderNo, dto.getOutTradeNo())
                .one();
        if (order == null) { return; }
        if (order.getOrderStatus() != null && order.getOrderStatus() == OrderStatus.PAID.getCode()) { return; }

        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.PAID.getCode());
        order.setOrderStatus(OrderStatus.PAID.getCode());
        order.setPayTime(LocalDateTime.now());
        baseService.updateById(order);
    }

    private OrderPaymentVO mockResult(PurchaseOrder order) {
        long ts = System.currentTimeMillis() / 1000;
        String nonce = IdUtil.fastSimpleUUID();
        Map<String, String> params = new LinkedHashMap<>();
        params.put("appId", payConfig.getAppid());
        params.put("timeStamp", String.valueOf(ts));
        params.put("nonceStr", nonce);
        params.put("package", "prepay_id=mock_" + order.getOrderNo());
        params.put("signType", "RSA");
        params.put("paySign", "mock_sign");
        return new OrderPaymentVO(order.getId(), order.getOrderNo(),
                OrderStatus.PENDING_PAY.getCode(), order.getPayAmount(), null,
                JSONUtil.toJsonStr(params), null);
    }

    private boolean isBlank(String s) { return s == null || s.isBlank(); }
}