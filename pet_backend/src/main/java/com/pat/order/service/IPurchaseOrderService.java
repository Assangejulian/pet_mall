package com.pat.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pat.order.dto.OrderSubmitDTO;
import com.pat.order.domain.entity.PurchaseOrder;

public interface IPurchaseOrderService extends IService<PurchaseOrder> {
    PurchaseOrder createOrderFromCart(OrderSubmitDTO dto);
    boolean updateOrderStatus(Long orderId, Integer targetStatus, String reason);
}