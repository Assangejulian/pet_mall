package com.pat.order.vo;

import com.pat.order.domain.entity.OrderItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单商品明细 VO —— 直接复用 OrderItem 的字段
 * 注意：productName、productImage、price 均继承自 OrderItem，无需重复声明。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderItemVO extends OrderItem {
    // 无额外字段；字段均由父类 OrderItem 提供
}
