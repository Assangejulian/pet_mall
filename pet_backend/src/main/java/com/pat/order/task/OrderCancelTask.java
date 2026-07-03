package com.pat.order.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.pat.order.domain.entity.OrderItem;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.mapper.OrderItemMapper;
import com.pat.order.service.base.PurchaseOrderBaseService;
import com.pat.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** 扫描超时未支付订单自动取消 */
@Component
@Slf4j
public class OrderCancelTask {
    private static final int CANCEL_TIMEOUT_MINUTES = 30;

    private final PurchaseOrderBaseService baseService;
    private final OrderItemMapper orderItemMapper;
    private final ProductService productService;

    public OrderCancelTask(PurchaseOrderBaseService baseService,
                           OrderItemMapper orderItemMapper,
                           ProductService productService) {
        this.baseService = baseService;
        this.orderItemMapper = orderItemMapper;
        this.productService = productService;
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional(rollbackFor = Exception.class)
    public void cancelExpiredOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(CANCEL_TIMEOUT_MINUTES);

        // 1. 查询超时待支付订单
        List<PurchaseOrder> expiredOrders = baseService.list(new QueryWrapper<PurchaseOrder>()
                .eq("order_status", 0)
                .lt("create_time", deadline));

        for (PurchaseOrder order : expiredOrders) {
            // 2. 恢复库存
            List<OrderItem> items = orderItemMapper.selectList(
                    new QueryWrapper<OrderItem>().eq("order_id", order.getId()));
            for (OrderItem item : items) {
                productService.restoreStock(item.getProductId(), item.getQuantity());
                log.info("恢复库存 productId={}, quantity={}, orderNo={}",
                        item.getProductId(), item.getQuantity(), order.getOrderNo());
            }

            // 3. 更新订单状态
            order.setOrderStatus(-1);
            order.setCancelReason("超时未支付，系统自动取消");
            order.setCancelTime(LocalDateTime.now());
            baseService.updateById(order);
        }

        if (!expiredOrders.isEmpty()) {
            log.info("已取消 {} 笔超时未支付订单并恢复库存", expiredOrders.size());
        }
    }
}
