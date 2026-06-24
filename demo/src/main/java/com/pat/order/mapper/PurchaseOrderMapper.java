package com.pat.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pat.order.entity.PurchaseOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PurchaseOrderMapper extends BaseMapper<PurchaseOrder> {
}