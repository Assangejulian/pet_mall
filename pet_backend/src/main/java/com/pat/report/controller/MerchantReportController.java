package com.pat.report.controller;

import com.pat.common.domain.Result;
import com.pat.common.util.UserHolder;
import com.pat.report.domain.vo.OrderReportVO;
import com.pat.report.domain.vo.SalesTop10ReportVO;
import com.pat.report.domain.vo.TurnoverReportVO;
import com.pat.report.domain.vo.UserReportVO;
import com.pat.report.service.ReportService;
import com.pat.store.service.IStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant/report")
@Tag(name = "商家首页概览")
public class MerchantReportController {

    private final ReportService reportService;
    private final IStoreService storeService;

    public MerchantReportController(ReportService reportService, IStoreService storeService) {
        this.reportService = reportService;
        this.storeService = storeService;
    }

    @Operation(summary = "获取商家概览统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Long userId = UserHolder.getUserId();
        List<Long> storeIds = storeService.getStoreIdsByUserId(userId);
        return Result.success(reportService.getDashboardStats(storeIds, false));
    }

    @Operation(summary = "商家营业额统计")
    @GetMapping("/turnover")
    public Result<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        Long userId = UserHolder.getUserId();
        List<Long> storeIds = storeService.getStoreIdsByUserId(userId);
        return Result.success(reportService.getMerchantTurnoverStatistics(begin, end, storeIds));
    }

    @Operation(summary = "商家用户统计")
    @GetMapping("/users")
    public Result<UserReportVO> userStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        Long userId = UserHolder.getUserId();
        List<Long> storeIds = storeService.getStoreIdsByUserId(userId);
        return Result.success(reportService.getMerchantUserStatistics(begin, end, storeIds));
    }

    @Operation(summary = "商家订单统计")
    @GetMapping("/orders")
    public Result<OrderReportVO> orderStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        Long userId = UserHolder.getUserId();
        List<Long> storeIds = storeService.getStoreIdsByUserId(userId);
        return Result.success(reportService.getMerchantOrderStatistics(begin, end, storeIds));
    }

    @Operation(summary = "商家销量排名 Top10")
    @GetMapping("/top10")
    public Result<SalesTop10ReportVO> top10(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        Long userId = UserHolder.getUserId();
        List<Long> storeIds = storeService.getStoreIdsByUserId(userId);
        return Result.success(reportService.getMerchantSalesTop10(begin, end, storeIds));
    }
}
