package com.pat.order.controller;

import com.pat.common.domain.Result;
import com.pat.order.domain.dto.PayNotifyDTO;
import com.pat.order.service.payment.PaymentServiceRouter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 支付回调 —— 根据回调 URL 路由到对应支付策略的 handleNotify
 */
@Slf4j
@RestController
@RequestMapping("/api/notify")
@Tag(name = "支付回调", description = "第三方支付平台回调通知")
public class PayNotifyController {

    private final PaymentServiceRouter paymentServiceRouter;

    public PayNotifyController(PaymentServiceRouter paymentServiceRouter) {
        this.paymentServiceRouter = paymentServiceRouter;
    }

    @Operation(summary = "支付成功回调（模拟/默认）")
    @PostMapping("/pay-success")
    public Result<Void> paySuccess(@Valid @RequestBody PayNotifyDTO dto) {
        String orderNo = dto.getOutTradeNo();
        log.info("支付成功回调, 订单号: {}", orderNo);
        paymentServiceRouter.getService("mock").handleNotify(dto);
        return Result.success();
    }

    @Operation(summary = "支付宝异步通知回调")
    @PostMapping("/alipay")
    public Result<Void> alipayNotify(@Valid @RequestBody PayNotifyDTO dto) {
        log.info("支付宝回调, 订单号: {}", dto.getOutTradeNo());
        paymentServiceRouter.getService("ALIPAY").handleNotify(dto);
        return Result.success();
    }

    @Operation(summary = "微信支付异步通知回调")
    @PostMapping("/wechat")
    public Result<Void> wechatNotify(@Valid @RequestBody PayNotifyDTO dto) {
        log.info("微信回调, 订单号: {}", dto.getOutTradeNo());
        paymentServiceRouter.getService("WECHAT").handleNotify(dto);
        return Result.success();
    }
}
