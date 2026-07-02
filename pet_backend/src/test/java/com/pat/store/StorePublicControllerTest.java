package com.pat.store;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pat.common.domain.Result;
import com.pat.common.exception.GlobalExceptionHandler;
import com.pat.product.domain.entity.Product;
import com.pat.product.domain.vo.ProductVO;
import com.pat.store.controller.StorePublicController;
import com.pat.store.domain.dto.NearbyQuery;
import com.pat.store.domain.entity.Store;
import com.pat.store.domain.vo.NearbyStoreRow;
import com.pat.store.domain.vo.StoreVO;
import com.pat.store.service.IStoreService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StorePublicControllerTest {

    private final IStoreService storeService = mock(IStoreService.class);
    private final StorePublicController controller = new StorePublicController(storeService);

    @Test
    void nearbyReturnsPagedRecordsWithRoundedDistance() {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<NearbyStoreRow> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 20, 2);
        page.setRecords(List.of(row(1L, "1.234"), row(2L, "2.235")));
        when(storeService.searchNearby(any(NearbyQuery.class))).thenReturn(page);
        when(storeService.countActiveProducts(any())).thenReturn(0L);

        Result<IPage<StoreVO>> response = controller.nearby(nearbyQuery());

        IPage<StoreVO> data = response.getData();
        assertThat(data.getTotal()).isEqualTo(2L);
        assertThat(data.getCurrent()).isEqualTo(1L);
        assertThat(data.getSize()).isEqualTo(20L);
        assertThat(data.getRecords()).extracting(StoreVO::getId).containsExactly(1L, 2L);
        assertThat(data.getRecords()).extracting(StoreVO::getDistanceKm)
                .containsExactly(new BigDecimal("1.23"), new BigDecimal("2.24"));
    }

    @Test
    void storeProductsRejectsMissingOrClosedStore() {
        when(storeService.getOne(any(), anyBoolean())).thenReturn(null);

        Result<List<ProductVO>> response = controller.storeProducts(404L);

        assertThat(response.getCode()).isNotEqualTo(200);
        assertThat(response.getMessage()).contains("商店不存在");
    }

    @Test
    void storeProductsReturnsPublicProductVOs() {
        Store store = new Store();
        store.setId(1L);
        store.setStatus(1);
        Product product = new Product();
        product.setId(10L);
        product.setStoreId(1L);
        product.setProductName("WangVerify-product");
        product.setProductType(2);
        product.setPrice(BigDecimal.valueOf(20));
        product.setStock(3);
        product.setStatus(1);
        product.setMainImage("/images/product.png");
        product.setCreateTime(LocalDateTime.now());
        when(storeService.getOne(any(), anyBoolean())).thenReturn(store);
        when(storeService.getStoreProducts(1L)).thenReturn(List.of(product));

        Result<List<ProductVO>> response = controller.storeProducts(1L);

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).getId()).isEqualTo(10L);
        assertThat(response.getData().get(0).getStatus()).isEqualTo("上架");
        assertThat(response.getData().get(0).getName()).isEqualTo("WangVerify-product");
    }

    @Test
    void invalidNearbyParametersReturnUnifiedResult() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/api/store/nearby")
                        .param("longitude", "181")
                        .param("latitude", "24.48")
                        .param("radiusKm", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(408))
                .andExpect(jsonPath("$.message").value("经度范围必须在-180到180之间"))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    private NearbyQuery nearbyQuery() {
        NearbyQuery query = new NearbyQuery();
        query.setLongitude(BigDecimal.valueOf(118.08));
        query.setLatitude(BigDecimal.valueOf(24.48));
        query.setRadiusKm(BigDecimal.TEN);
        return query;
    }

    private NearbyStoreRow row(Long id, String distanceKm) {
        NearbyStoreRow row = new NearbyStoreRow();
        row.setId(id);
        row.setUserId(100L + id);
        row.setStoreName("WangVerify-" + id);
        row.setLongitude(BigDecimal.valueOf(118.08));
        row.setLatitude(BigDecimal.valueOf(24.48));
        row.setStatus(1);
        row.setDistanceKm(new BigDecimal(distanceKm));
        return row;
    }
}
