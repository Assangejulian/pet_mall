package com.pat.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.order.domain.entity.OrderItem;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.helper.OrderStateMachine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.pat.order.mapper.OrderItemMapper;
import com.pat.order.service.IOrderAdminService;
import com.pat.order.service.base.PurchaseOrderBaseService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** 管理端订单操作：列表、状态变更、退单审核 */
@Service
public class OrderAdminServiceImpl implements IOrderAdminService {

    private static final Logger log = LoggerFactory.getLogger(OrderAdminServiceImpl.class);

    private final PurchaseOrderBaseService baseService;
    private final OrderItemMapper orderItemMapper;

    public OrderAdminServiceImpl(PurchaseOrderBaseService baseService,
                                 OrderItemMapper orderItemMapper) {
        this.baseService = baseService;
        this.orderItemMapper = orderItemMapper;
    }

    @Override
    public IPage<PurchaseOrder> pageList(PurchaseOrder param, Page<PurchaseOrder> page) {
        QueryWrapper<PurchaseOrder> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        if (param != null) {
            if (param.getOrderStatus() != null) wrapper.eq("order_status", param.getOrderStatus());
            if (param.getUserId() != null) wrapper.eq("user_id", param.getUserId());
            if (param.getOrderNo() != null && !param.getOrderNo().isBlank())
                wrapper.like("order_no", param.getOrderNo());
        }
        return baseService.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Map<String, Object> body) {
        PurchaseOrder order = baseService.getById(id);
        if (order == null) throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");

        Integer current = order.getOrderStatus();
        int target = Integer.parseInt(body.get("status").toString());
        OrderStateMachine.validate(current, target);

        order.setOrderStatus(target);
        String reason = (String) body.get("cancelReason");
        LocalDateTime now = LocalDateTime.now();
        switch (target) {
            case -1: order.setCancelReason(reason); order.setCancelTime(now); break;
            case 1:  order.setPayTime(now); break;
            case 2:  order.setShipTime(now); break;
            case 3:
                order.setReceiveTime(now);
                if (current == -2) order.setRefundAuditTime(now);
                break;
            case 4:  order.setEvaluateTime(now); break;
            case -3:
            case -4:
                order.setRefundAuditTime(now);
                order.setCancelReason(reason);
                break;
        }
        baseService.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void paySuccess(String orderNo) {
        PurchaseOrder order = baseService.lambdaQuery()
                .eq(PurchaseOrder::getOrderNo, orderNo)
                .one();
        if (order == null) {
            log.warn("paySuccess 订单不存在: {}", orderNo);
            return;
        }
        if (order.getOrderStatus() != 0) return;
        order.setOrderStatus(1);
        order.setPayTime(LocalDateTime.now());
        baseService.updateById(order);
        log.info("订单支付成功: {}", orderNo);
    }

    @Override
    public Map<String, Object> getDetail(Long id) {
        PurchaseOrder order = baseService.getById(id);
        if (order == null) throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");

        List<OrderItem> items = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", id));
        Map<String, Object> map = BeanUtil.beanToMap(order);
        map.put("items", items);
        return map;
    }
}
