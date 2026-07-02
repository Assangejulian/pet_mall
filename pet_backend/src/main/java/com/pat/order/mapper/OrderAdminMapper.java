package com.pat.order.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 订单管理端多表操作 —— 涉及 order + order_item + product
 */
@Mapper
public interface OrderAdminMapper {

    /** 支付成功后，将订单中所有在售商品标记为已售出 */
    @Update("UPDATE product p " +
            "INNER JOIN order_item oi ON p.id = oi.product_id " +
            "SET p.status = 2 " +
            "WHERE oi.order_id = #{orderId} AND p.status = 1")
    int batchMarkProductsAsSold(@Param("orderId") Long orderId);
}
