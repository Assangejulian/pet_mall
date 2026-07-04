package com.pat.report.service.impl;

import com.pat.report.domain.vo.OrderReportVO;
import com.pat.report.domain.vo.SalesTop10ReportVO;
import com.pat.report.domain.vo.TurnoverReportVO;
import com.pat.report.domain.vo.UserReportVO;
import com.pat.report.mapper.ReportMapper;
import com.pat.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;

    /**
     * 营业额统计
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        // 查询数据库
        List<Map<String, Object>> list = reportMapper.selectDailyTurnover(beginTime, endTime);

        // 填充日期范围内每一天的数据，无数据的日期补 0
        List<String> dateList = new ArrayList<>();
        List<String> turnoverList = new ArrayList<>();

        Map<String, Object> dataMap = list.stream()
                .collect(Collectors.toMap(
                        m -> m.get("date").toString(),
                        m -> m.get("turnover").toString()
                ));

        LocalDate current = begin;
        while (!current.isAfter(end)) {
            String dateStr = current.toString();
            dateList.add(dateStr);
            turnoverList.add(dataMap.getOrDefault(dateStr, "0"));
            current = current.plusDays(1);
        }

        return TurnoverReportVO.builder()
                .dateList(String.join(",", dateList))
                .turnoverList(String.join(",", turnoverList))
                .build();
    }

    /**
     * 用户统计
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        // 查询每日新增用户
        List<Map<String, Object>> newUserList = reportMapper.selectDailyNewUsers(beginTime, endTime);

        // 获取截止到 begin 前一天的总用户数作为基数
        long totalUser = reportMapper.selectTotalUserCount(beginTime);

        // 构建每日数据
        List<String> dateList = new ArrayList<>();
        List<String> newUserStrList = new ArrayList<>();
        List<String> totalUserStrList = new ArrayList<>();

        Map<String, Object> newUserMap = newUserList.stream()
                .collect(Collectors.toMap(
                        m -> m.get("date").toString(),
                        m -> ((Number) m.get("cnt")).longValue()
                ));

        LocalDate current = begin;
        while (!current.isAfter(end)) {
            String dateStr = current.toString();
            dateList.add(dateStr);

            long dailyNew = newUserMap.getOrDefault(dateStr, 0L);
            totalUser += dailyNew;

            newUserStrList.add(String.valueOf(dailyNew));
            totalUserStrList.add(String.valueOf(totalUser));

            current = current.plusDays(1);
        }

        return UserReportVO.builder()
                .dateList(String.join(",", dateList))
                .newUserList(String.join(",", newUserStrList))
                .totalUserList(String.join(",", totalUserStrList))
                .build();
    }

    /**
     * 订单统计
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        // 查询各类订单数据
        List<Map<String, Object>> totalOrders = reportMapper.selectDailyOrderCount(beginTime, endTime);
        List<Map<String, Object>> validOrders = reportMapper.selectDailyValidOrderCount(beginTime, endTime);
        List<Map<String, Object>> completedOrders = reportMapper.selectDailyCompletedOrderCount(beginTime, endTime);

        // 转 Map
        Map<String, Object> totalMap = totalOrders.stream()
                .collect(Collectors.toMap(m -> m.get("date").toString(), m -> ((Number) m.get("cnt")).longValue()));
        Map<String, Object> validMap = validOrders.stream()
                .collect(Collectors.toMap(m -> m.get("date").toString(), m -> ((Number) m.get("cnt")).longValue()));
        Map<String, Object> completedMap = completedOrders.stream()
                .collect(Collectors.toMap(m -> m.get("date").toString(), m -> ((Number) m.get("cnt")).longValue()));

        List<String> dateList = new ArrayList<>();
        List<String> totalCountList = new ArrayList<>();
        List<String> validCountList = new ArrayList<>();
        List<String> completedCountList = new ArrayList<>();

        LocalDate current = begin;
        while (!current.isAfter(end)) {
            String dateStr = current.toString();
            dateList.add(dateStr);
            totalCountList.add(String.valueOf(totalMap.getOrDefault(dateStr, 0L)));
            validCountList.add(String.valueOf(validMap.getOrDefault(dateStr, 0L)));
            completedCountList.add(String.valueOf(completedMap.getOrDefault(dateStr, 0L)));
            current = current.plusDays(1);
        }

        return OrderReportVO.builder()
                .dateList(String.join(",", dateList))
                .orderCountList(String.join(",", totalCountList))
                .validOrderCountList(String.join(",", validCountList))
                .completedOrderCountList(String.join(",", completedCountList))
                .build();
    }

    /**
     * 销量排名 Top10
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<Map<String, Object>> list = reportMapper.selectSalesTop10(beginTime, endTime);

        List<String> nameList = list.stream()
                .map(m -> m.get("name").toString())
                .collect(Collectors.toList());
        List<String> numberList = list.stream()
                .map(m -> ((Number) m.get("number")).longValue())
                .map(String::valueOf)
                .collect(Collectors.toList());

        return SalesTop10ReportVO.builder()
                .nameList(String.join(",", nameList))
                .numberList(String.join(",", numberList))
                .build();
    }

}
