package com.pat.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pat.product.domain.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /** 行级锁查询 — 用于下单扣库存时防超卖 */
    @Select("SELECT * FROM product WHERE id = #{id} FOR UPDATE")
    Product selectForUpdateById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM order_item WHERE product_id = #{productId}")
    long countOrderItemsByProductId(@Param("productId") Long productId);
}
