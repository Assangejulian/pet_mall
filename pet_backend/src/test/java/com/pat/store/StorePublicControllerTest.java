package com.pat.store;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.common.exception.BusinessException;
import com.pat.common.domain.Result;
import com.pat.store.controller.StorePublicController;
import com.pat.store.domain.dto.StoreDTO;
import com.pat.store.domain.entity.Store;
import com.pat.store.domain.vo.StoreVO;
import com.pat.store.service.IStoreService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StorePublicControllerTest {

    private final IStoreService storeService = mock(IStoreService.class);
    private final StorePublicController controller = new StorePublicController(storeService);

    @Test
    void nearbyRejectsMissingAndInvalidCoordinatesAndRadius() {
        StoreDTO missingLongitude = nearbyParam(null, "24.48", "10");
        assertThatThrownBy(() -> controller.nearby(missingLongitude, new Page<>(1, 10)))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getDescription()).contains("经纬度不能为空"));

        StoreDTO missingLatitude = nearbyParam("118.08", null, "10");
        assertThatThrownBy(() -> controller.nearby(missingLatitude, new Page<>(1, 10)))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getDescription()).contains("经纬度不能为空"));

        StoreDTO invalidLongitude = nearbyParam("181", "24.48", "10");
        assertThatThrownBy(() -> controller.nearby(invalidLongitude, new Page<>(1, 10)))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getDescription()).contains("经度范围"));

        StoreDTO invalidLatitude = nearbyParam("118.08", "91", "10");
        assertThatThrownBy(() -> controller.nearby(invalidLatitude, new Page<>(1, 10)))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getDescription()).contains("纬度范围"));

        StoreDTO invalidRadius = nearbyParam("118.08", "24.48", "0");
        assertThatThrownBy(() -> controller.nearby(invalidRadius, new Page<>(1, 10)))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getDescription()).contains("半径"));

        StoreDTO tooLargeRadius = nearbyParam("118.08", "24.48", "100.01");
        assertThatThrownBy(() -> controller.nearby(tooLargeRadius, new Page<>(1, 10)))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getDescription()).contains("100公里"));
    }

    @Test
    void nearbyUsesDefaultRadiusWhenRadiusIsMissing() {
        Store nearStore = store(1L, "118.0801", "24.4801");
        Store aboutElevenKmAway = store(2L, "118.08", "24.58");
        when(storeService.list(any(QueryWrapper.class))).thenReturn(List.of(aboutElevenKmAway, nearStore));
        when(storeService.countActiveProducts(any())).thenReturn(0L);

        Result<?> response = controller.nearby(nearbyParam("118.08", "24.48", null), new Page<>(1, 10));

        @SuppressWarnings("unchecked")
        Page<StoreVO> page = (Page<StoreVO>) response.getData();
        assertThat(page.getTotal()).isEqualTo(1);
        assertThat(page.getRecords()).extracting(StoreVO::getId).containsExactly(1L);
    }

    @Test
    void nearbySortsByDistanceThenIdAndFiltersOutsideRadius() {
        Store nearStore = store(1L, "118.0801", "24.4801");
        Store farStore = store(2L, "119.30", "25.40");
        Store middleStore = store(3L, "118.10", "24.50");
        Store sameLocationHigherId = store(4L, "118.0801", "24.4801");
        when(storeService.list(any(QueryWrapper.class))).thenReturn(List.of(farStore, middleStore, sameLocationHigherId, nearStore));
        when(storeService.countActiveProducts(any())).thenReturn(0L);

        Result<?> response = controller.nearby(nearbyParam("118.08", "24.48", "5"), new Page<>(1, 10));

        @SuppressWarnings("unchecked")
        Page<StoreVO> page = (Page<StoreVO>) response.getData();
        assertThat(page.getTotal()).isEqualTo(3);
        assertThat(page.getRecords()).extracting(StoreVO::getId).containsExactly(1L, 4L, 3L);
        assertThat(page.getRecords().get(0).getDistanceKm()).isEqualByComparingTo(page.getRecords().get(1).getDistanceKm());
        assertThat(page.getRecords().get(1).getDistanceKm()).isLessThan(page.getRecords().get(2).getDistanceKm());
    }

    @Test
    void nearbyFiltersByRawDistanceBeforeRoundingDistanceForResponse() {
        Store justOutsideRadius = store(1L, "0", "0.08995");
        when(storeService.list(any(QueryWrapper.class))).thenReturn(List.of(justOutsideRadius));

        Result<?> response = controller.nearby(nearbyParam("0", "0", "10"), new Page<>(1, 10));

        @SuppressWarnings("unchecked")
        Page<StoreVO> page = (Page<StoreVO>) response.getData();
        assertThat(page.getTotal()).isZero();
        assertThat(page.getRecords()).isEmpty();
        verify(storeService, never()).countActiveProducts(any());
    }

    @Test
    void nearbyPaginatesAndReturnsEmptyRecordsWhenCurrentExceedsTotal() {
        when(storeService.list(any(QueryWrapper.class))).thenReturn(List.of(
                store(1L, "118.0801", "24.4801"),
                store(2L, "118.0802", "24.4802"),
                store(3L, "118.0803", "24.4803")
        ));
        when(storeService.countActiveProducts(any())).thenReturn(0L);

        Result<?> firstPageResponse = controller.nearby(nearbyParam("118.08", "24.48", "5"), new Page<>(2, 1));
        @SuppressWarnings("unchecked")
        Page<StoreVO> firstPage = (Page<StoreVO>) firstPageResponse.getData();
        assertThat(firstPage.getTotal()).isEqualTo(3);
        assertThat(firstPage.getCurrent()).isEqualTo(2);
        assertThat(firstPage.getSize()).isEqualTo(1);
        assertThat(firstPage.getRecords()).extracting(StoreVO::getId).containsExactly(2L);

        Result<?> beyondPageResponse = controller.nearby(nearbyParam("118.08", "24.48", "5"), new Page<>(5, 2));
        @SuppressWarnings("unchecked")
        Page<StoreVO> beyondPage = (Page<StoreVO>) beyondPageResponse.getData();
        assertThat(beyondPage.getTotal()).isEqualTo(3);
        assertThat(beyondPage.getRecords()).isEmpty();

        Result<?> overflowPageResponse = controller.nearby(nearbyParam("118.08", "24.48", "5"), new Page<>(Long.MAX_VALUE, 10));
        @SuppressWarnings("unchecked")
        Page<StoreVO> overflowPage = (Page<StoreVO>) overflowPageResponse.getData();
        assertThat(overflowPage.getTotal()).isEqualTo(3);
        assertThat(overflowPage.getCurrent()).isEqualTo(Long.MAX_VALUE);
        assertThat(overflowPage.getSize()).isEqualTo(10);
        assertThat(overflowPage.getRecords()).isEmpty();
    }

    @Test
    void nearbySkipsStoresWithoutCoordinates() {
        Store missingLongitude = store(1L, null, "24.4801");
        Store missingLatitude = store(2L, "118.0802", null);
        Store normal = store(3L, "118.0803", "24.4803");
        when(storeService.list(any(QueryWrapper.class))).thenReturn(List.of(missingLongitude, missingLatitude, normal));
        when(storeService.countActiveProducts(any())).thenReturn(0L);

        Result<?> response = controller.nearby(nearbyParam("118.08", "24.48", "5"), new Page<>(1, 10));

        @SuppressWarnings("unchecked")
        Page<StoreVO> page = (Page<StoreVO>) response.getData();
        assertThat(page.getRecords()).extracting(StoreVO::getId).containsExactly(3L);
    }

    @Test
    void nearbyPassesKeywordAndCityIntoQueryWrapper() {
        when(storeService.list(any(QueryWrapper.class))).thenReturn(List.of(store(1L, "118.0801", "24.4801")));
        when(storeService.countActiveProducts(any())).thenReturn(0L);
        StoreDTO param = nearbyParam("118.08", "24.48", "5");
        param.setKeyword("暖窝");
        param.setCity("厦门市");

        controller.nearby(param, new Page<>(1, 10));

        @SuppressWarnings({"rawtypes", "unchecked"})
        ArgumentCaptor<QueryWrapper<Store>> captor = ArgumentCaptor.forClass((Class) QueryWrapper.class);
        verify(storeService).list(captor.capture());
        QueryWrapper<Store> wrapper = captor.getValue();
        assertThat(wrapper.getSqlSegment()).contains("store_name", "city");
        assertThat(wrapper.getParamNameValuePairs().values()).contains("%暖窝%", "厦门市");
    }

    private StoreDTO nearbyParam(String longitude, String latitude, String radiusKm) {
        StoreDTO dto = new StoreDTO();
        dto.setLongitude(longitude == null ? null : new BigDecimal(longitude));
        dto.setLatitude(latitude == null ? null : new BigDecimal(latitude));
        dto.setRadiusKm(radiusKm == null ? null : new BigDecimal(radiusKm));
        return dto;
    }

    private Store store(Long id, String longitude, String latitude) {
        Store store = new Store();
        store.setId(id);
        store.setUserId(100L + id);
        store.setStoreName("WangVerify-" + id);
        store.setLongitude(longitude == null ? null : new BigDecimal(longitude));
        store.setLatitude(latitude == null ? null : new BigDecimal(latitude));
        store.setStatus(1);
        store.setDeleted(0);
        return store;
    }
}
