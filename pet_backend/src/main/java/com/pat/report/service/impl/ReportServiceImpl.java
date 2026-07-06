package com.pat.report.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.mapper.PurchaseOrderMapper;
import com.pat.order.service.base.PurchaseOrderBaseService;
import com.pat.product.domain.entity.Product;
import com.pat.product.service.IProductService;
import com.pat.report.domain.vo.OrderReportVO;
import com.pat.report.domain.vo.SalesTop10ReportVO;
import com.pat.report.domain.vo.TurnoverReportVO;
import com.pat.report.domain.vo.UserReportVO;
import com.pat.report.mapper.ReportMapper;
import com.pat.report.service.ReportService;
import com.pat.store.service.IStoreService;
import com.pat.video.service.IVideoService;
import com.pat.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final IProductService productService;
    private final IStoreService storeService;
    private final IVideoService videoService;
    private final UserService userService;
    private final PurchaseOrderBaseService orderBaseService;
    private final PurchaseOrderMapper orderMapper;
    private final ReportMapper reportMapper;

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = begin.atStartOfDay();
        LocalDateTime endTime = end.plusDays(1).atStartOfDay();

        List<Map<String, Object>> rows = reportMapper.selectDailyTurnover(beginTime, endTime);
        List<String> dateList = new ArrayList<>();
        List<String> turnoverList = new ArrayList<>();

        Map<String, BigDecimal> map = rows.stream()
                .collect(Collectors.toMap(
                        r -> r.get("date").toString(),
                        r -> (BigDecimal) r.get("turnover"),
                        (a, b) -> a
                ));

        for (LocalDate d = begin; !d.isAfter(end); d = d.plusDays(1)) {
            String dateStr = d.toString();
            dateList.add(dateStr);
            BigDecimal turnover = map.getOrDefault(dateStr, BigDecimal.ZERO);
            turnoverList.add(turnover.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        }

        return TurnoverReportVO.builder()
                .dateList(String.join(",", dateList))
                .turnoverList(String.join(",", turnoverList))
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = begin.atStartOfDay();
        LocalDateTime endTime = end.plusDays(1).atStartOfDay();

        List<Map<String, Object>> newUserRows = reportMapper.selectDailyNewUsers(beginTime, endTime);
        Long totalUserCount = reportMapper.selectTotalUserCount(endTime);

        List<String> dateList = new ArrayList<>();
        List<String> newUserList = new ArrayList<>();
        List<String> totalUserList = new ArrayList<>();

        Map<String, Long> newUserMap = newUserRows.stream()
                .collect(Collectors.toMap(
                        r -> r.get("date").toString(),
                        r -> ((Number) r.get("cnt")).longValue(),
                        (a, b) -> a
                ));

        long runningTotal = totalUserCount;
        // Iterate from end to begin for running total
        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate d = begin; !d.isAfter(end); d = d.plusDays(1)) {
            dates.add(d);
        }
        // Calculate running total backwards
        for (int i = dates.size() - 1; i >= 0; i--) {
            String dateStr = dates.get(i).toString();
            long newUsers = newUserMap.getOrDefault(dateStr, 0L);
            runningTotal -= newUsers;
        }
        // Now iterate forward
        for (LocalDate d : dates) {
            String dateStr = d.toString();
            long newUsers = newUserMap.getOrDefault(dateStr, 0L);
            runningTotal += newUsers;
            dateList.add(dateStr);
            newUserList.add(String.valueOf(newUsers));
            totalUserList.add(String.valueOf(runningTotal));
        }

        return UserReportVO.builder()
                .dateList(String.join(",", dateList))
                .newUserList(String.join(",", newUserList))
                .totalUserList(String.join(",", totalUserList))
                .build();
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = begin.atStartOfDay();
        LocalDateTime endTime = end.plusDays(1).atStartOfDay();

        List<Map<String, Object>> totalRows = reportMapper.selectDailyOrderCount(beginTime, endTime);
        List<Map<String, Object>> validRows = reportMapper.selectDailyValidOrderCount(beginTime, endTime);
        List<Map<String, Object>> completedRows = reportMapper.selectDailyCompletedOrderCount(beginTime, endTime);

        Map<String, Long> totalMap = totalRows.stream()
                .collect(Collectors.toMap(r -> r.get("date").toString(), r -> ((Number) r.get("cnt")).longValue(), (a, b) -> a));
        Map<String, Long> validMap = validRows.stream()
                .collect(Collectors.toMap(r -> r.get("date").toString(), r -> ((Number) r.get("cnt")).longValue(), (a, b) -> a));
        Map<String, Long> completedMap = completedRows.stream()
                .collect(Collectors.toMap(r -> r.get("date").toString(), r -> ((Number) r.get("cnt")).longValue(), (a, b) -> a));

        List<String> dateList = new ArrayList<>();
        List<String> orderCountList = new ArrayList<>();
        List<String> validOrderCountList = new ArrayList<>();
        List<String> completedOrderCountList = new ArrayList<>();

        for (LocalDate d = begin; !d.isAfter(end); d = d.plusDays(1)) {
            String dateStr = d.toString();
            dateList.add(dateStr);
            orderCountList.add(String.valueOf(totalMap.getOrDefault(dateStr, 0L)));
            validOrderCountList.add(String.valueOf(validMap.getOrDefault(dateStr, 0L)));
            completedOrderCountList.add(String.valueOf(completedMap.getOrDefault(dateStr, 0L)));
        }

        return OrderReportVO.builder()
                .dateList(String.join(",", dateList))
                .orderCountList(String.join(",", orderCountList))
                .validOrderCountList(String.join(",", validOrderCountList))
                .completedOrderCountList(String.join(",", completedOrderCountList))
                .build();
    }

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = begin.atStartOfDay();
        LocalDateTime endTime = end.plusDays(1).atStartOfDay();

        List<Map<String, Object>> rows = reportMapper.selectSalesTop10(beginTime, endTime, null);

        List<String> nameList = new ArrayList<>();
        List<String> numberList = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            nameList.add((String) row.get("name"));
            numberList.add(((Number) row.get("number")).toString());
        }

        return SalesTop10ReportVO.builder()
                .nameList(String.join(",", nameList))
                .numberList(String.join(",", numberList))
                .build();
    }

    @Override
    public Map<String, Object> getDashboardStats(List<Long> storeIds, boolean isAdmin, LocalDate begin, LocalDate end) {
        long productCount;
        if (storeIds == null || storeIds.isEmpty()) {
            productCount = productService.count();
        } else {
            productCount = productService.lambdaQuery().in(Product::getStoreId, storeIds).count();
        }

        long todayOrders = 0L;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        Map<String, Integer> statusCount = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        if (storeIds == null || storeIds.isEmpty()) {
            LambdaQueryWrapper<PurchaseOrder> qw = new LambdaQueryWrapper<>();
            qw.ge(PurchaseOrder::getCreateTime, today.atStartOfDay());
            todayOrders = orderBaseService.count(qw);
            totalRevenue = orderMapper.selectTotalRevenue();
            orderMapper.selectOrderStatusCount()
                    .forEach(row -> statusCount.put(String.valueOf(row.get("order_status")), ((Number) row.get("cnt")).intValue()));
        } else {
            for (Long storeId : storeIds) {
                todayOrders += reportMapper.selectMerchantTodayOrders(storeId,
                        LocalDateTime.of(today, LocalTime.MIN));
                totalRevenue = totalRevenue.add(reportMapper.selectMerchantRevenue(storeId));
                reportMapper.selectMerchantOrderStatusCount(storeId).forEach(row ->
                        statusCount.merge(String.valueOf(row.get("order_status")),
                                ((Number) row.get("cnt")).intValue(), Integer::sum));
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        // video count
        long videoCount;
        if (isAdmin) {
            videoCount = videoService.count();
        } else if (storeIds != null && !storeIds.isEmpty()) {
            videoCount = reportMapper.selectMerchantVideoCount(storeIds);
        } else {
            videoCount = 0L;
        }

        // period revenue
        if (begin == null) begin = LocalDate.now().withDayOfMonth(1);
        if (end == null) end = LocalDate.now();
        LocalDateTime beginTime = begin.atStartOfDay();
        LocalDateTime endTime = end.plusDays(1).atStartOfDay();
        BigDecimal periodRevenue;
        if (storeIds == null || storeIds.isEmpty()) {
            periodRevenue = reportMapper.selectPeriodTurnover(beginTime, endTime);
        } else {
            periodRevenue = reportMapper.selectMerchantDailyTurnover(beginTime, endTime, storeIds).stream()
                .map(r -> (BigDecimal) r.get("turnover"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        if (periodRevenue == null) periodRevenue = BigDecimal.ZERO;

        data.put("userCount", isAdmin ? userService.count() : 0);
        data.put("storeCount", storeIds != null ? (long) storeIds.size() : storeService.count());
        data.put("productCount", productCount);
        data.put("videoCount", videoCount);
        data.put("todayOrders", todayOrders);
        data.put("totalRevenue", totalRevenue);
        data.put("periodRevenue", periodRevenue);
        data.put("orderStatusCount", statusCount);
        return data;
    }

    // ===== Merchant report methods =====

    public TurnoverReportVO getMerchantTurnoverStatistics(LocalDate begin, LocalDate end, List<Long> storeIds) {
        LocalDateTime beginTime = begin.atStartOfDay();
        LocalDateTime endTime = end.plusDays(1).atStartOfDay();

        List<Map<String, Object>> rows = reportMapper.selectMerchantDailyTurnover(beginTime, endTime, storeIds);
        Map<String, BigDecimal> map = rows.stream()
                .collect(Collectors.toMap(
                        r -> r.get("date").toString(),
                        r -> (BigDecimal) r.get("turnover"),
                        (a, b) -> a
                ));

        List<String> dateList = new ArrayList<>();
        List<String> turnoverList = new ArrayList<>();
        for (LocalDate d = begin; !d.isAfter(end); d = d.plusDays(1)) {
            String dateStr = d.toString();
            dateList.add(dateStr);
            turnoverList.add(map.getOrDefault(dateStr, BigDecimal.ZERO).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        }
        return TurnoverReportVO.builder()
                .dateList(String.join(",", dateList))
                .turnoverList(String.join(",", turnoverList))
                .build();
    }

    public UserReportVO getMerchantUserStatistics(LocalDate begin, LocalDate end) {
        // 商家用户统计复用全平台数据
        return getUserStatistics(begin, end);
    }

    public OrderReportVO getMerchantOrderStatistics(LocalDate begin, LocalDate end, List<Long> storeIds) {
        LocalDateTime beginTime = begin.atStartOfDay();
        LocalDateTime endTime = end.plusDays(1).atStartOfDay();

        List<Map<String, Object>> totalRows = reportMapper.selectMerchantDailyOrderCount(beginTime, endTime, storeIds);
        List<Map<String, Object>> validRows = reportMapper.selectMerchantDailyValidOrderCount(beginTime, endTime, storeIds);
        List<Map<String, Object>> completedRows = reportMapper.selectMerchantDailyCompletedOrderCount(beginTime, endTime, storeIds);

        Map<String, Long> totalMap = toMap(totalRows);
        Map<String, Long> validMap = toMap(validRows);
        Map<String, Long> completedMap = toMap(completedRows);

        List<String> dateList = new ArrayList<>();
        List<String> orderCountList = new ArrayList<>();
        List<String> validOrderCountList = new ArrayList<>();
        List<String> completedOrderCountList = new ArrayList<>();

        for (LocalDate d = begin; !d.isAfter(end); d = d.plusDays(1)) {
            String dateStr = d.toString();
            dateList.add(dateStr);
            orderCountList.add(String.valueOf(totalMap.getOrDefault(dateStr, 0L)));
            validOrderCountList.add(String.valueOf(validMap.getOrDefault(dateStr, 0L)));
            completedOrderCountList.add(String.valueOf(completedMap.getOrDefault(dateStr, 0L)));
        }
        return OrderReportVO.builder()
                .dateList(String.join(",", dateList))
                .orderCountList(String.join(",", orderCountList))
                .validOrderCountList(String.join(",", validOrderCountList))
                .completedOrderCountList(String.join(",", completedOrderCountList))
                .build();
    }

    public SalesTop10ReportVO getMerchantSalesTop10(LocalDate begin, LocalDate end, List<Long> storeIds) {
        LocalDateTime beginTime = begin.atStartOfDay();
        LocalDateTime endTime = end.plusDays(1).atStartOfDay();

        List<Map<String, Object>> rows = reportMapper.selectMerchantSalesTop10(beginTime, endTime, storeIds);
        List<String> nameList = new ArrayList<>();
        List<String> numberList = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            nameList.add((String) row.get("name"));
            numberList.add(((Number) row.get("number")).toString());
        }
        return SalesTop10ReportVO.builder()
                .nameList(String.join(",", nameList))
                .numberList(String.join(",", numberList))
                .build();
    }

    private Map<String, Long> toMap(List<Map<String, Object>> rows) {
        return rows.stream()
                .collect(Collectors.toMap(
                        r -> r.get("date").toString(),
                        r -> ((Number) r.get("cnt")).longValue(),
                        (a, b) -> a));
    }
}
