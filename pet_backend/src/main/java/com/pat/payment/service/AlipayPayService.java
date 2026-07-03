package com.pat.payment.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pat.order.domain.dto.PayNotifyDTO;
import com.pat.order.domain.entity.OrderItem;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.domain.enums.OrderStatus;
import com.pat.order.helper.OrderStateMachine;
import com.pat.order.mapper.OrderItemMapper;
import com.pat.order.service.base.PurchaseOrderBaseService;
import com.pat.payment.config.AlipayConfig;
import com.pat.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class AlipayPayService {

    private final AlipayConfig alipayConfig;
    private final PurchaseOrderBaseService baseService;
    private final OrderItemMapper orderItemMapper;
    private final ProductService productService;

    public AlipayPayService(AlipayConfig alipayConfig,
                            PurchaseOrderBaseService baseService,
                            OrderItemMapper orderItemMapper,
                            ProductService productService) {
        this.alipayConfig = alipayConfig;
        this.baseService = baseService;
        this.orderItemMapper = orderItemMapper;
        this.productService = productService;
    }

    public String createWapPayPage(String outTradeNo,
                                   String subject,
                                   String body,
                                   String totalAmount) throws AlipayApiException {
        if (isBlank(alipayConfig.getAppId()) || isBlank(alipayConfig.getPrivateKey())) {
            log.warn("Alipay config is incomplete, returning mock pay form orderNo={}", outTradeNo);
            return "<form data-mock=\"alipay\" data-order-no=\"" + outTradeNo + "\"></form>";
        }

        DefaultAlipayClient client = new DefaultAlipayClient(
                alipayConfig.getGateway(),
                alipayConfig.getAppId(),
                alipayConfig.getPrivateKey(),
                alipayConfig.getFormat(),
                alipayConfig.getCharset(),
                alipayConfig.getAlipayPublicKey(),
                alipayConfig.getSignType());

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
        return client.pageExecute(request).getBody();
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
        List<OrderItem> items = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", order.getId()));
        for (OrderItem item : items) {
            boolean ok = productService.deductStock(item.getProductId(), item.getQuantity());
            if (!ok) {
                log.warn("Alipay notify stock deduction failed productId={}, orderNo={}",
                        item.getProductId(), dto.getOutTradeNo());
            }
        }

        order.setOrderStatus(OrderStatus.PAID.getCode());
        order.setPayTime(LocalDateTime.now());
        baseService.updateById(order);
        log.info("Alipay notify handled orderNo={}", dto.getOutTradeNo());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
