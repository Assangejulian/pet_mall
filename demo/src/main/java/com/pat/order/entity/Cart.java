package com.pat.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.pat.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cart")
public class Cart extends BaseEntity {
    private Long userId;
    private Long productId;
    private Integer quantity;
    private Integer checked;
}
