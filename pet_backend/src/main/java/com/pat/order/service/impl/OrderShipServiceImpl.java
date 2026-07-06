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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 发货服务实现。
 *
 * <p>商家和管理员共用。商家发货时校验店铺归属权（merchantUserId），管理员发货不限。
 * 发货后更新订单状态为 {@link OrderStatus#SHIPPED}。</p>
 */
@Slf4j
@Service
public class OrderShipServiceImpl implements OrderShipService {

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

    /**
     * 执行发货操作。
     *
     * <p>商家调用时（merchantUserId 非空）校验该订单是否属于其店铺；
     * 管理员调用时（merchantUserId 为空）不限制。</p>
     *
     * @param dto            发货参数（订单 ID、物流单号、承运商）
     * @param merchantUserId 商家用户 ID（管理员传 null）
     * @throws BusinessException 订单不存在、无权限或状态不合法时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(OrderShipDTO dto, Long merchantUserId) {
        PurchaseOrder order = baseService.getById(dto.getOrderId());
        if (order == null) throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");

        // 商家发货时校验店铺归属权
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