package com.pat.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.order.domain.dto.OrderCreateDTO;
import com.pat.order.domain.dto.OrderPaymentDTO;
import com.pat.order.domain.vo.OrderPaymentVO;
import com.pat.order.domain.entity.OrderItem;
import com.pat.order.domain.entity.PurchaseOrder;

import java.util.List;

/** 用户端订单操作：下单、查询 */
public interface IOrderUserService {
    Long createOrder(OrderCreateDTO dto);
    IPage<PurchaseOrder> getUserOrderList(Integer orderStatus, Page<PurchaseOrder> page);
    PurchaseOrder getUserOrderDetail(Long id);
    List<OrderItem> getUserOrderItems(Long orderId);
    OrderPaymentVO payOrder(OrderPaymentDTO dto);
    PurchaseOrder confirmReceive(Long id);
}

