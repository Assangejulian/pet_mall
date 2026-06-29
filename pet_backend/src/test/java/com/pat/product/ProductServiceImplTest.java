package com.pat.product;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pat.common.exception.BusinessException;
import com.pat.product.domain.dto.ProductCreateDTO;
import com.pat.product.domain.dto.ProductUpdateDTO;
import com.pat.product.domain.entity.Product;
import com.pat.product.mapper.ProductMapper;
import com.pat.product.mapper.ProductStoreLookupMapper;
import com.pat.product.service.impl.ProductServiceImpl;
import com.pat.store.mapper.StoreMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductServiceImplTest {

    private final ProductMapper productMapper = mock(ProductMapper.class);
    private final ProductStoreLookupMapper storeLookupMapper = mock(ProductStoreLookupMapper.class);
    private final StoreMapper storeMapper = mock(StoreMapper.class);
    private final ProductServiceImpl productService = new ProductServiceImpl(storeLookupMapper, storeMapper, new ObjectMapper());

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
}
