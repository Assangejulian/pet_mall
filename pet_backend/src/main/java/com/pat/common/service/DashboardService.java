package com.pat.common.service;

import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.mapper.PurchaseOrderMapper;
import com.pat.order.service.base.PurchaseOrderBaseService;
import com.pat.product.service.ProductService;
import com.pat.store.service.IStoreService;
import com.pat.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserService userService;
    private final IStoreService storeService;
    private final ProductService productService;
    private final PurchaseOrderBaseService orderService;
    private final PurchaseOrderMapper orderMapper;

    public long getUserCount() {
        return userService.count();
    }

    public long getStoreCount() {
        return storeService.count();
    }

    public long getProductCount() {
        return productService.count();
    }

    public long getTodayOrderCount() {
        return orderService.lambdaQuery()
                .ge(PurchaseOrder::getCreateTime, LocalDate.now().atStartOfDay())
                .count();
    }

    public BigDecimal getTotalRevenue() {
        BigDecimal revenue = orderMapper.selectTotalRevenue();
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    public Map<String, Integer> getOrderStatusDistribution() {
        List<Map<String, Object>> rows = orderMapper.selectOrderStatusCount();
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            map.put(String.valueOf(row.get("order_status")), ((Number) row.get("cnt")).intValue());
        }
        return map;
    }
}