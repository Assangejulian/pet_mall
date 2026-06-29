package com.pat.order.task;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.service.IPurchaseOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 订单定时取消任务
 * 扫描超过30分钟未支付的订单自动取消
 */
@Component
public class OrderCancelTask {

    private static final Logger log = LoggerFactory.getLogger(OrderCancelTask.class);
    private static final int CANCEL_TIMEOUT_MINUTES = 30;

    private final IPurchaseOrderService orderService;

    public OrderCancelTask(IPurchaseOrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional(rollbackFor = Exception.class)
    public void cancelExpiredOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(CANCEL_TIMEOUT_MINUTES);
        LambdaUpdateWrapper<PurchaseOrder> wrapper = new LambdaUpdateWrapper<PurchaseOrder>()
                .eq(PurchaseOrder::getOrderStatus, 0)
                .lt(PurchaseOrder::getCreateTime, deadline)
                .set(PurchaseOrder::getOrderStatus, -1)
                .set(PurchaseOrder::getCancelReason, "超时未支付，系统自动取消")
                .set(PurchaseOrder::getCancelTime, LocalDateTime.now());

        try {
            orderService.update(wrapper);
            log.info("已扫描并取消超时未支付订单");
        } catch (Exception e) {
            log.error("取消超时订单异常", e);
        }
    }
}
