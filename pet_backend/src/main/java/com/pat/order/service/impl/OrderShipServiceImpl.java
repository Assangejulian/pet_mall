package com.pat.order.service.impl;

import lombok.extern.slf4j.Slf4j;
import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.order.domain.dto.OrderShipDTO;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.domain.enums.OrderStatus;
import com.pat.order.helper.OrderStateMachine;
import com.pat.order.mapper.OrderQueryMapper;
import com.pat.order.service.OrderShipService;
import com.pat.order.service.base.PurchaseOrderBaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class OrderShipServiceImpl implements OrderShipService {

    private final PurchaseOrderBaseService baseService;
    private final OrderQueryMapper orderQueryMapper;

    public OrderShipServiceImpl(PurchaseOrderBaseService baseService,
                                 OrderQueryMapper orderQueryMapper) {
        this.baseService = baseService;
        this.orderQueryMapper = orderQueryMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(OrderShipDTO dto, Long merchantUserId) {
        String carrier = normalizeLogisticsText(dto.getCarrier(), "物流公司");
        String logisticsNo = normalizeLogisticsText(dto.getLogisticsNo(), "物流单号");
        PurchaseOrder order = baseService.getById(dto.getOrderId());
        if (order == null) throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");

        if (merchantUserId != null) {
            List<Long> orderIds = orderQueryMapper.selectOrderIdsByMerchantUserId(merchantUserId);
            if (!orderIds.contains(dto.getOrderId())
                    || orderQueryMapper.countItemsOutsideMerchant(dto.getOrderId(), merchantUserId) > 0) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作整单或订单包含其他商家商品");
            }
        }

        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.SHIPPED.getCode());

        order.setOrderStatus(OrderStatus.SHIPPED.getCode());
        order.setShipTime(LocalDateTime.now());
        order.setLogisticsCarrier(carrier);
        order.setLogisticsNo(logisticsNo);
        baseService.updateById(order);

        log.info("发货 orderId={}, logisticsNo={}, carrier={}, merchantUserId={}",
                dto.getOrderId(), logisticsNo, carrier, merchantUserId);
    }

    private String normalizeLogisticsText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.FARAMS_NULL_ERROR, fieldName + "不能为空");
        }
        String trimmed = value.trim();
        if (trimmed.length() > 100) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, fieldName + "长度不能超过100");
        }
        return trimmed;
    }
}
