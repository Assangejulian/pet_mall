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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
        if (TableInfoHelper.getTableInfo(Store.class) == null) {
            TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), Store.class);
        }
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
        when(storeService.updateAuditStatus(1L, 1)).thenReturn(store(1L, 1));
        assertThat(storeController.approve(1L, "ok").getData().getStatus()).isEqualTo(1);
        verify(storeService).updateAuditStatus(1L, 1);
    }

    @Test
    void auditorCanRejectStore() {
        when(storeService.updateAuditStatus(1L, 2)).thenReturn(store(1L, 2));
        assertThat(storeController.reject(1L, "invalid").getData().getStatus()).isEqualTo(2);
        verify(storeService).updateAuditStatus(1L, 2);
    }

    @Test
    void auditorCanCloseStore() {
        when(storeService.updateAuditStatus(1L, 2)).thenReturn(store(1L, 2));
        assertThat(storeController.close(1L, null).getData().getStatus()).isEqualTo(2);
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
        when(productService.forceOfflineProduct(1L)).thenReturn(product);
        assertThat(productController.forceOffline(1L, "violation").getData().getStatusCode()).isZero();
        verify(productService).forceOfflineProduct(1L);
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
