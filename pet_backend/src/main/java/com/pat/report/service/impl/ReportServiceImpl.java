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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        // 查询每日营业额
        List<Map<String, Object>> list = reportMapper.selectDailyTurnover(beginTime, endTime);

        // 转成 Map：date -> turnover
        Map<String, Object> map = list.stream()
                .collect(Collectors.toMap(m -> m.get("date").toString(), m -> m.get("turnover")));

        // 遍历日期，无数据补 0
        List<String> dateList = new ArrayList<>();
        List<String> turnoverList = new ArrayList<>();
        for (LocalDate cur = begin; !cur.isAfter(end); cur = cur.plusDays(1)) {
            String ds = cur.toString();
            dateList.add(ds);
            turnoverList.add(map.getOrDefault(ds, BigDecimal.ZERO).toString());
        }

        return TurnoverReportVO.builder()
                .dateList(String.join(",", dateList))
                .turnoverList(String.join(",", turnoverList))
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        // 每日新增用户
        List<Map<String, Object>> newUsers = reportMapper.selectDailyNewUsers(beginTime, endTime);
        Map<String, Object> newMap = newUsers.stream()
                .collect(Collectors.toMap(m -> m.get("date").toString(), m -> m.get("cnt")));

        // 截止到 begin 前的总用户数
        long total = reportMapper.selectTotalUserCount(beginTime);

        // 遍历日期
        List<String> dateList = new ArrayList<>();
        List<String> newList = new ArrayList<>();
        List<String> totalList = new ArrayList<>();
        for (LocalDate cur = begin; !cur.isAfter(end); cur = cur.plusDays(1)) {
            String ds = cur.toString();
            dateList.add(ds);

            long daily = Long.parseLong(newMap.getOrDefault(ds, 0L).toString());
            total += daily;

            newList.add(String.valueOf(daily));
            totalList.add(String.valueOf(total));
        }

        return UserReportVO.builder()
                .dateList(String.join(",", dateList))
                .newUserList(String.join(",", newList))
                .totalUserList(String.join(",", totalList))
                .build();
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        // 分别查询三类订单数
        Map<String, Long> totalMap = toCountMap(reportMapper.selectDailyOrderCount(beginTime, endTime));
        Map<String, Long> validMap = toCountMap(reportMapper.selectDailyValidOrderCount(beginTime, endTime));
        Map<String, Long> completedMap = toCountMap(reportMapper.selectDailyCompletedOrderCount(beginTime, endTime));

        List<String> dateList = new ArrayList<>();
        List<String> totalCount = new ArrayList<>();
        List<String> validCount = new ArrayList<>();
        List<String> completedCount = new ArrayList<>();
        for (LocalDate cur = begin; !cur.isAfter(end); cur = cur.plusDays(1)) {
            String ds = cur.toString();
            dateList.add(ds);
            totalCount.add(String.valueOf(totalMap.getOrDefault(ds, 0L)));
            validCount.add(String.valueOf(validMap.getOrDefault(ds, 0L)));
            completedCount.add(String.valueOf(completedMap.getOrDefault(ds, 0L)));
        }

        return OrderReportVO.builder()
                .dateList(String.join(",", dateList))
                .orderCountList(String.join(",", totalCount))
                .validOrderCountList(String.join(",", validCount))
                .completedOrderCountList(String.join(",", completedCount))
                .build();
    }

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        // 查询销量排行
        List<Map<String, Object>> list = reportMapper.selectSalesTop10(beginTime, endTime);

        List<String> nameList = list.stream()
                .map(m -> m.get("name").toString())
                .collect(Collectors.toList());
        List<String> numberList = list.stream()
                .map(m -> m.get("number").toString())
                .collect(Collectors.toList());

        return SalesTop10ReportVO.builder()
                .nameList(String.join(",", nameList))
                .numberList(String.join(",", numberList))
                .build();
    }

    /** 把 List<Map> 转成 Map<date, count> */
    private Map<String, Long> toCountMap(List<Map<String, Object>> list) {
        return list.stream().collect(Collectors.toMap(
                m -> m.get("date").toString(),
                m -> ((Number) m.get("cnt")).longValue()
        ));
    }
}