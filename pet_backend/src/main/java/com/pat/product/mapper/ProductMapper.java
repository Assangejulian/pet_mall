package com.pat.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pat.product.domain.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}