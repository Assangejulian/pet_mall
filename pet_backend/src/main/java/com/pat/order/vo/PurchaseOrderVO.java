package com.pat.order.vo;

import com.pat.order.domain.entity.PurchaseOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaseOrderVO extends PurchaseOrder {
    private List<OrderItemVO> items;
}
