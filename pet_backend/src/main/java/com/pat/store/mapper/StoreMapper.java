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

    @Select("SELECT p.id, p.store_id, p.product_name, p.product_type, p.category, p.product_desc, p.price, " +
            "p.stock, p.main_image, p.images, p.status, p.video_id, p.create_time, p.update_time " +
            "FROM product p WHERE p.store_id = #{storeId} AND p.deleted = 0 AND p.status = 1 " +
            "ORDER BY p.create_time DESC, p.id DESC")
    List<Product> selectStoreProducts(@Param("storeId") Long storeId);
    /** 附近门店 Haversine 距离计算 + 过滤 + 排序 + 分页 */
    @Select("SELECT s.id, s.user_id AS userId, s.store_name AS storeName, " +
            "s.store_logo AS storeLogo, s.store_phone AS storePhone, s.store_desc AS storeDesc, " +
            "s.province, s.city, s.district, s.address, " +
            "s.longitude, s.latitude, s.status, s.create_time AS createTime, s.update_time AS updateTime, " +
            "(6371.0088 * ACOS(LEAST(1, GREATEST(-1, COS(RADIANS(#{lat})) * COS(RADIANS(s.latitude)) " +
            "  * COS(RADIANS(s.longitude) - RADIANS(#{lng})) + SIN(RADIANS(#{lat})) * SIN(RADIANS(s.latitude)))))) AS distanceKm " +
            "FROM store s " +
            "WHERE s.status = 1 AND s.deleted = 0 AND s.longitude IS NOT NULL AND s.latitude IS NOT NULL " +
            "AND (s.store_name LIKE CONCAT('%', #{keyword}, '%') OR #{keyword} IS NULL) " +
            "AND (s.city = #{city} OR #{city} IS NULL) " +
            "HAVING distanceKm <= #{radiusKm} " +
            "ORDER BY distanceKm, s.id " +
            "LIMIT #{offset}, #{limit}")
    List<NearbyStoreRow> searchNearbyPage(@Param("lat") Double lat, @Param("lng") Double lng,
                                          @Param("radiusKm") Double radiusKm,
                                          @Param("keyword") String keyword, @Param("city") String city,
                                          @Param("offset") Long offset, @Param("limit") Long limit);

    /** 附近门店总数 */
    @Select("SELECT COUNT(*) FROM (SELECT s.id, " +
            "(6371.0088 * ACOS(LEAST(1, GREATEST(-1, COS(RADIANS(#{lat})) * COS(RADIANS(s.latitude)) " +
            "  * COS(RADIANS(s.longitude) - RADIANS(#{lng})) + SIN(RADIANS(#{lat})) * SIN(RADIANS(s.latitude)))))) AS d " +
            "FROM store s " +
            "WHERE s.status = 1 AND s.deleted = 0 AND s.longitude IS NOT NULL AND s.latitude IS NOT NULL " +
            "AND (s.store_name LIKE CONCAT('%', #{keyword}, '%') OR #{keyword} IS NULL) " +
            "AND (s.city = #{city} OR #{city} IS NULL) " +
            "HAVING d <= #{radiusKm}) t")
    Long countNearby(@Param("lat") Double lat, @Param("lng") Double lng,
                     @Param("radiusKm") Double radiusKm,
                     @Param("keyword") String keyword, @Param("city") String city);
}
