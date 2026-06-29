package com.pat.order.vo;

import com.pat.order.domain.entity.OrderItem;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderItemVO extends OrderItem {
    private String productName;
    private String mainImage;
    private java.math.BigDecimal price;
}
