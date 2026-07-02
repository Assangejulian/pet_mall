package com.pat.product;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pat.common.exception.BusinessException;
import com.pat.product.domain.entity.Product;
import com.pat.product.domain.dto.ProductUpdateDTO;
import com.pat.product.mapper.ProductMapper;
import com.pat.product.mapper.ProductStoreLookupMapper;
import com.pat.product.service.impl.ProductServiceImpl;
import com.pat.store.service.IStoreService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductRegulationWorkflowTest {

    private final ProductMapper mapper = mock(ProductMapper.class);
    private final ProductStoreLookupMapper lookupMapper = mock(ProductStoreLookupMapper.class);
    private final IStoreService storeService = mock(IStoreService.class);
    private final ProductServiceImpl service = new ProductServiceImpl(lookupMapper, storeService, new ObjectMapper());

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        if (TableInfoHelper.getTableInfo(Product.class) == null) {
            TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), Product.class);
        }
    }

    @Test
    void forceOfflineSavesReasonOperatorAndTime() {
        when(mapper.selectById(1L)).thenReturn(product(1L, 1));
        when(mapper.updateById(any(Product.class))).thenReturn(1);

        var result = service.forceOfflineProduct(1L, "虚假宣传", 99L);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(mapper).updateById(captor.capture());
        assertThat(captor.getValue().getStatus()).isZero();
        assertThat(captor.getValue().getOfflineReason()).isEqualTo("虚假宣传");
        assertThat(captor.getValue().getOfflineUserId()).isEqualTo(99L);
        assertThat(captor.getValue().getOfflineTime()).isNotNull();
        assertThat(result.getPlatformRestricted()).isTrue();
    }

    @Test
    void platformRestrictedProductCannotBePutOnline() {
        Product product = product(1L, 0);
        product.setOfflineReason("违规");
        product.setOfflineUserId(99L);
        product.setOfflineTime(LocalDateTime.now());
        when(mapper.selectById(1L)).thenReturn(product);

        assertThatThrownBy(() -> service.onlineProduct(1L))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getDescription()).contains("平台强制下架"));
    }

    @Test
    void productEditCannotBypassPlatformRestriction() {
        Product product = product(1L, 0);
        product.setOfflineReason("违规");
        when(mapper.selectById(1L)).thenReturn(product);
        ProductUpdateDTO dto = new ProductUpdateDTO();
        dto.setStatus("1");
        assertThatThrownBy(() -> service.updateProduct(1L, dto))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getDescription()).contains("平台强制下架"));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void releaseClearsRestrictionAndKeepsProductOffline() {
        Product product = product(1L, 0);
        product.setOfflineReason("违规");
        product.setOfflineUserId(99L);
        product.setOfflineTime(LocalDateTime.now());
        when(mapper.selectById(1L)).thenReturn(product);
        when(mapper.update(isNull(), any(Wrapper.class))).thenReturn(1);

        var result = service.releaseOfflineRestriction(1L);

        assertThat(result.getStatusCode()).isZero();
        assertThat(result.getPlatformRestricted()).isFalse();
        assertThat(result.getOfflineReason()).isNull();
        verify(mapper).update(isNull(), any(Wrapper.class));
    }

    @Test
    void merchantVoluntaryOfflineDoesNotCreatePlatformInformation() {
        Product online = product(1L, 1);
        Product offline = product(1L, 0);
        when(mapper.selectById(1L)).thenReturn(online, offline);
        when(mapper.updateById(any(Product.class))).thenReturn(1);

        var result = service.offlineProduct(1L);

        assertThat(result.getStatusCode()).isZero();
        assertThat(result.getPlatformRestricted()).isFalse();
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void productCanBePutOnlineAfterRestrictionIsReleased() {
        Product product = product(1L, 0);
        when(mapper.selectById(1L)).thenReturn(product);
        when(lookupMapper.existsOperatingStore(1L)).thenReturn(1);
        when(mapper.update(isNull(), any(Wrapper.class))).thenReturn(1);
        assertThat(service.onlineProduct(1L)).isNotNull();
        verify(mapper).update(isNull(), any(Wrapper.class));
    }

    private Product product(Long id, Integer status) {
        Product product = new Product();
        product.setId(id);
        product.setStoreId(1L);
        product.setProductName("测试商品");
        product.setProductType(2);
        product.setPrice(BigDecimal.ONE);
        product.setStock(2);
        product.setStatus(status);
        product.setDeleted(0);
        return product;
    }
}
