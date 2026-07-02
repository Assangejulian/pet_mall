package com.pat.order.service;

import com.pat.order.domain.dto.OrderCancelDTO;
import com.pat.order.domain.dto.OrderRefundDTO;

/**
 * 管理端特有订单操作（取消、退款审核、支付回调等）
 * <p>通用操作（列表、详情、发货）请使用 {@link IOrderService}</p>
 */
public interface IOrderAdminService {

    /** 取消订单 */
    void cancelOrder(OrderCancelDTO dto);

    /** 退款审核：approved=true → 退款通过(-3), false → 驳回回到已收货(3) */
    void refundApprove(OrderRefundDTO dto);

    /** 直接退款（不经过退款申请流程） */
    void refundDirect(OrderCancelDTO dto);

    /** 支付成功回调 */
    void paySuccess(String orderNo);
}
