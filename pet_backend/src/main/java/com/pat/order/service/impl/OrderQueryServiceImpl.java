package com.pat.order.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.order.domain.entity.OrderItem;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.mapper.OrderQueryMapper;
import com.pat.order.service.OrderQueryService;
import com.pat.order.service.base.PurchaseOrderBaseService;
import com.pat.store.service.IStoreService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单查询服务实现。
 *
 * <p>提供订单分页查询和详情查询能力，支持商家按店铺筛选（merchantUserId 控制数据范围）。
 * 查询结果会富化地址快照、收货人姓名等信息。</p>
 */
@Service
public class OrderQueryServiceImpl implements OrderQueryService {

    private final PurchaseOrderBaseService baseService;
    private final OrderQueryMapper orderQueryMapper;
    private final IStoreService storeService;

    public OrderQueryServiceImpl(PurchaseOrderBaseService baseService,
                                 OrderQueryMapper orderQueryMapper,
                                 IStoreService storeService) {
        this.baseService = baseService;
        this.orderQueryMapper = orderQueryMapper;
        this.storeService = storeService;
    }

    /**
     * 分页查询订单列表。
     *
     * <p>商家调用时（merchantUserId 非空）只返回其店铺的订单；
     * 管理员调用时（merchantUserId 为空）返回全部订单。
     * 批量查询订单明细，避免 N+1 问题。</p>
     *
     * @param param          查询参数（orderStatus、userId、orderNo）
     * @param page           分页参数
     * @param merchantUserId 商家用户 ID（管理员传 null）
     * @return 富化后的订单分页结果（含地址、明细、状态信息）
     */
    @Override
    public IPage<Map<String, Object>> pageList(PurchaseOrder param, Page<?> page, Long merchantUserId) {
        QueryWrapper<PurchaseOrder> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");

        // 商家数据权限：按所属店铺过滤
        if (merchantUserId != null) {
            List<Long> storeIds = storeService.getStoreIdsByUserId(merchantUserId);
            if (storeIds.isEmpty()) {
                return new Page<Map<String, Object>>(page.getCurrent(), page.getSize()).setRecords(List.of());
            }
            String idsStr = storeIds.stream().map(String::valueOf).collect(Collectors.joining(","));
            List<Long> orderIds = orderQueryMapper.selectOrderIdsByStoreIds(idsStr);
            if (orderIds.isEmpty()) {
                return new Page<Map<String, Object>>(page.getCurrent(), page.getSize()).setRecords(List.of());
            }
            wrapper.in("id", orderIds);
        }

        if (param != null) {
            if (param.getOrderStatus() != null) wrapper.eq("order_status", param.getOrderStatus());
            if (param.getUserId() != null) wrapper.eq("user_id", param.getUserId());
            if (param.getOrderNo() != null && !param.getOrderNo().isBlank())
                wrapper.like("order_no", param.getOrderNo());
        }

        IPage<PurchaseOrder> result = baseService.page(new Page<>(page.getCurrent(), page.getSize()), wrapper);

        // 批量查询订单明细，避免 N+1
        List<Long> orderIds = result.getRecords().stream().map(PurchaseOrder::getId).collect(Collectors.toList());
        List<OrderItem> allItems = orderIds.isEmpty() ? List.of()
                : orderQueryMapper.selectItemsByOrderIds(orderIds.stream().map(String::valueOf).collect(Collectors.joining(",")));

        Map<Long, List<OrderItem>> itemsByOrderId = allItems.stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId));

        List<Map<String, Object>> records = result.getRecords().stream().map(order -> {
            order.setItems(itemsByOrderId.getOrDefault(order.getId(), List.of()));
            Map<String, Object> map = BeanUtil.beanToMap(order);
            map.put("id", String.valueOf(order.getId()));
            map.put("userId", String.valueOf(order.getUserId()));
            enrichAddressAndUser(map, order);
            return map;
        }).collect(Collectors.toList());

        Page<Map<String, Object>> resultPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    /**
     * 获取订单详情。
     *
     * <p>商家调用时校验订单是否属于其店铺；管理员不限制。
     * 返回结果包含订单头、明细、地址等完整信息。</p>
     *
     * @param id             订单 ID
     * @param merchantUserId 商家用户 ID（管理员传 null）
     * @return 富化后的订单详情 Map
     * @throws BusinessException 订单不存在或无权限时抛出
     */
    @Override
    public Map<String, Object> getDetail(Long id, Long merchantUserId) {
        PurchaseOrder order = baseService.getById(id);
        if (order == null) throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");

        if (merchantUserId != null) {
            List<Long> storeIds = storeService.getStoreIdsByUserId(merchantUserId);
            if (!storeIds.isEmpty()) {
                String idsStr = storeIds.stream().map(String::valueOf).collect(Collectors.joining(","));
                List<Long> orderIds = orderQueryMapper.selectOrderIdsByStoreIds(idsStr);
                if (!orderIds.contains(id)) {
                    throw new BusinessException(ErrorCode.FORBIDDEN, "无权查看该订单");
                }
            }
        }

        List<OrderItem> items = orderQueryMapper.selectItemsByOrderIds(String.valueOf(id));
        Map<String, Object> map = BeanUtil.beanToMap(order);
        map.put("id", String.valueOf(order.getId()));
        map.put("userId", String.valueOf(order.getUserId()));
        map.put("items", items);
        enrichAddressAndUser(map, order);
        return map;
    }

    /**
     * 富化地址信息和用户信息。
     *
     * <p>从 addressSnapshot JSON 中解析出完整地址、收货人、电话等字段，
     * 并补充状态码和退款原因。</p>
     */
    private void enrichAddressAndUser(Map<String, Object> map, PurchaseOrder order) {
        if (order.getAddressSnapshot() != null && JSONUtil.isTypeJSON(order.getAddressSnapshot())) {
            JSONObject addr = JSONUtil.parseObj(order.getAddressSnapshot());
            String fullAddress = String.join(" ",
                    addr.getStr("province", ""),
                    addr.getStr("city", ""),
                    addr.getStr("district", ""),
                    addr.getStr("detail", "")).trim();
            map.put("address", fullAddress);
            map.put("userName", addr.getStr("receiverName", ""));
            map.put("receiverPhone", addr.getStr("phone", ""));
        } else {
            map.put("address", "");
            map.put("userName", "");
        }
        map.put("status", order.getOrderStatus() == null ? "0" : String.valueOf(order.getOrderStatus()));
        Integer s = order.getOrderStatus();
        if (s != null && (s == -2 || s == -3 || s == -4) && order.getCancelReason() != null) {
            map.put("returnReason", order.getCancelReason());
        }
    }
}