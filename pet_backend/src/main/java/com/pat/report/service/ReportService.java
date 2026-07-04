package com.pat.report.service;

import com.pat.report.domain.vo.OrderReportVO;
import com.pat.report.domain.vo.SalesTop10ReportVO;
import com.pat.report.domain.vo.TurnoverReportVO;
import com.pat.report.domain.vo.UserReportVO;

import java.time.LocalDate;

/**
 * 统计报表 Service 接口
 */
public interface ReportService {

    /**
     * 营业额统计
     */
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

    /**
     * 用户统计
     */
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

    /**
     * 订单统计
     */
    OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end);

    /**
     * 销量排名 Top10
     */
    SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end);

}
