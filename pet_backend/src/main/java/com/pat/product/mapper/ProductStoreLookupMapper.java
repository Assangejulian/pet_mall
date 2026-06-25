package com.pat.product.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.pat.store.domain.entity.Store;

@Mapper
public interface ProductStoreLookupMapper {

    @Select("SELECT COUNT(1) FROM store WHERE id = #{storeId} AND deleted = 0")
    Integer existsUndeletedStore(@Param("storeId") Long storeId);

    @Select("SELECT COUNT(1) FROM store WHERE id = #{storeId} AND deleted = 0 AND status = 1")
    Integer existsOperatingStore(@Param("storeId") Long storeId);

    @Select("SELECT id, user_id, store_name, store_logo, store_phone, province, city, district, address, status FROM store WHERE id = #{storeId} AND deleted = 0")
    Store selectUndeletedStore(@Param("storeId") Long storeId);

}
