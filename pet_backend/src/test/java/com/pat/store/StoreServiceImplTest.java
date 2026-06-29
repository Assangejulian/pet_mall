package com.pat.store;

import com.pat.common.exception.BusinessException;
import com.pat.store.domain.entity.Store;
import com.pat.store.mapper.StoreMapper;
import com.pat.store.service.impl.StoreServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StoreServiceImplTest {

    private final StoreMapper storeMapper = mock(StoreMapper.class);
    private final StoreServiceImpl storeService = new StoreServiceImpl();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(storeService, "baseMapper", storeMapper);
    }

    @Test
    void closeOrDeleteIsRejectedWhenAnyUndeletedProductExists() {
        Store store = store(1L);
        when(storeMapper.selectById(1L)).thenReturn(store);
        when(storeMapper.countActiveProducts(1L)).thenReturn(1L);

        assertThatThrownBy(() -> storeService.ensureCanCloseOrDelete(1L))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThatCode(() -> {
                            if (!ex.getDescription().contains("关联商品")) {
                                throw new AssertionError(ex.getDescription());
                            }
                        }).doesNotThrowAnyException());
    }

    @Test
    void closeOrDeleteIsAllowedWhenNoUndeletedProductExists() {
        Store store = store(1L);
        when(storeMapper.selectById(1L)).thenReturn(store);
        when(storeMapper.countActiveProducts(1L)).thenReturn(0L);

        assertThatCode(() -> storeService.ensureCanCloseOrDelete(1L)).doesNotThrowAnyException();
    }

    @Test
    void closeOrDeleteRejectsMissingStore() {
        when(storeMapper.selectById(404L)).thenReturn(null);

        assertThatThrownBy(() -> storeService.ensureCanCloseOrDelete(404L))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThatCode(() -> {
                            if (!ex.getDescription().contains("商店不存在")) {
                                throw new AssertionError(ex.getDescription());
                            }
                        }).doesNotThrowAnyException());
    }

    private Store store(Long id) {
        Store store = new Store();
        store.setId(id);
        store.setStatus(1);
        store.setDeleted(0);
        return store;
    }
}
