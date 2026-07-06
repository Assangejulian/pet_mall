package com.pat.report;

import com.pat.order.mapper.PurchaseOrderMapper;
import com.pat.order.service.base.PurchaseOrderBaseService;
import com.pat.product.service.IProductService;
import com.pat.report.domain.vo.UserReportVO;
import com.pat.report.mapper.ReportMapper;
import com.pat.report.service.impl.ReportServiceImpl;
import com.pat.store.service.IStoreService;
import com.pat.user.service.UserService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MerchantReportIsolationTest {

    private final IProductService products = mock(IProductService.class);
    private final IStoreService stores = mock(IStoreService.class);
    private final UserService users = mock(UserService.class);
    private final PurchaseOrderBaseService orders = mock(PurchaseOrderBaseService.class);
    private final PurchaseOrderMapper orderMapper = mock(PurchaseOrderMapper.class);
    private final ReportMapper reports = mock(ReportMapper.class);
    private final ReportServiceImpl service = new ReportServiceImpl(
            products, stores, users, orders, orderMapper, reports);

    @Test
    void merchantWithoutStoresNeverFallsBackToPlatformDashboardData() {
        Map<String, Object> stats = service.getDashboardStats(List.of(), false);

        assertThat(stats).containsEntry("storeCount", 0L)
                .containsEntry("productCount", 0L)
                .containsEntry("todayOrders", 0L);
        verify(products, never()).count();
        verify(orders, never()).count(any());
        verify(orderMapper, never()).selectTotalRevenue();
    }

    @Test
    void merchantWithoutStoresReceivesZeroUserSeriesWithoutGlobalQueries() {
        LocalDate day = LocalDate.of(2026, 7, 6);
        UserReportVO report = service.getMerchantUserStatistics(day, day, List.of());

        assertThat(report.getNewUserList()).isEqualTo("0");
        assertThat(report.getTotalUserList()).isEqualTo("0");
        verify(reports, never()).selectDailyNewUsers(any(), any());
        verify(reports, never()).selectTotalUserCount(any());
        verify(reports, never()).selectMerchantDailyNewUsers(any(), any(), any());
    }

    @Test
    void merchantUserSeriesUsesOnlyOwnedStoreIds() {
        LocalDate day = LocalDate.of(2026, 7, 6);
        List<Long> storeIds = List.of(11L, 12L);
        when(reports.selectMerchantDailyNewUsers(any(), any(), eq(storeIds)))
                .thenReturn(List.of(Map.of("date", "2026-07-06", "cnt", 2L)));
        when(reports.selectMerchantTotalUserCount(any(), eq(storeIds))).thenReturn(5L);

        UserReportVO report = service.getMerchantUserStatistics(day, day, storeIds);

        assertThat(report.getNewUserList()).isEqualTo("2");
        assertThat(report.getTotalUserList()).isEqualTo("5");
        verify(reports).selectMerchantDailyNewUsers(any(), any(), eq(storeIds));
        verify(reports).selectMerchantTotalUserCount(any(), eq(storeIds));
    }
}
