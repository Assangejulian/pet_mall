package com.pat.order.service;

import com.pat.order.domain.dto.OrderShipDTO;

/**
 * 发货服务：商户/管理员共用，merchantUserId 控制数据范围。
 */
public interface IOrderShipService {

    /** 发货（商户校验店铺归属，管理员不限制） */
    void shipOrder(OrderShipDTO dto, Long merchantUserId);
}
