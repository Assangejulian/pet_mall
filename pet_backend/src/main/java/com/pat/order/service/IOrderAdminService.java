package com.pat.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.order.domain.entity.PurchaseOrder;

import java.util.Map;

/** 管理端订单操作：列表、状态变更、退单审核 */
public interface IOrderAdminService {
    IPage<PurchaseOrder> pageList(PurchaseOrder param, Page<PurchaseOrder> page);
    void updateStatus(Long id, Map<String, Object> body);
    Map<String, Object> getDetail(Long id);
    void paySuccess(String orderNo);
}
