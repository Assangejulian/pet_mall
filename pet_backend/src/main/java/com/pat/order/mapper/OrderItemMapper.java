package com.pat.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pat.order.domain.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /** 查询包含指定店铺商品的订单ID列表（去重） */
    @Select("SELECT DISTINCT oi.order_id FROM order_item oi INNER JOIN product p ON oi.product_id = p.id WHERE p.store_id IN (${storeIds})")
    List<Long> selectOrderIdsByStoreIds(@Param("storeIds") String storeIds);
}

