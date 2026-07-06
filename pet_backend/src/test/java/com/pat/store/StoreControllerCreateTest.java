package com.pat.store;

import com.pat.store.controller.StoreController;
import com.pat.store.domain.entity.Store;
import com.pat.store.helper.MapHelper;
import com.pat.store.service.IStoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StoreControllerCreateTest {

    private IStoreService storeService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        storeService = mock(IStoreService.class);
        when(storeService.save(any(Store.class))).thenReturn(true);
        StoreController controller = new StoreController(storeService, mock(MapHelper.class));
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void adminCreateAlwaysForcesPendingStatus(int requestedStatus) throws Exception {
        mockMvc.perform(post("/api/admin/store")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(baseJson("\"status\":" + requestedStatus)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        assertThat(capturedStore().getStatus()).isZero();
    }

    @Test
    void adminCreateDiscardsInjectedAuditCloseAndDeletedFields() throws Exception {
        mockMvc.perform(post("/api/admin/store")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(baseJson("\"status\":1,"
                                + "\"auditUserId\":999,"
                                + "\"auditTime\":\"2026-07-05T12:00:00\","
                                + "\"auditRemark\":\"injected audit\","
                                + "\"closeReason\":\"injected close\","
                                + "\"deleted\":1")))
                .andExpect(status().isOk());

        Store saved = capturedStore();
        assertThat(saved.getStatus()).isZero();
        assertThat(saved.getAuditUserId()).isNull();
        assertThat(saved.getAuditTime()).isNull();
        assertThat(saved.getAuditRemark()).isNull();
        assertThat(saved.getCloseReason()).isNull();
        assertThat(saved.getDeleted()).isZero();
    }

    @Test
    void adminCreateStillPreservesOwnerAndBusinessFields() throws Exception {
        mockMvc.perform(post("/api/admin/store")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(baseJson("\"status\":0")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        Store saved = capturedStore();
        assertThat(saved.getUserId()).isEqualTo(22L);
        assertThat(saved.getStoreName()).isEqualTo("安全新增门店");
        assertThat(saved.getAddress()).isEqualTo("软件园二期");
        assertThat(saved.getStatus()).isZero();
        assertThat(saved.getDeleted()).isZero();
    }

    private Store capturedStore() {
        ArgumentCaptor<Store> captor = ArgumentCaptor.forClass(Store.class);
        verify(storeService).save(captor.capture());
        return captor.getValue();
    }

    private String baseJson(String extraFields) {
        return "{"
                + "\"userId\":22,"
                + "\"storeName\":\"安全新增门店\","
                + "\"address\":\"软件园二期\","
                + "\"longitude\":118.12,"
                + "\"latitude\":24.49,"
                + extraFields
                + "}";
    }
}
