package com.pat.store.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pat.product.domain.entity.Product;
import com.pat.store.domain.entity.Store;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface StoreMapper extends BaseMapper<Store> {

    @Select("SELECT COUNT(1) FROM product WHERE store_id = #{storeId} AND deleted = 0")
    Long countActiveProducts(@Param("storeId") Long storeId);

    @Select("SELECT COUNT(1) FROM product WHERE store_id = #{storeId} AND deleted = 0 AND status = 1")
    Long countOnlineProducts(@Param("storeId") Long storeId);

    /** 附近门店搜索 (Haversine) */
    @Select("SELECT *, " +
            "(6371 * acos(cos(radians(#{lat})) * cos(radians(latitude)) " +
            "* cos(radians(longitude) - radians(#{lng})) + sin(radians(#{lat})) * sin(radians(latitude)))) AS distance " +
            "FROM store " +
            "WHERE status = 1 AND deleted = 0 " +
            "HAVING distance <= #{radius} " +
            "ORDER BY distance")
    List<Store> searchNearby(@Param("lat") BigDecimal lat, @Param("lng") BigDecimal lng, @Param("radius") Double radius);

    /** 查询门店上架商品列表 */
    @Select("SELECT p.* FROM product p WHERE p.store_id = #{storeId} AND p.deleted = 0 AND p.status = 1 ORDER BY p.create_time DESC")
    List<Product> selectStoreProducts(@Param("storeId") Long storeId);
}
