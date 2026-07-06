package com.pat.order.service.support;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pat.common.service.StockDeductionService;
import com.pat.order.domain.entity.OrderItem;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.mapper.OrderItemMapper;
import com.pat.order.service.base.PurchaseOrderBaseService;
import com.pat.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单取消服务 —— 封装取消订单的核心业务逻辑。
 *
 * <p>无论是 {@code @Scheduled} 定时扫描、Redis ZSET 轮询、还是 MQ 延迟消息触发，
 * 都调此类的 {@link #cancelOrder(PurchaseOrder)} 方法，保证取消逻辑统一。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCancelService {

    private final PurchaseOrderBaseService baseService;
    private final OrderItemMapper orderItemMapper;
    private final ProductService productService;
    private final StockDeductionService stockDeductionService;

    /**
     * 取消单个订单：恢复库存 → 更新订单状态。
     *
     * @param order 待取消的订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(PurchaseOrder order) {
        // 1. 恢复库存（DB + Redis）
        List<OrderItem> items = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", order.getId()));
        for (OrderItem item : items) {
            productService.restoreStock(item.getProductId(), item.getQuantity());
            stockDeductionService.restore(item.getProductId(), item.getQuantity());
        }

        // 2. 更新订单状态
        order.setOrderStatus(-1);
        order.setCancelReason("超时未支付，系统自动取消");
        order.setCancelTime(LocalDateTime.now());
        baseService.updateById(order);

        log.info("订单已取消 orderId={}, orderNo={}, items={}",
                order.getId(), order.getOrderNo(), items.size());
    }

    /**
     * 批量取消超时订单（供 {@code @Scheduled} 调用）。
     *
     * @param deadline 超时时间点
     * @return 取消的订单数
     */
    @Transactional(rollbackFor = Exception.class)
    public int cancelExpiredOrders(LocalDateTime deadline) {
        List<PurchaseOrder> expiredOrders = baseService.list(new QueryWrapper<PurchaseOrder>()
                .eq("order_status", 0)
                .lt("create_time", deadline));

        for (PurchaseOrder order : expiredOrders) {
            cancelOrder(order);
        }

        if (!expiredOrders.isEmpty()) {
            log.info("批量取消超时订单 {} 笔", expiredOrders.size());
        }
        return expiredOrders.size();
    }
}
