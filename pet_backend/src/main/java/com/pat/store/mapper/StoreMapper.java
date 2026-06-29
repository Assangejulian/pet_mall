package com.pat.store.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pat.store.domain.entity.Store;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StoreMapper extends BaseMapper<Store> {

    @Select("SELECT COUNT(1) FROM product WHERE store_id = #{storeId} AND deleted = 0")
    Long countActiveProducts(@Param("storeId") Long storeId);

    @Select("SELECT COUNT(1) FROM product WHERE store_id = #{storeId} AND deleted = 0 AND status = 1")
    Long countOnlineProducts(@Param("storeId") Long storeId);
}
