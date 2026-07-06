package com.pat.integration;

import com.baomidou.mybatisplus.annotation.TableField;
import com.pat.common.util.StatusDisplayUtil;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.mapper.OrderQueryMapper;
import com.pat.product.domain.dto.ProductQueryDTO;
import com.pat.product.mapper.ProductMapper;
import com.pat.store.mapper.StoreMapper;
import com.pat.video.controller.MerchantVideoController;
import com.pat.video.domain.entity.Video;
import org.apache.ibatis.annotations.Select;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.assertj.core.api.Assertions.assertThat;

class MemberAIntegrationContractTest {

    @Test
    void productQuerySupportsCanonicalPageAndSize() {
        ProductQueryDTO query = new ProductQueryDTO();
        query.setPage(2L);
        query.setSize(20L);
        assertThat(query.getPage()).isEqualTo(2L);
        assertThat(query.getSize()).isEqualTo(20L);
    }

    @Test
    void productQuerySupportsAdminCurrentPage() {
        ProductQueryDTO query = new ProductQueryDTO();
        query.setCurrent(3L);
        assertThat(query.getCurrent()).isEqualTo(3L);
    }

    @Test
    void productQuerySupportsLegacyPageAliases() {
        ProductQueryDTO query = new ProductQueryDTO();
        query.setPageNum(4L);
        query.setPageSize(25L);
        assertThat(query.getPageNum()).isEqualTo(4L);
        assertThat(query.getPageSize()).isEqualTo(25L);
    }

    @Test
    void productQuerySupportsLegacyNameAndTypeAliases() {
        ProductQueryDTO query = new ProductQueryDTO();
        query.setProductName("幼犬");
        query.setType("活体宠物");
        assertThat(query.getProductName()).isEqualTo("幼犬");
        assertThat(query.getType()).isEqualTo("活体宠物");
    }

    @Test
    void sharedStatusDisplayKeepsStoreVocabulary() {
        assertThat(StatusDisplayUtil.storeStatus(0)).isEqualTo("待审核");
        assertThat(StatusDisplayUtil.storeStatus(1)).isEqualTo("营业中");
        assertThat(StatusDisplayUtil.storeStatus(2)).isEqualTo("已关闭");
        assertThat(StatusDisplayUtil.storeStatus(3)).isEqualTo("审核驳回");
    }

    @Test
    void sharedStatusDisplayKeepsProductVocabulary() {
        assertThat(StatusDisplayUtil.productStatus(0)).isEqualTo("下架");
        assertThat(StatusDisplayUtil.productStatus(1)).isEqualTo("上架");
        assertThat(StatusDisplayUtil.productStatus(2)).isEqualTo("已售出");
    }

    @Test
    void productMapperCanDetectHistoricalOrders() throws Exception {
        String sql = selectSql(ProductMapper.class, "countOrderItemsByProductId", Long.class);
        assertThat(sql).contains("order_item", "product_id");
    }

    @Test
    void nearbyStoreQueryOnlyReturnsPublicOpenStores() throws Exception {
        String sql = selectSql(StoreMapper.class, "searchNearbyPage", Double.class, Double.class,
                Double.class, String.class, String.class, Long.class, Long.class);
        assertThat(sql).contains("s.status = 1", "s.deleted = 0");
    }

    @Test
    void nearbyStoreQueryClampsDistanceAndIncludesRadiusBoundary() throws Exception {
        String sql = selectSql(StoreMapper.class, "searchNearbyPage", Double.class, Double.class,
                Double.class, String.class, String.class, Long.class, Long.class);
        assertThat(sql).contains("LEAST(1", "GREATEST(-1", "distanceKm <= #{radiusKm}");
    }

    @Test
    void merchantOrderQueriesEnforceOwnershipAndWholeOrderIsolation() throws Exception {
        String ownedOrders = selectSql(OrderQueryMapper.class, "selectOrderIdsByMerchantUserId", Long.class);
        String outsideItems = selectSql(OrderQueryMapper.class, "countItemsOutsideMerchant", Long.class, Long.class);
        assertThat(ownedOrders).contains("store", "s.user_id");
        assertThat(outsideItems).contains("s.user_id IS NULL", "s.user_id !=");
    }

    @Test
    void merchantVideoUsesDedicatedApiRoute() {
        RequestMapping mapping = MerchantVideoController.class.getAnnotation(RequestMapping.class);
        assertThat(mapping.value()).containsExactly("/api/merchant/video");
    }

    @Test
    void logisticsAndVideoColumnContractsRemainPersistable() throws Exception {
        PurchaseOrder order = new PurchaseOrder();
        order.setLogisticsCarrier("顺丰速运");
        order.setLogisticsNo("SF-001");
        assertThat(order.getLogisticsCarrier()).isEqualTo("顺丰速运");
        assertThat(order.getLogisticsNo()).isEqualTo("SF-001");
        assertThat(Video.class.getDeclaredField("url").getAnnotation(TableField.class).value())
                .isEqualTo("video_url");
        assertThat(Video.class.getDeclaredField("cover").getAnnotation(TableField.class).value())
                .isEqualTo("cover_url");
    }

    private static String selectSql(Class<?> mapper, String method, Class<?>... parameterTypes)
            throws Exception {
        Select select = mapper.getMethod(method, parameterTypes).getAnnotation(Select.class);
        return String.join(" ", select.value());
    }
}
