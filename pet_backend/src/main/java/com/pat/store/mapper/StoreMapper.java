package com.pat.store.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pat.product.domain.entity.Product;
import com.pat.store.domain.entity.Store;
import com.pat.store.domain.vo.NearbyStoreRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StoreMapper extends BaseMapper<Store> {

    @Select("SELECT COUNT(1) FROM product WHERE store_id = #{storeId} AND deleted = 0")
    Long countActiveProducts(@Param("storeId") Long storeId);

    @Select("SELECT COUNT(1) FROM product WHERE store_id = #{storeId} AND deleted = 0 AND status = 1")
    Long countOnlineProducts(@Param("storeId") Long storeId);

    @Select("SELECT s.id AS id, s.user_id AS userId, s.store_name AS storeName, " +
            "s.store_logo AS storeLogo, s.store_phone AS storePhone, s.store_desc AS storeDesc, " +
            "s.province AS province, s.city AS city, s.district AS district, s.address AS address, " +
            "s.longitude AS longitude, s.latitude AS latitude, s.status AS status, " +
            "s.create_time AS createTime, s.update_time AS updateTime " +
            "FROM store s WHERE s.status = 1 AND s.deleted = 0 " +
            "AND s.longitude IS NOT NULL AND s.latitude IS NOT NULL " +
            "ORDER BY s.create_time DESC")
    List<NearbyStoreRow> selectAllActiveStores();

    @Select("SELECT p.id, p.store_id, p.product_name, p.product_type, p.category, p.product_desc, p.price, " +
            "p.stock, p.main_image, p.images, p.status, p.video_id, p.create_time, p.update_time " +
            "FROM product p WHERE p.store_id = #{storeId} AND p.deleted = 0 AND p.status = 1 " +
            "ORDER BY p.create_time DESC, p.id DESC")
    List<Product> selectStoreProducts(@Param("storeId") Long storeId);
}
