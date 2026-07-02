package com.pat.store;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.pat.common.exception.BusinessException;
import com.pat.store.controller.StoreController;
import com.pat.store.domain.dto.StoreDTO;
import com.pat.store.domain.entity.Store;
import com.pat.store.helper.MapHelper;
import com.pat.store.mapper.StoreMapper;
import com.pat.store.service.IStoreService;
import com.pat.store.service.impl.StoreServiceImpl;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
    void pendingApprovalSavesAuditorTimeAndRemark() {
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
    void pendingRejectionUsesStatusThreeAndRequiresReason() {
        when(mapper.selectById(1L)).thenReturn(store(1L, 0));
        when(mapper.update(isNull(), any(Wrapper.class))).thenReturn(1);

        assertThat(service.auditStore(1L, 3, 99L, "地址不完整").getStatus()).isEqualTo(3);
        assertThatThrownBy(() -> service.auditStore(1L, 3, 99L, " "))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("审核驳回原因不能为空");
    }

    @ParameterizedTest
    @CsvSource({
            "1,1",
            "1,3",
            "2,1",
            "2,3",
            "3,1",
            "3,3"
    })
    void auditRejectsStoresThatAreNotPending(Integer currentStatus, Integer auditStatus) {
        when(mapper.selectById(1L)).thenReturn(store(1L, currentStatus));

        assertThatThrownBy(() -> service.auditStore(1L, auditStatus, 99L, auditStatus == 3 ? "原因" : null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("只有待审核门店可以执行审核");
        verify(mapper, never()).update(isNull(), any(Wrapper.class));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void auditUpdateRequiresPendingAndNotDeletedInSqlCondition() {
        when(mapper.selectById(1L)).thenReturn(store(1L, 0));
        when(mapper.update(isNull(), any(Wrapper.class))).thenReturn(1);

        service.auditStore(1L, 1, 99L, null);

        LambdaUpdateWrapper<Store> wrapper = captureUpdateWrapper();
        assertThat(wrapper.getSqlSet()).contains("status", "audit_user_id", "audit_time", "audit_remark");
        assertThat(wrapper.getSqlSegment()).contains("id", "status", "deleted");
        assertThat(((AbstractWrapper) wrapper).getParamNameValuePairs().values())
                .contains(1L, 1, 99L, 0);
        long zeroConditions = ((AbstractWrapper<?, ?, ?>) wrapper).getParamNameValuePairs().values().stream()
                .filter(Integer.valueOf(0)::equals)
                .count();
        assertThat(zeroConditions).isGreaterThanOrEqualTo(2);
    }

    @Test
    void auditFailsWhenAtomicUpdateFindsNoPendingRow() {
        when(mapper.selectById(1L)).thenReturn(store(1L, 0));
        when(mapper.update(isNull(), any(Wrapper.class))).thenReturn(0);

        assertThatThrownBy(() -> service.auditStore(1L, 1, 99L, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("只有待审核门店可以执行审核");
    }

    @Test
    void auditRejectsMissingAuditorBeforeUpdating() {
        assertThatThrownBy(() -> service.auditStore(1L, 1, null, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("未获取到当前审核人员");
        verify(mapper, never()).update(isNull(), any(Wrapper.class));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void approvalWithoutRemarkStillClearsPreviousRemarkWhenStoreIsPendingAgain() {
        Store pendingAgain = store(1L, 0);
        pendingAgain.setAuditRemark("旧驳回原因");
        when(mapper.selectById(1L)).thenReturn(pendingAgain);
        when(mapper.update(isNull(), any(Wrapper.class))).thenReturn(1);

        Store result = service.auditStore(1L, 1, 99L, null);

        LambdaUpdateWrapper<Store> wrapper = captureUpdateWrapper();
        assertThat(wrapper.getSqlSet()).contains("audit_remark");
        assertThat(result.getAuditRemark()).isNull();
    }

    @Test
    void closeAllowsOpenStoreWithoutOnlineProductsAndSavesReason() {
        when(mapper.selectById(1L)).thenReturn(store(1L, 1));
        when(mapper.countOnlineProducts(1L)).thenReturn(0L);
        when(mapper.update(isNull(), any(Wrapper.class))).thenReturn(1);

        Store result = service.closeStore(1L, "商家停止经营");

        assertThat(result.getStatus()).isEqualTo(2);
        assertThat(result.getCloseReason()).isEqualTo("商家停止经营");
    }

    @Test
    void closeRejectsOpenStoreWithOnlineProducts() {
        when(mapper.selectById(1L)).thenReturn(store(1L, 1));
        when(mapper.countOnlineProducts(1L)).thenReturn(1L);

        assertThatThrownBy(() -> service.closeStore(1L, "仍有商品"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("门店仍有上架商品，不能关闭");
        verify(mapper, never()).update(isNull(), any(Wrapper.class));
    }

    @Test
    void closeRejectsMissingStoreBeforeCountingProducts() {
        when(mapper.selectById(1L)).thenReturn(null);

        assertThatThrownBy(() -> service.closeStore(1L, "不存在"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("商店不存在");
        verify(mapper, never()).countOnlineProducts(1L);
        verify(mapper, never()).update(isNull(), any(Wrapper.class));
    }

    @Test
    void closeAllowsStoreWithOnlyOfflineProducts() {
        when(mapper.selectById(1L)).thenReturn(store(1L, 1));
        when(mapper.countActiveProducts(1L)).thenReturn(5L);
        when(mapper.countOnlineProducts(1L)).thenReturn(0L);
        when(mapper.update(isNull(), any(Wrapper.class))).thenReturn(1);

        assertThat(service.closeStore(1L, "下架商品不阻止关闭").getStatus()).isEqualTo(2);
        verify(mapper).countOnlineProducts(1L);
        verify(mapper, never()).countActiveProducts(1L);
    }

    @Test
    void closeAllowsStoreWithOnlySoldProducts() {
        when(mapper.selectById(1L)).thenReturn(store(1L, 1));
        when(mapper.countActiveProducts(1L)).thenReturn(3L);
        when(mapper.countOnlineProducts(1L)).thenReturn(0L);
        when(mapper.update(isNull(), any(Wrapper.class))).thenReturn(1);

        assertThat(service.closeStore(1L, "已售出商品不阻止关闭").getStatus()).isEqualTo(2);
        verify(mapper).countOnlineProducts(1L);
        verify(mapper, never()).countActiveProducts(1L);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 2, 3})
    void closeRejectsStoresThatAreNotOpen(Integer currentStatus) {
        when(mapper.selectById(1L)).thenReturn(store(1L, currentStatus));

        assertThatThrownBy(() -> service.closeStore(1L, "状态不允许"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("只有营业中门店可以关闭");
        verify(mapper, never()).countOnlineProducts(1L);
        verify(mapper, never()).update(isNull(), any(Wrapper.class));
    }

    @Test
    void closeRequiresReason() {
        assertThatThrownBy(() -> service.closeStore(1L, " "))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("门店关闭原因不能为空");
        verify(mapper, never()).selectById(1L);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void closeUpdateRequiresOpenAndNotDeletedInSqlCondition() {
        when(mapper.selectById(1L)).thenReturn(store(1L, 1));
        when(mapper.countOnlineProducts(1L)).thenReturn(0L);
        when(mapper.update(isNull(), any(Wrapper.class))).thenReturn(1);

        service.closeStore(1L, "正常关闭");

        LambdaUpdateWrapper<Store> wrapper = captureUpdateWrapper();
        assertThat(wrapper.getSqlSet()).contains("status", "close_reason");
        assertThat(wrapper.getSqlSegment()).contains("id", "status", "deleted");
        assertThat(((AbstractWrapper) wrapper).getParamNameValuePairs().values())
                .contains(1L, 2, "正常关闭", 1, 0);
    }

    @Test
    void closeUsesOnlineProductCountInsteadOfActiveProductCount() {
        when(mapper.selectById(1L)).thenReturn(store(1L, 1));
        when(mapper.countActiveProducts(1L)).thenReturn(99L);
        when(mapper.countOnlineProducts(1L)).thenReturn(0L);
        when(mapper.update(isNull(), any(Wrapper.class))).thenReturn(1);

        service.closeStore(1L, "只检查上架商品");

        verify(mapper).countOnlineProducts(1L);
        verify(mapper, never()).countActiveProducts(1L);
    }

    @Test
    void closeFailsWhenAtomicUpdateFindsNoOpenRow() {
        when(mapper.selectById(1L)).thenReturn(store(1L, 1));
        when(mapper.countOnlineProducts(1L)).thenReturn(0L);
        when(mapper.update(isNull(), any(Wrapper.class))).thenReturn(0);

        assertThatThrownBy(() -> service.closeStore(1L, "并发关闭"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("只有营业中门店可以关闭");
    }

    @Test
    void adminProfileUpdateCannotModifyOwner() {
        IStoreService storeService = mock(IStoreService.class);
        StoreDTO dto = editableDto();
        dto.setId(999L);
        dto.setUserId(22L);

        Store update = captureAdminUpdate(storeService, dto);

        assertThat(update.getId()).isEqualTo(1L);
        assertThat(update.getUserId()).isNull();
    }

    @Test
    void adminProfileUpdateCannotModifyStatus() {
        IStoreService storeService = mock(IStoreService.class);
        StoreDTO dto = editableDto();
        dto.setStatus(2);

        Store update = captureAdminUpdate(storeService, dto);

        assertThat(update.getStatus()).isNull();
        verify(storeService, never()).auditStore(any(), any(), any(), any());
        verify(storeService, never()).closeStore(any(), any());
    }

    @Test
    void adminProfileUpdateCannotModifyAuditFields() {
        IStoreService storeService = mock(IStoreService.class);
        StoreDTO dto = editableDto();
        dto.setStatus(3);
        dto.setAuditRemark("恶意审核意见");

        Store update = captureAdminUpdate(storeService, dto);

        assertThat(update.getAuditRemark()).isNull();
        assertThat(update.getAuditUserId()).isNull();
        assertThat(update.getAuditTime()).isNull();
        verify(storeService, never()).auditStore(any(), any(), any(), any());
    }

    @Test
    void adminProfileUpdateCannotModifyCloseReason() {
        IStoreService storeService = mock(IStoreService.class);
        StoreDTO dto = editableDto();
        dto.setStatus(2);
        dto.setCloseReason("恶意关闭原因");

        Store update = captureAdminUpdate(storeService, dto);

        assertThat(update.getCloseReason()).isNull();
        verify(storeService, never()).closeStore(any(), any());
    }

    @Test
    void adminProfileUpdateStillWritesEditableBusinessFields() {
        IStoreService storeService = mock(IStoreService.class);
        StoreDTO dto = editableDto();

        Store update = captureAdminUpdate(storeService, dto);

        assertThat(update.getStoreName()).isEqualTo("新门店");
        assertThat(update.getStoreLogo()).isEqualTo("https://example.test/logo.png");
        assertThat(update.getStorePhone()).isEqualTo("18800000000");
        assertThat(update.getStoreDesc()).isEqualTo("新的经营描述");
        assertThat(update.getProvince()).isEqualTo("福建省");
        assertThat(update.getCity()).isEqualTo("厦门市");
        assertThat(update.getDistrict()).isEqualTo("思明区");
        assertThat(update.getAddress()).isEqualTo("软件园二期");
        assertThat(update.getLongitude()).isEqualByComparingTo("118.12");
        assertThat(update.getLatitude()).isEqualByComparingTo("24.49");
    }

    @Test
    void adminCreateStillKeepsOwnerFromRequest() {
        IStoreService storeService = mock(IStoreService.class);
        when(storeService.save(any(Store.class))).thenReturn(true);
        StoreController controller = new StoreController(storeService, mock(MapHelper.class));
        StoreDTO dto = editableDto();
        dto.setUserId(22L);
        dto.setStatus(null);

        controller.save(dto);

        ArgumentCaptor<Store> captor = ArgumentCaptor.forClass(Store.class);
        verify(storeService).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(22L);
        assertThat(captor.getValue().getStatus()).isZero();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private LambdaUpdateWrapper<Store> captureUpdateWrapper() {
        ArgumentCaptor<Wrapper<Store>> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(mapper).update(isNull(), captor.capture());
        return (LambdaUpdateWrapper<Store>) captor.getValue();
    }

    private Store captureAdminUpdate(IStoreService storeService, StoreDTO dto) {
        when(storeService.updateById(any(Store.class))).thenReturn(true);
        StoreController controller = new StoreController(storeService, mock(MapHelper.class));

        controller.update(1L, dto);

        ArgumentCaptor<Store> captor = ArgumentCaptor.forClass(Store.class);
        verify(storeService).updateById(captor.capture());
        return captor.getValue();
    }

    private StoreDTO editableDto() {
        StoreDTO dto = new StoreDTO();
        dto.setStoreName("新门店");
        dto.setStoreLogo("https://example.test/logo.png");
        dto.setStorePhone("18800000000");
        dto.setStoreDesc("新的经营描述");
        dto.setProvince("福建省");
        dto.setCity("厦门市");
        dto.setDistrict("思明区");
        dto.setAddress("软件园二期");
        dto.setLongitude(BigDecimal.valueOf(118.12));
        dto.setLatitude(BigDecimal.valueOf(24.49));
        return dto;
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
