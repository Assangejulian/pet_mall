package com.pat.order.service.support;

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
 *
 * <p>负责地址快照生成和订单数据落库。地址快照用于在订单创建后冻结收货地址，
 * 防止用户后续修改地址影响历史订单。</p>
 */
@Service
public class OrderCreationService {

    private final PurchaseOrderBaseService baseService;
    private final OrderItemMapper orderItemMapper;
    private final UserAddressMapper addressMapper;

    public OrderCreationService(PurchaseOrderBaseService baseService,
                                OrderItemMapper orderItemMapper,
                                UserAddressMapper addressMapper) {
        this.baseService = baseService;
        this.orderItemMapper = orderItemMapper;
        this.addressMapper = addressMapper;
    }

    /**
     * 根据地址 ID 查询并生成地址快照 JSON。
     *
     * @param addressId 收货地址 ID
     * @return JSON 格式的地址快照
     * @throws BusinessException 地址不存在时抛出
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
     * 保存订单头 + 明细。
     *
     * @param userId         用户 ID
     * @param dto            下单参数
     * @param orderItems     OrderItem 列表
     * @param addressSnapshot 地址快照 JSON
     * @param totalAmount    商品原价合计
     * @param discountAmount 优惠金额
     * @param payAmount      实付金额
     * @return 新订单 ID
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