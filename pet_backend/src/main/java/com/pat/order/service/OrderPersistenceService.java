package com.pat.order.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.order.domain.dto.OrderCreateDTO;
import com.pat.order.domain.entity.OrderItem;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.domain.enums.OrderStatus;
import com.pat.order.mapper.OrderItemMapper;
import com.pat.order.service.base.PurchaseOrderBaseService;
import com.pat.user.domain.entity.UserAddress;
import com.pat.user.mapper.UserAddressMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 订单持久化。
 * <p>只负责：地址快照、保存订单头 + 订单明细。</p>
 */
@Service
public class OrderPersistenceService {

    private final PurchaseOrderBaseService baseService;
    private final OrderItemMapper orderItemMapper;
    private final UserAddressMapper addressMapper;

    public OrderPersistenceService(PurchaseOrderBaseService baseService,
                                   OrderItemMapper orderItemMapper,
                                   UserAddressMapper addressMapper) {
        this.baseService = baseService;
        this.orderItemMapper = orderItemMapper;
        this.addressMapper = addressMapper;
    }

    /**
     * 根据地址 ID 查询并生成地址快照 JSON。
     */
    public String snapshotAddressById(Long addressId) {
        UserAddress addr = addressMapper.selectById(addressId);
        if (addr == null) throw new BusinessException(ErrorCode.NOT_FOUND, "收货地址不存在");
        LinkedHashMap<String, String> snapshot = new LinkedHashMap<>();
        snapshot.put("receiverName", addr.getReceiverName());
        snapshot.put("phone", addr.getPhone());
        snapshot.put("province", addr.getProvince());
        snapshot.put("city", addr.getCity());
        snapshot.put("district", addr.getDistrict());
        snapshot.put("detail", addr.getDetail());
        return JSONUtil.toJsonStr(snapshot);
    }

    /**
     * 保存订单头 + 明细，返回订单 ID。
     */
    public Long save(Long userId, OrderCreateDTO dto,
                     List<OrderItem> orderItems,
                     String addressSnapshot,
                     BigDecimal totalAmount,
                     BigDecimal discountAmount,
                     BigDecimal payAmount) {
        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo(IdUtil.fastSimpleUUID());
        order.setUserId(userId);
        order.setAddressId(dto.getAddressId());
        order.setAddressSnapshot(addressSnapshot);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setPayAmount(payAmount);
        order.setRemark(dto.getRemark());
        order.setOrderStatus(OrderStatus.PENDING_PAY.getCode());
        baseService.save(order);

        Long orderId = order.getId();
        for (OrderItem oi : orderItems) {
            oi.setOrderId(orderId);
            orderItemMapper.insert(oi);
        }
        return orderId;
    }
}