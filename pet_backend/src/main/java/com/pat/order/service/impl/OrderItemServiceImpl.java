package com.pat.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pat.order.domain.entity.OrderItem;
import com.pat.order.mapper.OrderItemMapper;
import com.pat.order.service.IOrderItemService;
import org.springframework.stereotype.Service;

/**
 * 订单明细基础 CRUD 实现。
 *
 * <p>继承 MyBatis-Plus ServiceImpl 提供标准的增删改查能力，
 * 复杂查询请直接使用 {@link OrderItemMapper}。</p>
 */
@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements IOrderItemService {
}