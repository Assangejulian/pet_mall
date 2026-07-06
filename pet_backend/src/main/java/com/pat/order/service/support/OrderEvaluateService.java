package com.pat.order.service.support;

import com.pat.order.domain.dto.OrderEvaluateDTO;
import com.pat.order.domain.entity.OrderItem;
import com.pat.order.mapper.OrderItemMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单评价服务。
 *
 * <p>负责逐项更新订单明细的评价内容。抽取为独立 Service 以保持编排层纯净。</p>
 */
@Service
public class OrderEvaluateService {

    private final OrderItemMapper orderItemMapper;

    public OrderEvaluateService(OrderItemMapper orderItemMapper) {
        this.orderItemMapper = orderItemMapper;
    }

    /**
     * 逐项更新评价内容。
     *
     * @param orderId 订单 ID
     * @param items   评价项列表
     * @return 评价时间（所有项使用同一时间戳）
     */
    public LocalDateTime updateEvaluations(Long orderId, List<OrderEvaluateDTO.ItemEvaluate> items) {
        List<OrderItem> existingItems = orderItemMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<OrderItem>()
                        .eq("order_id", orderId));
        Map<Long, OrderItem> itemMap = existingItems.stream()
                .collect(Collectors.toMap(OrderItem::getId, item -> item));

        LocalDateTime now = LocalDateTime.now();
        if (items != null) {
            for (OrderEvaluateDTO.ItemEvaluate ie : items) {
                OrderItem item = itemMap.get(ie.getOrderItemId());
                if (item != null) {
                    item.setEvaluateContent(ie.getContent());
                    item.setEvaluateTime(now);
                    orderItemMapper.updateById(item);
                }
            }
        }
        return now;
    }
}