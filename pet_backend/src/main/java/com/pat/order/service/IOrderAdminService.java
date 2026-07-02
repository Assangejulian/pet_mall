package com.pat.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.order.domain.dto.OrderCancelDTO;
import com.pat.order.domain.dto.OrderRefundDTO;
import com.pat.order.domain.dto.OrderShipDTO;
import com.pat.order.domain.entity.PurchaseOrder;

import java.util.Map;

/**
 * 管理端订单操作：列表、发货、取消、退款审核
 */
public interface IOrderAdminService {
    IPage<PurchaseOrder> pageList(PurchaseOrder param, Page<PurchaseOrder> page);

    Map<String, Object> getDetail(Long id);

    /** 发货 */
    void shipOrder(OrderShipDTO dto);

    /** 取消订单 */
    void cancelOrder(OrderCancelDTO dto);

    /** 退款审核：approved=true → 退款通过(-3), false → 驳回回到已收货(3) */
    void refundApprove(OrderRefundDTO dto);

    /** 直接退款（不经过退款申请流程） */
    void refundDirect(OrderCancelDTO dto);

    /** 支付成功回调 */
    void paySuccess(String orderNo);
}
