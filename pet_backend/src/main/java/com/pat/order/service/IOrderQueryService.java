package com.pat.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.order.domain.entity.PurchaseOrder;

import java.util.Map;

/**
 * 订单查询服务：商户/管理员共用，merchantUserId 控制数据范围。
 */
public interface IOrderQueryService {

    /** 分页查询订单 */
    IPage<Map<String, Object>> pageList(PurchaseOrder param, Page<?> page, Long merchantUserId);

    /** 订单详情 */
    Map<String, Object> getDetail(Long id, Long merchantUserId);
}
