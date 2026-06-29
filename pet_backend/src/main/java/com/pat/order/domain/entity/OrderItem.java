package com.pat.order.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pat.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_item")
public class OrderItem extends BaseEntity {
    private Long orderId;
    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal price;
    private Integer quantity;
    private String evaluateContent;
    private Integer evaluateStar;
    private LocalDateTime evaluateTime;

    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private LocalDateTime updateTime;
}
