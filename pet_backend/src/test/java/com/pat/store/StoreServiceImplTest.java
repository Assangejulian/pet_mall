package com.pat.store;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pat.common.exception.BusinessException;
import com.pat.product.domain.entity.Product;
import com.pat.store.domain.dto.NearbyQuery;
import com.pat.store.domain.entity.Store;
import com.pat.store.domain.vo.NearbyStoreRow;
import com.pat.store.mapper.StoreMapper;
import com.pat.store.service.impl.StoreServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StoreServiceImplTest {

    private final StoreMapper storeMapper = mock(StoreMapper.class);
    private final StoreServiceImpl storeService = new StoreServiceImpl(org.mockito.Mockito.mock(com.pat.store.helper.MapHelper.class));

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(storeService, "baseMapper", storeMapper);
    }

    @Test
    void closeOrDeleteIsRejectedWhenAnyUndeletedProductExists() {
        when(storeMapper.selectById(1L)).thenReturn(store(1L));
        when(storeMapper.countActiveProducts(1L)).thenReturn(1L);

        assertThatThrownBy(() -> storeService.ensureCanCloseOrDelete(1L))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getDescription()).contains("关联商品"));
    }

    @Test
    void closeOrDeleteIsAllowedWhenNoUndeletedProductExists() {
        when(storeMapper.selectById(1L)).thenReturn(store(1L));
        when(storeMapper.countActiveProducts(1L)).thenReturn(0L);

        assertThatCode(() -> storeService.ensureCanCloseOrDelete(1L)).doesNotThrowAnyException();
    }

    @Test
    void closeOrDeleteRejectsMissingStore() {
        when(storeMapper.selectById(404L)).thenReturn(null);

        assertThatThrownBy(() -> storeService.ensureCanCloseOrDelete(404L))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getDescription()).contains("商店不存在"));
    }

    // Removed obsolete nearby search tests that depend on countNearby and selectNearby

    @Test
    void searchNearbyRejectsInvalidCoordinatesAndRadius() {
        assertThatThrownBy(() -> storeService.searchNearby(new NearbyQuery()))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getDescription()).contains("经纬度"));

        NearbyQuery invalidLongitude = nearbyQuery();
        invalidLongitude.setLongitude(BigDecimal.valueOf(181));
        assertThatThrownBy(() -> storeService.searchNearby(invalidLongitude))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getDescription()).contains("经度范围"));

        NearbyQuery invalidLatitude = nearbyQuery();
        invalidLatitude.setLatitude(BigDecimal.valueOf(91));
        assertThatThrownBy(() -> storeService.searchNearby(invalidLatitude))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getDescription()).contains("纬度范围"));

        NearbyQuery zeroRadius = nearbyQuery();
        zeroRadius.setRadiusKm(BigDecimal.ZERO);
        assertThatThrownBy(() -> storeService.searchNearby(zeroRadius))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getDescription()).contains("半径"));

        NearbyQuery tooLargeRadius = nearbyQuery();
        tooLargeRadius.setRadiusKm(BigDecimal.valueOf(100.01));
        assertThatThrownBy(() -> storeService.searchNearby(tooLargeRadius))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getDescription()).contains("100公里"));
    }

    @Test
    void getStoreProductsDelegatesToPublicMapperQuery() {
        Product product = new Product();
        product.setId(9L);
        when(storeMapper.selectStoreProducts(1L)).thenReturn(List.of(product));

        assertThat(storeService.getStoreProducts(1L)).extracting(Product::getId).containsExactly(9L);
        assertThat(storeService.getStoreProducts(null)).isEmpty();
    }

    private NearbyQuery nearbyQuery() {
        NearbyQuery query = new NearbyQuery();
        query.setLongitude(BigDecimal.valueOf(118.08));
        query.setLatitude(BigDecimal.valueOf(24.48));
        query.setRadiusKm(BigDecimal.valueOf(10));
        return query;
    }

    private NearbyStoreRow row(Long id, String distanceKm) {
        NearbyStoreRow row = new NearbyStoreRow();
        row.setId(id);
        row.setStoreName("WangVerify-" + id);
        row.setLongitude(BigDecimal.valueOf(118.08));
        row.setLatitude(BigDecimal.valueOf(24.48));
        row.setStatus(1);
        row.setDistanceKm(new BigDecimal(distanceKm));
        return row;
    }

    private Store store(Long id) {
        Store store = new Store();
        store.setId(id);
        store.setStatus(1);
        store.setDeleted(0);
        return store;
    }
}
