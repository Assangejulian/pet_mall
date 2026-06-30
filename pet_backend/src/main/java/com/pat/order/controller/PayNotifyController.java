package com.pat.order.controller;

import com.pat.common.domain.Result;
import com.pat.order.service.IOrderAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/notify")
@Tag(name = "支付回调", description = "第三方支付平台回调通知")
public class PayNotifyController {

    private final IOrderAdminService orderAdminService;

    public PayNotifyController(IOrderAdminService orderAdminService) {
        this.orderAdminService = orderAdminService;
    }

    @Operation(summary = "支付成功回调（微信/支付宝通知）")
    @PostMapping("/pay-success")
    public Result<Void> paySuccess(@RequestBody Map<String, Object> body) {
        String orderNo = (String) body.get("out_trade_no");
        log.info("支付成功回调, 订单号: {}", orderNo);
        if (orderNo != null) {
            orderAdminService.paySuccess(orderNo);
        }
        return Result.success();
    }
}
