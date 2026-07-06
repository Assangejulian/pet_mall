package com.pat.product;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pat.common.exception.BusinessException;
import com.pat.product.domain.dto.ProductCreateDTO;
import com.pat.product.domain.dto.ProductUpdateDTO;
import com.pat.product.domain.entity.Product;
import com.pat.product.mapper.ProductMapper;
import com.pat.product.mapper.ProductStoreLookupMapper;
import com.pat.product.service.impl.ProductServiceImpl;
import com.pat.store.service.IStoreService;
import com.pat.store.domain.entity.Store;
import com.pat.product.domain.dto.ProductQueryDTO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

class ProductServiceImplTest {

    private final ProductMapper productMapper = mock(ProductMapper.class);
    private final ProductStoreLookupMapper storeLookupMapper = mock(ProductStoreLookupMapper.class);
    private final IStoreService storeService = mock(IStoreService.class);
    private final ProductServiceImpl productService =
            new ProductServiceImpl(storeLookupMapper, storeService, new ObjectMapper());

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(productService, "baseMapper", productMapper);
        if (TableInfoHelper.getTableInfo(Product.class) == null) {
            TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), Product.class);
        }
    }

    @Test
    void createProductSucceedsOnlyForOperatingStore() {
        ProductCreateDTO dto = createDto("1", 1);
        when(storeLookupMapper.existsOperatingStore(1L)).thenReturn(1);
        when(productMapper.insert(any(Product.class))).thenReturn(1);

        assertThat(productService.createProduct(dto).getStatus()).isEqualTo("上架");
    }

    @Test
    void createProductRejectsLivePetStockGreaterThanOne() {
        ProductCreateDTO dto = createDto("1", 2);
        dto.setProductType(1);
        when(storeLookupMapper.existsOperatingStore(1L)).thenReturn(1);

        assertThatThrownBy(() -> productService.createProduct(dto))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getDescription()).contains("活体宠物"));
    }

    @Test
    void createProductAllowsSupplyStockGreaterThanOne() {
        ProductCreateDTO dto = createDto("1", 10);
        dto.setProductType(2);
        when(storeLookupMapper.existsOperatingStore(1L)).thenReturn(1);
        when(productMapper.insert(any(Product.class))).thenReturn(1);

        assertThat(productService.createProduct(dto).getStatus()).isEqualTo("上架");
    }

    @Test
    void createProductRejectsPendingOrClosedStore() {
        ProductCreateDTO dto = createDto("1", 1);
        when(storeLookupMapper.existsOperatingStore(1L)).thenReturn(0);
        when(storeLookupMapper.existsUndeletedStore(1L)).thenReturn(1);

        assertThatThrownBy(() -> productService.createProduct(dto))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getDescription()).contains("未营业"));
    }

    @Test
    void createProductRejectsZeroStockWhenStatusIsOnline() {
        ProductCreateDTO dto = createDto("1", 0);
        when(storeLookupMapper.existsOperatingStore(1L)).thenReturn(1);

        assertThatThrownBy(() -> productService.createProduct(dto))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getDescription()).contains("库存为0"));
    }

    @Test
    void zeroStockProductCannotBePutOnline() {
        Product product = product(1L, 1L, 0, 0);
        when(productMapper.selectById(1L)).thenReturn(product);
        when(storeLookupMapper.existsOperatingStore(1L)).thenReturn(1);

        assertThatThrownBy(() -> productService.onlineProduct(1L))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getDescription()).contains("库存为0"));
    }

    @Test
    void productInNonOperatingStoreCannotBePutOnline() {
        Product product = product(1L, 1L, 0, 1);
        when(productMapper.selectById(1L)).thenReturn(product);
        when(storeLookupMapper.existsOperatingStore(1L)).thenReturn(0);
        when(storeLookupMapper.existsUndeletedStore(1L)).thenReturn(1);

        assertThatThrownBy(() -> productService.onlineProduct(1L))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getDescription()).contains("未营业"));
    }

    @Test
    void productCannotBeMovedToNonOperatingStore() {
        Product product = product(1L, 1L, 0, 1);
        ProductUpdateDTO dto = new ProductUpdateDTO();
        dto.setStoreId(2L);
        when(productMapper.selectById(1L)).thenReturn(product);
        when(storeLookupMapper.existsOperatingStore(2L)).thenReturn(0);
        when(storeLookupMapper.existsUndeletedStore(2L)).thenReturn(1);

        assertThatThrownBy(() -> productService.updateProduct(1L, dto))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getDescription()).contains("未营业"));
    }

    @Test
    void soldProductCannotBePutOnlineOrOffline() {
        Product product = product(1L, 1L, 2, 1);
        when(productMapper.selectById(1L)).thenReturn(product);
        when(storeLookupMapper.existsOperatingStore(1L)).thenReturn(1);

        assertThatThrownBy(() -> productService.onlineProduct(1L))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getDescription()).contains("已售出"));

        assertThatThrownBy(() -> productService.offlineProduct(1L))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getDescription()).contains("已售出"));
    }

    @Test
    void manualSoldStatusIsRejectedOnCreateAndUpdate() {
        ProductCreateDTO createDTO = createDto("2", 1);
        when(storeLookupMapper.existsOperatingStore(1L)).thenReturn(1);
        assertThatThrownBy(() -> productService.createProduct(createDTO))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getDescription()).contains("订单流程"));

        Product existing = product(1L, 1L, 0, 1);
        when(productMapper.selectById(1L)).thenReturn(existing);
        ProductUpdateDTO updateDTO = new ProductUpdateDTO();
        updateDTO.setStatus("2");
        assertThatThrownBy(() -> productService.updateProduct(1L, updateDTO))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getDescription()).contains("订单流程"));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void merchantProductSearchIsLimitedThroughOwnedStores() {
        when(productMapper.selectPage(any(Page.class), any(Wrapper.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        productService.pageMerchantProducts(new ProductQueryDTO(), 11L);
        ArgumentCaptor<Wrapper<Product>> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(productMapper).selectPage(any(Page.class), captor.capture());
        assertThat(captor.getValue().getSqlSegment()).contains("user_id = 11");
    }

    @Test
    void merchantCannotViewAnotherMerchantsProduct() {
        mockProductOwner(22L);
        assertForbidden(() -> productService.requireOwnedProduct(1L, 11L));
    }

    @Test
    void merchantCannotModifyAnotherMerchantsProduct() {
        mockProductOwner(22L);
        assertForbidden(() -> productService.updateMerchantProduct(1L, new ProductUpdateDTO(), 11L));
    }

    @Test
    void merchantCannotDeleteAnotherMerchantsProduct() {
        mockProductOwner(22L);
        assertForbidden(() -> productService.requireOwnedProduct(1L, 11L));
    }

    @Test
    void merchantCannotPutAnotherMerchantsProductOnline() {
        mockProductOwner(22L);
        assertForbidden(() -> productService.requireOwnedProduct(1L, 11L));
    }

    @Test
    void merchantCannotPutAnotherMerchantsProductOffline() {
        mockProductOwner(22L);
        assertForbidden(() -> productService.requireOwnedProduct(1L, 11L));
    }

    @Test
    void merchantCannotCreateProductInAnotherMerchantsStore() {
        when(storeService.getById(2L)).thenReturn(store(2L, 22L));
        ProductCreateDTO dto = createDto("1", 1);
        dto.setStoreId(2L);
        assertForbidden(() -> productService.createMerchantProduct(dto, 11L));
    }

    @Test
    void merchantCannotTransferProductToAnotherMerchantsStore() {
        mockProductOwner(11L);
        when(storeService.getById(2L)).thenReturn(store(2L, 22L));
        ProductUpdateDTO dto = new ProductUpdateDTO();
        dto.setStoreId(2L);
        assertForbidden(() -> productService.updateMerchantProduct(1L, dto, 11L));
    }

    @Test
    void productWithOrderHistoryCannotBeTransferredEvenWithinSameMerchant() {
        mockProductOwner(11L);
        when(storeService.getById(2L)).thenReturn(store(2L, 11L));
        when(productMapper.countOrderItemsByProductId(1L)).thenReturn(1L);
        ProductUpdateDTO dto = new ProductUpdateDTO();
        dto.setStoreId(2L);

        assertThatThrownBy(() -> productService.updateMerchantProduct(1L, dto, 11L))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getDescription()).contains("历史订单"));
        verify(productMapper, org.mockito.Mockito.never()).updateById(any(Product.class));
    }

    @Test
    void productWithoutOrderHistoryCanBeTransferredWithinSameMerchant() {
        mockProductOwner(11L);
        when(storeService.getById(2L)).thenReturn(store(2L, 11L));
        when(productMapper.countOrderItemsByProductId(1L)).thenReturn(0L);
        when(storeLookupMapper.existsOperatingStore(2L)).thenReturn(1);
        when(productMapper.updateById(any(Product.class))).thenReturn(1);
        ProductUpdateDTO dto = new ProductUpdateDTO();
        dto.setStoreId(2L);

        productService.updateMerchantProduct(1L, dto, 11L);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productMapper).updateById(captor.capture());
        assertThat(captor.getValue().getStoreId()).isEqualTo(2L);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void livePetStockDeductionMarksSoldOnlyWhenLastUnitIsReserved() {
        when(productMapper.update(any(), any(Wrapper.class))).thenReturn(1);
        ArgumentCaptor<Wrapper<Product>> captor = ArgumentCaptor.forClass(Wrapper.class);

        assertThat(productService.deductStock(1L, 1)).isTrue();

        verify(productMapper).update(org.mockito.ArgumentMatchers.isNull(), captor.capture());
        String sqlSet = ((com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Product>) captor.getValue())
                .getSqlSet();
        assertThat(sqlSet).contains("product_type = 1", "stock = 1", "THEN 2", "stock = stock - 1");
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void supplyStockDeductionDoesNotMarkTheProductSold() {
        when(productMapper.update(any(), any(Wrapper.class))).thenReturn(1);
        ArgumentCaptor<Wrapper<Product>> captor = ArgumentCaptor.forClass(Wrapper.class);

        assertThat(productService.deductStock(2L, 3)).isTrue();

        verify(productMapper).update(org.mockito.ArgumentMatchers.isNull(), captor.capture());
        String sqlSet = ((com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Product>) captor.getValue())
                .getSqlSet();
        assertThat(sqlSet).contains("product_type = 1", "ELSE status", "stock = stock - 3");
    }

    @Test
    void soldLivePetReturnsOnlineOnlyForOperatingStoreWithoutRestriction() {
        Product pet = soldLivePet();
        Store store = store(1L, 11L);

        LambdaUpdateWrapper<Product> update = restoreLivePet(pet, store, true);

        assertThat(update.getSqlSet()).contains("stock", "status");
        assertThat(update.getSqlSegment()).contains("EXISTS", "offline_reason", "offline_user_id", "offline_time");
        assertThat(update.getParamNameValuePairs()).containsEntry("MPGENVAL2", 1);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 0, 3})
    void soldLivePetStaysOfflineForClosedPendingOrRejectedStore(int storeStatus) {
        Store store = store(1L, 11L);
        store.setStatus(storeStatus);

        LambdaUpdateWrapper<Product> update = restoreLivePet(soldLivePet(), store, true);

        assertOfflineRestore(update);
    }

    @Test
    void soldLivePetStaysOfflineForLogicallyDeletedStore() {
        Store store = store(1L, 11L);
        store.setDeleted(1);

        LambdaUpdateWrapper<Product> update = restoreLivePet(soldLivePet(), store, true);

        assertOfflineRestore(update);
    }

    @Test
    void soldLivePetStaysOfflineWhenStoreDoesNotExist() {
        LambdaUpdateWrapper<Product> update = restoreLivePet(soldLivePet(), null, true);

        assertOfflineRestore(update);
    }

    @ParameterizedTest
    @ValueSource(strings = {"reason", "user", "time"})
    void everyPlatformRestrictionKeepsRestoredLivePetOfflineAndIsNotCleared(String restrictedField) {
        Product pet = offlineLivePet();
        if (restrictedField.equals("reason")) pet.setOfflineReason("");
        if (restrictedField.equals("user")) pet.setOfflineUserId(99L);
        if (restrictedField.equals("time")) pet.setOfflineTime(LocalDateTime.of(2026, 7, 5, 12, 0));

        LambdaUpdateWrapper<Product> update = restoreLivePet(pet, store(1L, 11L), true);

        assertThat(update.getSqlSet()).contains("stock").doesNotContain("status");
        assertThat(update.getSqlSet()).doesNotContain("offline_reason", "offline_user_id", "offline_time");
        assertThat(update.getSqlSegment()).contains("product_type", "status", "stock", "deleted",
                "offline_reason", "offline_user_id", "offline_time", "OR");
        if (restrictedField.equals("reason")) assertThat(pet.getOfflineReason()).isEmpty();
        if (restrictedField.equals("user")) assertThat(pet.getOfflineUserId()).isEqualTo(99L);
        if (restrictedField.equals("time")) assertThat(pet.getOfflineTime()).isNotNull();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void orderDeductionThenForceOfflineThenRestoreKeepsPlatformRestriction() {
        Product pet = product(1L, 1L, 1, 1);
        pet.setProductType(1);
        LocalDateTime[] forcedOfflineAt = new LocalDateTime[1];
        when(productMapper.selectById(1L)).thenReturn(pet);
        when(productMapper.update(any(), any(Wrapper.class)))
                .thenAnswer(invocation -> {
                    pet.setStock(0);
                    pet.setStatus(2);
                    return 1;
                })
                .thenAnswer(invocation -> {
                    pet.setStock(1);
                    return 1;
                });
        doAnswer(invocation -> {
            Product update = invocation.getArgument(0);
            pet.setStatus(update.getStatus());
            pet.setOfflineReason(update.getOfflineReason());
            pet.setOfflineUserId(update.getOfflineUserId());
            pet.setOfflineTime(update.getOfflineTime());
            forcedOfflineAt[0] = update.getOfflineTime();
            return 1;
        }).when(productMapper).updateById(any(Product.class));

        assertThat(productService.deductStock(1L, 1)).isTrue();
        assertThat(pet.getStatus()).isEqualTo(2);
        assertThat(pet.getStock()).isZero();

        productService.forceOfflineProduct(1L, "平台强制下架回归", 99L);
        assertThat(pet.getStatus()).isZero();
        assertThat(pet.getOfflineReason()).isEqualTo("平台强制下架回归");
        assertThat(pet.getOfflineUserId()).isEqualTo(99L);
        assertThat(pet.getOfflineTime()).isNotNull();

        assertThat(productService.restoreStock(1L, 1)).isTrue();
        assertThat(pet.getStock()).isOne();
        assertThat(pet.getStatus()).isZero();
        assertThat(pet.getOfflineReason()).isEqualTo("平台强制下架回归");
        assertThat(pet.getOfflineUserId()).isEqualTo(99L);
        assertThat(pet.getOfflineTime()).isEqualTo(forcedOfflineAt[0]);
        assertThat(productService.restoreStock(1L, 1)).isFalse();
        assertThat(pet.getStock()).isOne();

        ArgumentCaptor<Wrapper<Product>> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(productMapper, times(2)).update(org.mockito.ArgumentMatchers.isNull(), captor.capture());
        LambdaUpdateWrapper<Product> restore = (LambdaUpdateWrapper<Product>) captor.getAllValues().get(1);
        assertThat(restore.getSqlSet()).contains("stock").doesNotContain("status", "offline_reason", "offline_user_id", "offline_time");
        assertThat(restore.getSqlSegment()).contains("product_type", "status", "stock", "deleted",
                "offline_reason", "offline_user_id", "offline_time", "OR");
    }

    @Test
    void ordinaryOfflineLivePetCannotBeRestoredWithoutPlatformRestriction() {
        Product pet = offlineLivePet();
        when(productMapper.selectById(1L)).thenReturn(pet);

        assertThat(productService.restoreStock(1L, 1)).isFalse();

        verify(productMapper, never()).update(any(), any(Wrapper.class));
        assertThat(pet.getStock()).isZero();
        assertThat(pet.getStatus()).isZero();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void supplyRestoreKeepsItsOriginalStatus() {
        Product goods = product(2L, 1L, 0, 5);
        when(productMapper.selectById(2L)).thenReturn(goods);
        when(productMapper.update(any(), any(Wrapper.class))).thenReturn(1);
        ArgumentCaptor<Wrapper<Product>> captor = ArgumentCaptor.forClass(Wrapper.class);

        assertThat(productService.restoreStock(2L, 3)).isTrue();

        verify(productMapper).update(org.mockito.ArgumentMatchers.isNull(), captor.capture());
        String sqlSet = ((com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Product>) captor.getValue())
                .getSqlSet();
        assertThat(sqlSet).contains("stock = stock + 3").doesNotContain("status");
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void repeatedLivePetRestoreCannotIncreaseStockPastOne() {
        Product pet = soldLivePet();
        when(productMapper.selectById(1L)).thenReturn(pet);
        when(storeService.getById(1L)).thenReturn(store(1L, 11L));
        when(productMapper.update(any(), any(Wrapper.class))).thenReturn(1, 0, 0);

        assertThat(productService.restoreStock(1L, 1)).isTrue();
        assertThat(productService.restoreStock(1L, 1)).isFalse();

        ArgumentCaptor<Wrapper<Product>> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(productMapper, times(3)).update(org.mockito.ArgumentMatchers.isNull(), captor.capture());
        List<Wrapper<Product>> attempts = captor.getAllValues();
        assertThat(attempts).allSatisfy(wrapper -> {
            LambdaUpdateWrapper<Product> update = (LambdaUpdateWrapper<Product>) wrapper;
            assertThat(update.getSqlSet()).contains("stock", "status");
            assertThat(update.getSqlSegment()).contains("product_type", "status", "stock", "deleted");
            assertThat(update.getParamNameValuePairs()).containsValue(2).containsValue(0);
        });
    }

    @Test
    void merchantCanCreateProductInOwnOperatingStore() {
        when(storeService.getById(1L)).thenReturn(store(1L, 11L));
        when(storeLookupMapper.existsOperatingStore(1L)).thenReturn(1);
        when(productMapper.insert(any(Product.class))).thenReturn(1);
        assertThat(productService.createMerchantProduct(createDto("1", 1), 11L).getStatus()).isEqualTo("上架");
    }

    private ProductCreateDTO createDto(String status, int stock) {
        ProductCreateDTO dto = new ProductCreateDTO();
        dto.setStoreId(1L);
        dto.setProductName("WangVerify-product");
        dto.setProductType(2);
        dto.setPrice(BigDecimal.ONE);
        dto.setStock(stock);
        dto.setStatus(status);
        return dto;
    }

    private Product product(Long id, Long storeId, Integer status, Integer stock) {
        Product product = new Product();
        product.setId(id);
        product.setStoreId(storeId);
        product.setProductName("WangVerify-product");
        product.setProductType(2);
        product.setPrice(BigDecimal.ONE);
        product.setStock(stock);
        product.setStatus(status);
        product.setDeleted(0);
        return product;
    }

    private Product soldLivePet() {
        Product product = product(1L, 1L, 2, 0);
        product.setProductType(1);
        return product;
    }

    private Product offlineLivePet() {
        Product product = product(1L, 1L, 0, 0);
        product.setProductType(1);
        return product;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private LambdaUpdateWrapper<Product> restoreLivePet(Product pet, Store store, boolean expectedResult) {
        when(productMapper.selectById(pet.getId())).thenReturn(pet);
        when(storeService.getById(pet.getStoreId())).thenReturn(store);
        when(productMapper.update(any(), any(Wrapper.class))).thenReturn(expectedResult ? 1 : 0);

        assertThat(productService.restoreStock(pet.getId(), 1)).isEqualTo(expectedResult);

        ArgumentCaptor<Wrapper<Product>> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(productMapper).update(org.mockito.ArgumentMatchers.isNull(), captor.capture());
        return (LambdaUpdateWrapper<Product>) captor.getValue();
    }

    private void assertOfflineRestore(LambdaUpdateWrapper<Product> update) {
        assertThat(update.getSqlSet()).contains("stock", "status");
        assertThat(update.getSqlSegment()).doesNotContain("EXISTS");
        assertThat(update.getParamNameValuePairs()).containsEntry("MPGENVAL2", 0);
    }

    private void mockProductOwner(Long ownerId) {
        when(productMapper.selectById(1L)).thenReturn(product(1L, 1L, 0, 1));
        when(storeService.getById(1L)).thenReturn(store(1L, ownerId));
    }

    private Store store(Long id, Long userId) {
        Store store = new Store();
        store.setId(id);
        store.setUserId(userId);
        store.setStatus(1);
        store.setDeleted(0);
        return store;
    }

    private void assertForbidden(org.assertj.core.api.ThrowableAssert.ThrowingCallable callable) {
        assertThatThrownBy(callable)
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo(403));
    }
}
