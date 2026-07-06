package com.pat.order.task;

import com.pat.order.service.support.OrderCancelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 定时扫描超时未支付订单并自动取消。
 *
 * <p>当前使用 {@code @Scheduled} 每 5 分钟轮询 DB，
 * 实际取消逻辑委托给 {@link OrderCancelService#cancelExpiredOrders(LocalDateTime)}。
 * 如需改为 Redis ZSET / MQ 延迟消息，只需新增一个触发器调用 {@code OrderCancelService.cancelOrder()} 即可。</p>
 */
@Slf4j
@Component
public class OrderCancelTask {

    private static final int CANCEL_TIMEOUT_MINUTES = 30;

    private final OrderCancelService orderCancelService;

    public OrderCancelTask(OrderCancelService orderCancelService) {
        this.orderCancelService = orderCancelService;
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void cancelExpiredOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(CANCEL_TIMEOUT_MINUTES);
        int count = orderCancelService.cancelExpiredOrders(deadline);
        if (count > 0) {
            log.info("定时取消超时订单完成，共 {} 笔", count);
        }
    }
}
