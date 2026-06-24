package com.pat.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pat.order.entity.OrderItem;
import com.pat.order.mapper.OrderItemMapper;
import com.pat.order.service.IOrderItemService;
import org.springframework.stereotype.Service;

@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements IOrderItemService {
}