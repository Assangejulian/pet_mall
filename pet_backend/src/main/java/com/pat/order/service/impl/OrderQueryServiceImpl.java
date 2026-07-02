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
import com.pat.order.mapper.OrderItemMapper;
import com.pat.order.service.OrderQueryService;
import com.pat.order.service.base.PurchaseOrderBaseService;
import com.pat.store.service.IStoreService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderQueryServiceImpl implements OrderQueryService {

    private final PurchaseOrderBaseService baseService;
    private final OrderItemMapper orderItemMapper;
    private final IStoreService storeService;

    public OrderQueryServiceImpl(PurchaseOrderBaseService baseService,
                                 OrderItemMapper orderItemMapper,
                                 IStoreService storeService) {
        this.baseService = baseService;
        this.orderItemMapper = orderItemMapper;
        this.storeService = storeService;
    }

    @Override
    public IPage<Map<String, Object>> pageList(PurchaseOrder param, Page<?> page, Long merchantUserId) {
        QueryWrapper<PurchaseOrder> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");

        if (merchantUserId != null) {
            List<Long> storeIds = storeService.getStoreIdsByUserId(merchantUserId);
            if (storeIds.isEmpty()) {
                return new Page<Map<String, Object>>(page.getCurrent(), page.getSize()).setRecords(List.of());
            }
            String idsStr = storeIds.stream().map(String::valueOf).collect(Collectors.joining(","));
            List<Long> orderIds = orderItemMapper.selectOrderIdsByStoreIds(idsStr);
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

        List<Map<String, Object>> records = result.getRecords().stream().map(order -> {
            List<OrderItem> items = orderItemMapper.selectList(
                    new QueryWrapper<OrderItem>().eq("order_id", order.getId()));
            order.setItems(items);
            Map<String, Object> map = BeanUtil.beanToMap(order);
            enrichAddressAndUser(map, order);
            return map;
        }).collect(Collectors.toList());

        Page<Map<String, Object>> resultPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        resultPage.setRecords(records);
        return resultPage;
    }

    @Override
    public Map<String, Object> getDetail(Long id, Long merchantUserId) {
        PurchaseOrder order = baseService.getById(id);
        if (order == null) throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");

        if (merchantUserId != null) {
            List<Long> storeIds = storeService.getStoreIdsByUserId(merchantUserId);
            if (!storeIds.isEmpty()) {
                String idsStr = storeIds.stream().map(String::valueOf).collect(Collectors.joining(","));
                List<Long> orderIds = orderItemMapper.selectOrderIdsByStoreIds(idsStr);
                if (!orderIds.contains(id)) {
                    throw new BusinessException(ErrorCode.FORBIDDEN, "无权查看该订单");
                }
            }
        }

        List<OrderItem> items = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", id));
        Map<String, Object> map = BeanUtil.beanToMap(order);
        map.put("items", items);
        enrichAddressAndUser(map, order);
        return map;
    }

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
