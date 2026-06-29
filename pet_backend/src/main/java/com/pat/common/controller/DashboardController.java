package com.pat.common.controller;

import com.pat.common.domain.Result;
import com.pat.common.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@Tag(name = "首页概览")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(summary = "获取概览统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userCount", dashboardService.getUserCount());
        data.put("storeCount", dashboardService.getStoreCount());
        data.put("productCount", dashboardService.getProductCount());
        data.put("todayOrders", dashboardService.getTodayOrderCount());
        data.put("totalRevenue", dashboardService.getTotalRevenue());
        data.put("orderStatusCount", dashboardService.getOrderStatusDistribution());
        return Result.success(data);
    }
}
