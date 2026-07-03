package com.pat.order.mapper;

import com.pat.order.domain.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单多表查询 —— 关联 order + order_item + product
 */
@Mapper
public interface OrderQueryMapper {

    /** 批量查询多个订单的订单项 */
    @Select("SELECT * FROM order_item WHERE order_id IN (${orderIds})")
    List<OrderItem> selectItemsByOrderIds(@Param("orderIds") String orderIds);

    /** 查询包含指定店铺商品的订单ID列表（去重） */
    @Select("SELECT DISTINCT oi.order_id FROM order_item oi " +
            "INNER JOIN product p ON oi.product_id = p.id " +
            "WHERE p.store_id IN (${storeIds})")
    List<Long> selectOrderIdsByStoreIds(@Param("storeIds") String storeIds);

    @Select("SELECT oi.evaluate_content as content, oi.evaluate_time as createTime, po.user_id as userId " +
            "FROM order_item oi " +
            "JOIN purchase_order po ON oi.order_id = po.id " +
            "WHERE oi.product_id = #{productId} AND oi.evaluate_content IS NOT NULL " +
            "ORDER BY oi.evaluate_time DESC")
    List<java.util.Map<String, Object>> selectProductReviews(@Param("productId") Long productId);
}