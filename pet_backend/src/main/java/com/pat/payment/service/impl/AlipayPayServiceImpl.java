package com.pat.payment.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.pat.order.domain.dto.PayNotifyDTO;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.domain.enums.OrderStatus;
import com.pat.order.helper.OrderStateMachine;
import com.pat.order.service.base.PurchaseOrderBaseService;
import com.pat.payment.config.AlipayConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
public class AlipayPayServiceImpl {

    private final AlipayConfig alipayConfig;
    private final PurchaseOrderBaseService baseService;

    public AlipayPayServiceImpl(AlipayConfig alipayConfig,
                                PurchaseOrderBaseService baseService) {
        this.alipayConfig = alipayConfig;
        this.baseService = baseService;
    }

    /**
     * 创建支付宝手机网站支付表单（POST 方式，返回 HTML 表单）
     */
    public String createWapPayPage(String outTradeNo,
                                   String subject,
                                   String body,
                                   String totalAmount) throws AlipayApiException {
        if (isBlank(alipayConfig.getAppId()) || isBlank(alipayConfig.getPrivateKey())) {
            log.warn("Alipay config is incomplete, returning mock pay form orderNo={}", outTradeNo);
            return "<form data-mock=\"alipay\" data-order-no=\"" + outTradeNo + "\"></form>";
        }

        DefaultAlipayClient client = buildClient();
        AlipayTradeWapPayRequest request = buildWapPayRequest(outTradeNo, subject, body, totalAmount);
        return client.pageExecute(request).getBody();
    }

    /**
     * 创建支付宝手机网站支付跳转 URL（GET 方式，适合小程序 webview 打开）
     */
    public String createWapPayUrl(String outTradeNo,
                                  String subject,
                                  String body,
                                  String totalAmount) throws AlipayApiException {
        if (isBlank(alipayConfig.getAppId()) || isBlank(alipayConfig.getPrivateKey())) {
            log.warn("Alipay config is incomplete, returning mock URL orderNo={}", outTradeNo);
            return "#";
        }

        DefaultAlipayClient client = buildClient();
        AlipayTradeWapPayRequest request = buildWapPayRequest(outTradeNo, subject, body, totalAmount);
        return client.pageExecute(request, "GET").getBody();
    }

    private DefaultAlipayClient buildClient() {
        return new DefaultAlipayClient(
                alipayConfig.getGateway(),
                alipayConfig.getAppId(),
                alipayConfig.getPrivateKey(),
                alipayConfig.getFormat(),
                alipayConfig.getCharset(),
                alipayConfig.getAlipayPublicKey(),
                alipayConfig.getSignType());
    }

    private AlipayTradeWapPayRequest buildWapPayRequest(String outTradeNo,
                                                         String subject,
                                                         String body,
                                                         String totalAmount) {
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        model.setOutTradeNo(outTradeNo);
        model.setSubject(subject);
        model.setBody(body);
        model.setTotalAmount(totalAmount);
        model.setProductCode("QUICK_WAP_WAY");

        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        request.setBizModel(model);
        request.setNotifyUrl(alipayConfig.getNotifyUrl());
        request.setReturnUrl(alipayConfig.getReturnUrl());
        return request;
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleNotify(PayNotifyDTO dto) {
        PurchaseOrder order = baseService.lambdaQuery()
                .eq(PurchaseOrder::getOrderNo, dto.getOutTradeNo())
                .one();
        if (order == null) {
            log.warn("Alipay notify ignored, order not found orderNo={}", dto.getOutTradeNo());
            return;
        }
        if (order.getOrderStatus() != null && order.getOrderStatus() == OrderStatus.PAID.getCode()) {
            log.info("Alipay notify ignored, order already paid orderNo={}", dto.getOutTradeNo());
            return;
        }

        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.PAID.getCode());
        order.setOrderStatus(OrderStatus.PAID.getCode());
        order.setPayTime(LocalDateTime.now());
        baseService.updateById(order);
        log.info("Alipay notify handled orderNo={}", dto.getOutTradeNo());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
