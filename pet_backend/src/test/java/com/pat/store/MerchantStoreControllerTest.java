package com.pat.store;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.store.controller.MerchantStoreController;
import com.pat.store.domain.dto.StoreDTO;
import com.pat.store.domain.entity.Store;
import com.pat.store.service.IStoreService;
import com.pat.common.util.UserHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MerchantStoreControllerTest {

    private final IStoreService storeService = mock(IStoreService.class);
    private final MerchantStoreController controller = new MerchantStoreController(storeService);

    @BeforeEach
    void setUp() {
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
    void merchant1SearchIsLimitedToOwnStores() {
        UserHolder.save("userId", 11L);
        when(storeService.page(any(Page.class), any(Wrapper.class))).thenReturn(new Page<Store>(1, 10, 0));
        controller.search(new StoreDTO(), new Page<>());
        ArgumentCaptor<Wrapper<Store>> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(storeService).page(any(Page.class), captor.capture());
        assertThat(captor.getValue().getSqlSegment()).contains("user_id");
        assertThat(((AbstractWrapper<?, ?, ?>) captor.getValue()).getParamNameValuePairs()).containsValue(11L);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void merchant2SearchIsLimitedToOwnStores() {
        UserHolder.save("userId", 22L);
        when(storeService.page(any(Page.class), any(Wrapper.class))).thenReturn(new Page<Store>(1, 10, 0));
        controller.search(new StoreDTO(), new Page<>());
        ArgumentCaptor<Wrapper<Store>> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(storeService).page(any(Page.class), captor.capture());
        captor.getValue().getSqlSegment();
        assertThat(((AbstractWrapper<?, ?, ?>) captor.getValue()).getParamNameValuePairs()).containsValue(22L);
    }

    @Test
    void merchantCannotViewAnotherMerchantsStore() {
        UserHolder.save("userId", 11L);
        when(storeService.requireOwnedStore(2L, 11L)).thenThrow(forbidden());
        assertThatThrownBy(() -> controller.detail(2L)).isInstanceOf(BusinessException.class);
    }

    @Test
    void merchantCannotUpdateAnotherMerchantsStore() {
        UserHolder.save("userId", 11L);
        when(storeService.requireOwnedStore(2L, 11L)).thenThrow(forbidden());
        assertThatThrownBy(() -> controller.update(2L, new StoreDTO())).isInstanceOf(BusinessException.class);
        verify(storeService, never()).updateById(any());
    }

    @Test
    void merchantCannotDeleteAnotherMerchantsStore() {
        UserHolder.save("userId", 11L);
        when(storeService.requireOwnedStore(2L, 11L)).thenThrow(forbidden());
        assertThatThrownBy(() -> controller.delete(2L)).isInstanceOf(BusinessException.class);
        verify(storeService, never()).removeById(2L);
    }

    @Test
    void createAlwaysUsesCurrentMerchantAsOwner() {
        UserHolder.save("userId", 11L);
        when(storeService.save(any(Store.class))).thenReturn(true);
        StoreDTO dto = validCreate();
        dto.setUserId(22L);
        dto.setStatus(1);
        controller.create(dto);
        ArgumentCaptor<Store> captor = ArgumentCaptor.forClass(Store.class);
        verify(storeService).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(11L);
    }

    @Test
    void forgedOwnerIdIsIgnoredOnCreate() {
        UserHolder.save("userId", 22L);
        when(storeService.save(any(Store.class))).thenReturn(true);
        StoreDTO dto = validCreate();
        dto.setUserId(999L);
        controller.create(dto);
        ArgumentCaptor<Store> captor = ArgumentCaptor.forClass(Store.class);
        verify(storeService).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(22L);
    }

    @Test
    void merchantCannotSelfApproveNewStore() {
        UserHolder.save("userId", 11L);
        when(storeService.save(any(Store.class))).thenReturn(true);
        StoreDTO dto = validCreate();
        dto.setStatus(1);
        controller.create(dto);
        ArgumentCaptor<Store> captor = ArgumentCaptor.forClass(Store.class);
        verify(storeService).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isZero();
        assertThat(captor.getValue().getDeleted()).isZero();
    }

    @Test
    void merchantStoreEditPreservesOwnerAndReturnsToPending() {
        UserHolder.save("userId", 11L);
        Store original = store(1L, 11L);
        when(storeService.requireOwnedStore(1L, 11L)).thenReturn(original);
        when(storeService.updateById(any(Store.class))).thenReturn(true);
        StoreDTO dto = new StoreDTO();
        dto.setStoreName("new name");
        dto.setUserId(22L);
        dto.setStatus(1);
        controller.update(1L, dto);
        ArgumentCaptor<Store> captor = ArgumentCaptor.forClass(Store.class);
        verify(storeService).updateById(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(11L);
        assertThat(captor.getValue().getStatus()).isZero();
    }

    @Test
    void merchantCanDeleteOwnEmptyStore() {
        UserHolder.save("userId", 11L);
        when(storeService.requireOwnedStore(1L, 11L)).thenReturn(store(1L, 11L));
        when(storeService.removeById(1L)).thenReturn(true);
        assertThat(controller.delete(1L).getData()).isTrue();
        verify(storeService).ensureCanCloseOrDelete(1L);
    }

    private StoreDTO validCreate() {
        StoreDTO dto = new StoreDTO();
        dto.setStoreName("merchant store");
        dto.setAddress("test address");
        dto.setLongitude(BigDecimal.valueOf(118));
        dto.setLatitude(BigDecimal.valueOf(24));
        return dto;
    }

    private Store store(Long id, Long userId) {
        Store store = new Store();
        store.setId(id);
        store.setUserId(userId);
        store.setStatus(1);
        store.setDeleted(0);
        return store;
    }

    private BusinessException forbidden() {
        return new BusinessException(ErrorCode.FORBIDDEN, "无权操作该商店");
    }
}
