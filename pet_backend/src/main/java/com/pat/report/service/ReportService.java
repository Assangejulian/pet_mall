package com.pat.report.service;

import com.pat.report.domain.vo.OrderReportVO;
import com.pat.report.domain.vo.SalesTop10ReportVO;
import com.pat.report.domain.vo.TurnoverReportVO;
import com.pat.report.domain.vo.UserReportVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ReportService {

    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

    OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end);

    SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end);

    Map<String, Object> getDashboardStats(List<Long> storeIds, boolean isAdmin);

    // ===== Merchant report methods =====

    TurnoverReportVO getMerchantTurnoverStatistics(LocalDate begin, LocalDate end, List<Long> storeIds);

    UserReportVO getMerchantUserStatistics(LocalDate begin, LocalDate end);

    OrderReportVO getMerchantOrderStatistics(LocalDate begin, LocalDate end, List<Long> storeIds);

    SalesTop10ReportVO getMerchantSalesTop10(LocalDate begin, LocalDate end, List<Long> storeIds);
}
