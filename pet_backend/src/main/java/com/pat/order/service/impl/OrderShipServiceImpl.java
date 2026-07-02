package com.pat.order.service.impl;

import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.order.domain.dto.OrderShipDTO;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.domain.enums.OrderStatus;
import com.pat.order.helper.OrderStateMachine;
import com.pat.order.mapper.OrderQueryMapper;
import com.pat.order.service.OrderShipService;
import com.pat.order.service.base.PurchaseOrderBaseService;
import com.pat.store.service.IStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderShipServiceImpl implements OrderShipService {

    private static final Logger log = LoggerFactory.getLogger(OrderShipServiceImpl.class);

    private final PurchaseOrderBaseService baseService;
    private final OrderQueryMapper orderQueryMapper;
    private final IStoreService storeService;

    public OrderShipServiceImpl(PurchaseOrderBaseService baseService,
                                 OrderQueryMapper orderQueryMapper,
                                IStoreService storeService) {
        this.baseService = baseService;
        this.orderQueryMapper = orderQueryMapper;
        this.storeService = storeService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(OrderShipDTO dto, Long merchantUserId) {
        PurchaseOrder order = baseService.getById(dto.getOrderId());
        if (order == null) throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");

        if (merchantUserId != null) {
            List<Long> storeIds = storeService.getStoreIdsByUserId(merchantUserId);
            if (!storeIds.isEmpty()) {
                String idsStr = storeIds.stream().map(String::valueOf).collect(Collectors.joining(","));
                List<Long> orderIds = orderQueryMapper.selectOrderIdsByStoreIds(idsStr);
                if (!orderIds.contains(dto.getOrderId())) {
                    throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该订单");
                }
            }
        }

        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.SHIPPED.getCode());

        order.setOrderStatus(OrderStatus.SHIPPED.getCode());
        order.setShipTime(LocalDateTime.now());
        baseService.updateById(order);

        log.info("发货 orderId={}, logisticsNo={}, carrier={}, merchantUserId={}",
                dto.getOrderId(), dto.getLogisticsNo(), dto.getCarrier(), merchantUserId);
    }
}