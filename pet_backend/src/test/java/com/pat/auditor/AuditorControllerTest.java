package com.pat.auditor;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.product.controller.AuditorProductController;
import com.pat.product.domain.vo.ProductVO;
import com.pat.product.service.ProductService;
import com.pat.store.controller.AuditorStoreController;
import com.pat.store.domain.dto.StoreDTO;
import com.pat.store.domain.entity.Store;
import com.pat.store.service.IStoreService;
import com.pat.common.exception.BusinessException;
import com.pat.common.util.UserHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuditorControllerTest {

    private final IStoreService storeService = mock(IStoreService.class);
    private final ProductService productService = mock(ProductService.class);
    private final AuditorStoreController storeController = new AuditorStoreController(storeService);
    private final AuditorProductController productController = new AuditorProductController(productService);

    @BeforeEach
    void setUp() {
        UserHolder.save("userId", 99L);
        if (TableInfoHelper.getTableInfo(Store.class) == null) {
            TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), Store.class);
        }
    }

    @AfterEach
    void cleanUp() {
        UserHolder.remove();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void auditorCanSearchAllPendingStores() {
        Store pending = store(1L, 0);
        Page<Store> result = new Page<>(1, 10, 1);
        result.setRecords(List.of(pending));
        when(storeService.page(any(Page.class), any(Wrapper.class))).thenReturn(result);
        StoreDTO query = new StoreDTO();
        query.setStatus(0);
        assertThat(storeController.search(query, new Page<>()).getData().getRecords()).hasSize(1);
        ArgumentCaptor<Wrapper<Store>> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(storeService).page(any(Page.class), captor.capture());
        captor.getValue().getSqlSegment();
        assertThat(((AbstractWrapper<?, ?, ?>) captor.getValue()).getParamNameValuePairs()).containsValue(0);
    }

    @Test
    void auditorCanApproveStore() {
        when(storeService.auditStore(1L, 1, 99L, "ok")).thenReturn(store(1L, 1));
        assertThat(storeController.approve(1L, "ok").getData().getStatus()).isEqualTo(1);
        verify(storeService).auditStore(1L, 1, 99L, "ok");
    }

    @Test
    void auditorCanRejectStore() {
        when(storeService.auditStore(1L, 3, 99L, "invalid")).thenReturn(store(1L, 3));
        assertThat(storeController.reject(1L, "invalid").getData().getStatus()).isEqualTo(3);
        verify(storeService).auditStore(1L, 3, 99L, "invalid");
    }

    @Test
    void auditorCanCloseStore() {
        when(storeService.closeStore(1L, "停业")).thenReturn(store(1L, 2));
        assertThat(storeController.close(1L, "停业").getData().getStatus()).isEqualTo(2);
        verify(storeService).closeStore(1L, "停业");
    }

    @Test
    void rejectAndCloseRequireReason() {
        assertThatThrownBy(() -> storeController.reject(1L, " ")).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> storeController.close(1L, null)).isInstanceOf(BusinessException.class);
    }

    @Test
    void auditorHasNoStoreBusinessEditEndpoint() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(storeController).build();
        mvc.perform(put("/api/auditor/store/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"storeName\":\"forged\"}"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void auditorCanForceProductOffline() {
        ProductVO product = new ProductVO();
        product.setId(1L);
        product.setStatus("下架");
        product.setStatusCode(0);
        when(productService.forceOfflineProduct(1L, "violation", 99L)).thenReturn(product);
        assertThat(productController.forceOffline(1L, "violation").getData().getStatusCode()).isZero();
        verify(productService).forceOfflineProduct(1L, "violation", 99L);
    }

    @Test
    void forceOfflineRequiresReasonAndRestrictionCanBeReleased() {
        assertThatThrownBy(() -> productController.forceOffline(1L, " ")).isInstanceOf(BusinessException.class);
        ProductVO product = new ProductVO();
        product.setId(1L);
        product.setStatusCode(0);
        when(productService.releaseOfflineRestriction(1L)).thenReturn(product);
        assertThat(productController.releaseOffline(1L).getData().getStatusCode()).isZero();
        verify(productService).releaseOfflineRestriction(1L);
    }

    @Test
    void auditorHasNoProductPriceOrStockEditEndpoint() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(productController).build();
        mvc.perform(put("/api/auditor/product/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\":1,\"stock\":99}"))
                .andExpect(status().isMethodNotAllowed());
    }

    private Store store(Long id, Integer status) {
        Store store = new Store();
        store.setId(id);
        store.setUserId(11L);
        store.setStatus(status);
        store.setDeleted(0);
        return store;
    }
}
