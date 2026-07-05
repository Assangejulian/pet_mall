package com.pat.common.controller;

import com.pat.common.domain.Result;
import com.pat.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@Tag(name = "首页概览")
public class DashboardController {

    private final ReportService reportService;

    public DashboardController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(summary = "获取概览统计")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.success(reportService.getDashboardStats(null, true));
    }
}
