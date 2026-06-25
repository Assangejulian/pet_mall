package com.pat.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pat.order.domain.entity.PurchaseOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface PurchaseOrderMapper extends BaseMapper<PurchaseOrder> {

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM purchase_order WHERE order_status IN (1, 2, 3, 4)")
    BigDecimal selectTotalRevenue();

    @Select("SELECT order_status, COUNT(*) AS cnt FROM purchase_order GROUP BY order_status")
    List<Map<String, Object>> selectOrderStatusCount();
}