package com.pat.store;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.pat.common.exception.BusinessException;
import com.pat.store.domain.entity.Store;
import com.pat.store.mapper.StoreMapper;
import com.pat.store.service.impl.StoreServiceImpl;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StoreAuditWorkflowTest {

    private final StoreMapper mapper = mock(StoreMapper.class);
    private final StoreServiceImpl service = new StoreServiceImpl();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        if (TableInfoHelper.getTableInfo(Store.class) == null) {
            TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), Store.class);
        }
    }

    @Test
    void approvalSavesAuditorTimeAndRemark() {
        when(mapper.selectById(1L)).thenReturn(store(1L, 0));
        when(mapper.update(isNull(), any(Wrapper.class))).thenReturn(1);

        Store result = service.auditStore(1L, 1, 99L, "资料合格");

        verify(mapper).update(isNull(), any(Wrapper.class));
        assertThat(result.getStatus()).isEqualTo(1);
        assertThat(result.getAuditUserId()).isEqualTo(99L);
        assertThat(result.getAuditRemark()).isEqualTo("资料合格");
        assertThat(result.getAuditTime()).isNotNull();
    }

    @Test
    void rejectionUsesStatusThreeAndRequiresReason() {
        when(mapper.selectById(1L)).thenReturn(store(1L, 0));
        when(mapper.update(isNull(), any(Wrapper.class))).thenReturn(1);
        assertThat(service.auditStore(1L, 3, 99L, "地址不完整").getStatus()).isEqualTo(3);
        assertThatThrownBy(() -> service.auditStore(1L, 3, 99L, " "))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void approvalWithoutRemarkStillOverwritesPreviousRejectionRemark() {
        Store rejected = store(1L, 3);
        rejected.setAuditRemark("旧驳回原因");
        when(mapper.selectById(1L)).thenReturn(rejected);
        when(mapper.update(isNull(), any(Wrapper.class))).thenReturn(1);

        Store result = service.auditStore(1L, 1, 99L, null);

        ArgumentCaptor<Wrapper<Store>> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(mapper).update(isNull(), captor.capture());
        assertThat(((LambdaUpdateWrapper<Store>) captor.getValue()).getSqlSet()).contains("audit_remark");
        assertThat(result.getAuditRemark()).isNull();
    }

    @Test
    void closeSavesRequiredReason() {
        when(mapper.selectById(1L)).thenReturn(store(1L, 1));
        when(mapper.updateById(any(Store.class))).thenReturn(1);
        assertThat(service.closeStore(1L, "商家停止经营").getCloseReason()).isEqualTo("商家停止经营");
        assertThatThrownBy(() -> service.closeStore(1L, null)).isInstanceOf(BusinessException.class);
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
