package com.pat.order;

import com.pat.common.exception.BusinessException;
import com.pat.common.util.UserHolder;
import com.pat.order.domain.dto.OrderCreateDTO;
import com.pat.order.domain.dto.OrderShipDTO;
import com.pat.order.domain.entity.OrderItem;
import com.pat.order.domain.entity.Cart;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.mapper.OrderItemMapper;
import com.pat.order.mapper.OrderQueryMapper;
import com.pat.order.service.ICartService;
import com.pat.order.service.base.PurchaseOrderBaseService;
import com.pat.order.service.impl.OrderQueryServiceImpl;
import com.pat.order.service.impl.OrderShipServiceImpl;
import com.pat.order.service.impl.OrderUserServiceImpl;
import com.pat.payment.service.impl.PaymentServiceRouter;
import com.pat.product.domain.entity.Product;
import com.pat.product.service.ProductService;
import com.pat.store.domain.entity.Store;
import com.pat.store.service.IStoreService;
import com.pat.user.domain.entity.UserAddress;
import com.pat.user.mapper.UserAddressMapper;
import com.pat.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderOwnershipIntegrityTest {

    @AfterEach
    void cleanup() {
        UserHolder.remove();
    }

    @Test
    void singleMerchantSeesOnlyItsItemsAndOtherMerchantCannotReadDetail() {
        PurchaseOrderBaseService base = mock(PurchaseOrderBaseService.class);
        OrderQueryMapper mapper = mock(OrderQueryMapper.class);
        PurchaseOrder order = paidOrder(100L);
        OrderItem ownItem = item(1L, 100L, 501L);
        when(base.getById(100L)).thenReturn(order);
        when(mapper.selectOrderIdsByMerchantUserId(11L)).thenReturn(List.of(100L));
        when(mapper.selectMerchantItems(List.of(100L), 11L)).thenReturn(List.of(ownItem));
        when(mapper.selectOrderIdsByMerchantUserId(22L)).thenReturn(List.of());
        OrderQueryServiceImpl service = new OrderQueryServiceImpl(base, mapper);

        Map<String, Object> detail = service.getDetail(100L, 11L);

        assertThat(detail.get("items")).isEqualTo(List.of(ownItem));
        assertThatThrownBy(() -> service.getDetail(100L, 22L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void sameMerchantLegacyMultiStoreOrderShipsAsOneWholeOrder() {
        PurchaseOrderBaseService base = mock(PurchaseOrderBaseService.class);
        OrderQueryMapper mapper = mock(OrderQueryMapper.class);
        PurchaseOrder order = paidOrder(200L);
        when(base.getById(200L)).thenReturn(order);
        when(mapper.selectOrderIdsByMerchantUserId(11L)).thenReturn(List.of(200L));
        when(mapper.countItemsOutsideMerchant(200L, 11L)).thenReturn(0L);
        OrderShipServiceImpl service = new OrderShipServiceImpl(base, mapper);

        service.shipOrder(ship(200L), 11L);

        ArgumentCaptor<PurchaseOrder> captor = ArgumentCaptor.forClass(PurchaseOrder.class);
        verify(base).updateById(captor.capture());
        assertThat(captor.getValue().getOrderStatus()).isEqualTo(2);
        assertThat(captor.getValue().getShipTime()).isNotNull();
        assertThat(captor.getValue().getLogisticsCarrier()).isEqualTo("test-only");
        assertThat(captor.getValue().getLogisticsNo()).isEqualTo("PHASE2-NO");
    }

    @Test
    void shippingPersistsCarrierNumberAndServerGeneratedShipTime() {
        PurchaseOrderBaseService base = mock(PurchaseOrderBaseService.class);
        OrderQueryMapper mapper = mock(OrderQueryMapper.class);
        PurchaseOrder order = paidOrder(210L);
        when(base.getById(210L)).thenReturn(order);
        when(mapper.selectOrderIdsByMerchantUserId(11L)).thenReturn(List.of(210L));
        when(mapper.countItemsOutsideMerchant(210L, 11L)).thenReturn(0L);
        OrderShipServiceImpl service = new OrderShipServiceImpl(base, mapper);
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        service.shipOrder(ship(210L, "  顺丰速运  ", "  SF20260703001  "), 11L);

        ArgumentCaptor<PurchaseOrder> captor = ArgumentCaptor.forClass(PurchaseOrder.class);
        verify(base).updateById(captor.capture());
        PurchaseOrder saved = captor.getValue();
        assertThat(saved.getOrderStatus()).isEqualTo(2);
        assertThat(saved.getLogisticsCarrier()).isEqualTo("顺丰速运");
        assertThat(saved.getLogisticsNo()).isEqualTo("SF20260703001");
        assertThat(saved.getShipTime()).isAfterOrEqualTo(before);
    }

    @Test
    void blankOrOversizedLogisticsFieldsAreRejectedBeforeUpdatingOrder() {
        OrderShipServiceImpl service = new OrderShipServiceImpl(
                mock(PurchaseOrderBaseService.class), mock(OrderQueryMapper.class));

        assertThatThrownBy(() -> service.shipOrder(ship(211L, " ", "SF001"), 11L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("物流公司不能为空");
        assertThatThrownBy(() -> service.shipOrder(ship(211L, "顺丰速运", "  "), 11L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("物流单号不能为空");
        assertThatThrownBy(() -> service.shipOrder(ship(211L, "顺丰速运", "A".repeat(101)), 11L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("物流单号长度不能超过100");
    }

    @Test
    void unpaidCancelledAndCompletedOrdersCannotBeShipped() {
        PurchaseOrderBaseService base = mock(PurchaseOrderBaseService.class);
        OrderShipServiceImpl service = new OrderShipServiceImpl(base, mock(OrderQueryMapper.class));
        when(base.getById(220L)).thenReturn(orderWithStatus(220L, 0));
        when(base.getById(221L)).thenReturn(orderWithStatus(221L, -1));
        when(base.getById(222L)).thenReturn(orderWithStatus(222L, 3));

        assertThatThrownBy(() -> service.shipOrder(ship(220L), null)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.shipOrder(ship(221L), null)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.shipOrder(ship(222L), null)).isInstanceOf(BusinessException.class);
        verify(base, never()).updateById(any(PurchaseOrder.class));
    }

    @Test
    void merchantOrderDetailReadsPersistedLogisticsFields() {
        PurchaseOrderBaseService base = mock(PurchaseOrderBaseService.class);
        OrderQueryMapper mapper = mock(OrderQueryMapper.class);
        PurchaseOrder order = paidOrder(230L);
        order.setLogisticsCarrier("京东物流");
        order.setLogisticsNo("JD20260703001");
        when(base.getById(230L)).thenReturn(order);
        when(mapper.selectOrderIdsByMerchantUserId(11L)).thenReturn(List.of(230L));
        when(mapper.selectMerchantItems(List.of(230L), 11L)).thenReturn(List.of(item(1L, 230L, 501L)));
        OrderQueryServiceImpl service = new OrderQueryServiceImpl(base, mapper);

        Map<String, Object> detail = service.getDetail(230L, 11L);

        assertThat(detail.get("logisticsCarrier")).isEqualTo("京东物流");
        assertThat(detail.get("logisticsNo")).isEqualTo("JD20260703001");
    }

    @Test
    void userOrderDetailReadsAllowedLogisticsFields() {
        PurchaseOrderBaseService base = mock(PurchaseOrderBaseService.class);
        ProductService products = mock(ProductService.class);
        UserAddressMapper addresses = mock(UserAddressMapper.class);
        OrderItemMapper items = mock(OrderItemMapper.class);
        PurchaseOrder order = paidOrder(240L);
        order.setUserId(11L);
        order.setLogisticsCarrier("中通快递");
        order.setLogisticsNo("ZT20260703001");
        when(base.getById(240L)).thenReturn(order);
        when(items.selectList(any())).thenReturn(List.of(item(1L, 240L, 501L)));
        UserHolder.save("userId", 11L);
        OrderUserServiceImpl service = userOrderService(base, products, addresses, items);

        PurchaseOrder detail = service.getUserOrderDetail(240L);

        assertThat(detail.getLogisticsCarrier()).isEqualTo("中通快递");
        assertThat(detail.getLogisticsNo()).isEqualTo("ZT20260703001");
        assertThat(detail.getItems()).hasSize(1);
    }

    @Test
    void crossMerchantOrderDetailIsFilteredPerMerchantAndWholeOrderShippingIsRejected() {
        PurchaseOrderBaseService base = mock(PurchaseOrderBaseService.class);
        OrderQueryMapper mapper = mock(OrderQueryMapper.class);
        PurchaseOrder order = paidOrder(300L);
        OrderItem first = item(1L, 300L, 501L);
        OrderItem second = item(2L, 300L, 502L);
        when(base.getById(300L)).thenReturn(order);
        when(mapper.selectOrderIdsByMerchantUserId(11L)).thenReturn(List.of(300L));
        when(mapper.selectOrderIdsByMerchantUserId(22L)).thenReturn(List.of(300L));
        when(mapper.selectMerchantItems(List.of(300L), 11L)).thenReturn(List.of(first));
        when(mapper.selectMerchantItems(List.of(300L), 22L)).thenReturn(List.of(second));
        when(mapper.countItemsOutsideMerchant(300L, 11L)).thenReturn(1L);
        OrderQueryServiceImpl query = new OrderQueryServiceImpl(base, mapper);
        OrderShipServiceImpl ship = new OrderShipServiceImpl(base, mapper);

        assertThat(query.getDetail(300L, 11L).get("items")).isEqualTo(List.of(first));
        assertThat(query.getDetail(300L, 22L).get("items")).isEqualTo(List.of(second));
        assertThatThrownBy(() -> ship.shipOrder(ship(300L), 11L))
                .isInstanceOf(BusinessException.class);
        verify(base, never()).updateById(any(PurchaseOrder.class));
    }

    @Test
    void merchantWithoutAnyStoreCannotReadOrShipAnotherOrder() {
        PurchaseOrderBaseService base = mock(PurchaseOrderBaseService.class);
        OrderQueryMapper mapper = mock(OrderQueryMapper.class);
        when(base.getById(400L)).thenReturn(paidOrder(400L));
        when(mapper.selectOrderIdsByMerchantUserId(99L)).thenReturn(List.of());
        OrderQueryServiceImpl query = new OrderQueryServiceImpl(base, mapper);
        OrderShipServiceImpl ship = new OrderShipServiceImpl(base, mapper);

        assertThatThrownBy(() -> query.getDetail(400L, 99L)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> ship.shipOrder(ship(400L), 99L)).isInstanceOf(BusinessException.class);
        verify(base, never()).updateById(any(PurchaseOrder.class));
    }

    @Test
    void deletedProductOrStoreDoesNotEraseOwnershipBecauseMapperUsesRawOwnerJoin() throws Exception {
        String orderSql = sqlOf("selectOrderIdsByMerchantUserId", Long.class);
        String itemSql = sqlOf("selectMerchantItems", List.class, Long.class);

        assertThat(orderSql).contains("INNER JOIN product", "INNER JOIN store", "s.user_id")
                .doesNotContain("deleted = 0");
        assertThat(itemSql).contains("INNER JOIN product", "INNER JOIN store", "s.user_id")
                .doesNotContain("deleted = 0");
    }

    @Test
    void newOrderWithProductsFromDifferentStoresIsRejectedBeforeInventoryChanges() {
        PurchaseOrderBaseService base = mock(PurchaseOrderBaseService.class);
        ProductService products = mock(ProductService.class);
        UserAddressMapper addresses = mock(UserAddressMapper.class);
        when(addresses.selectById(9L)).thenReturn(address(9L, 11L));
        when(products.getById(501L)).thenReturn(product(501L, 1L));
        when(products.getById(502L)).thenReturn(product(502L, 2L));
        OrderUserServiceImpl service = userOrderService(base, products, addresses, mock(OrderItemMapper.class));
        UserHolder.save("userId", 11L);

        assertThatThrownBy(() -> service.createOrder(createDto(501L, 502L)))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getDescription()).contains("同一门店"));
        verify(products, never()).deductStock(any(), any());
        verify(base, never()).save(any(PurchaseOrder.class));
    }

    @Test
    void newOrderWithProductsFromOneStoreIsCreatedNormally() {
        PurchaseOrderBaseService base = mock(PurchaseOrderBaseService.class);
        ProductService products = mock(ProductService.class);
        UserAddressMapper addresses = mock(UserAddressMapper.class);
        OrderItemMapper items = mock(OrderItemMapper.class);
        when(addresses.selectById(9L)).thenReturn(address(9L, 11L));
        when(products.getById(501L)).thenReturn(product(501L, 1L));
        when(products.getById(502L)).thenReturn(product(502L, 1L));
        when(products.deductStock(any(), any())).thenReturn(true);
        doAnswer(invocation -> {
            PurchaseOrder order = invocation.getArgument(0);
            order.setId(500L);
            return true;
        }).when(base).save(any(PurchaseOrder.class));
        OrderUserServiceImpl service = userOrderService(base, products, addresses, items);
        UserHolder.save("userId", 11L);

        Long orderId = service.createOrder(createDto(501L, 502L));

        assertThat(orderId).isEqualTo(500L);
        verify(products).deductStock(501L, 1);
        verify(products).deductStock(502L, 1);
        verify(items, org.mockito.Mockito.times(2)).insert(any(OrderItem.class));
    }

    @Test
    void pendingStoreProductCannotBeOrderedByDirectCreateCall() {
        assertOrderRejectedForStore(storeWithStatus(1L, 0, 0), "未营业");
    }

    @Test
    void closedStoreProductCannotBeOrderedByDirectCreateCall() {
        assertOrderRejectedForStore(storeWithStatus(1L, 2, 0), "未营业");
    }

    @Test
    void rejectedStoreProductCannotBeOrderedByDirectCreateCall() {
        assertOrderRejectedForStore(storeWithStatus(1L, 3, 0), "未营业");
    }

    @Test
    void deletedStoreProductCannotBeOrderedByDirectCreateCall() {
        assertOrderRejectedForStore(storeWithStatus(1L, 1, 1), "已删除");
    }

    @Test
    void missingStoreProductCannotBeOrderedByDirectCreateCall() {
        assertOrderRejectedForStore(null, "门店不存在");
    }

    @Test
    void offlineProductStillCannotBeOrderedBeforeStoreCheck() {
        PurchaseOrderBaseService base = mock(PurchaseOrderBaseService.class);
        ProductService products = mock(ProductService.class);
        UserAddressMapper addresses = mock(UserAddressMapper.class);
        IStoreService stores = mock(IStoreService.class);
        Product offline = product(501L, 1L);
        offline.setStatus(0);
        when(addresses.selectById(9L)).thenReturn(address(9L, 11L));
        when(products.getById(501L)).thenReturn(offline);
        OrderUserServiceImpl service = userOrderService(base, products, stores, addresses, mock(OrderItemMapper.class));
        UserHolder.save("userId", 11L);

        assertThatThrownBy(() -> service.createOrder(createDto(501L)))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getDescription()).contains("已下架"));
        verify(stores, never()).getById(any());
        verify(products, never()).deductStock(any(), any());
        verify(base, never()).save(any(PurchaseOrder.class));
    }

    private static String sqlOf(String method, Class<?>... parameterTypes) throws Exception {
        org.apache.ibatis.annotations.Select select = OrderQueryMapper.class
                .getMethod(method, parameterTypes).getAnnotation(org.apache.ibatis.annotations.Select.class);
        return String.join(" ", select.value());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static OrderUserServiceImpl userOrderService(PurchaseOrderBaseService base,
                                                         ProductService products,
                                                         UserAddressMapper addresses,
                                                         OrderItemMapper items) {
        IStoreService stores = mock(IStoreService.class);
        when(stores.getById(any())).thenAnswer(invocation -> store(invocation.getArgument(0), 11L));
        return userOrderService(base, products, stores, addresses, items);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static OrderUserServiceImpl userOrderService(PurchaseOrderBaseService base,
                                                         ProductService products,
                                                         IStoreService stores,
                                                         UserAddressMapper addresses,
                                                         OrderItemMapper items) {
        ICartService cart = mock(ICartService.class);
        LambdaUpdateChainWrapper chain = mock(LambdaUpdateChainWrapper.class);
        when(cart.lambdaUpdate()).thenReturn(chain);
        when(chain.eq(any(), any())).thenReturn(chain);
        when(chain.in(any(), any(java.util.Collection.class))).thenReturn(chain);
        return new OrderUserServiceImpl(base, products, stores, addresses, cart, items,
                mock(PaymentServiceRouter.class), mock(UserService.class));
    }

    private static void assertOrderRejectedForStore(Store store, String messagePart) {
        PurchaseOrderBaseService base = mock(PurchaseOrderBaseService.class);
        ProductService products = mock(ProductService.class);
        UserAddressMapper addresses = mock(UserAddressMapper.class);
        IStoreService stores = mock(IStoreService.class);
        when(addresses.selectById(9L)).thenReturn(address(9L, 11L));
        when(products.getById(501L)).thenReturn(product(501L, 1L));
        when(stores.getById(1L)).thenReturn(store);
        OrderUserServiceImpl service = userOrderService(base, products, stores, addresses, mock(OrderItemMapper.class));
        UserHolder.save("userId", 11L);

        assertThatThrownBy(() -> service.createOrder(createDto(501L)))
                .isInstanceOfSatisfying(BusinessException.class,
                        ex -> assertThat(ex.getDescription()).contains(messagePart));
        verify(products, never()).deductStock(any(), any());
        verify(base, never()).save(any(PurchaseOrder.class));
    }

    private static OrderCreateDTO createDto(Long... productIds) {
        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setAddressId(9L);
        dto.setItems(java.util.Arrays.stream(productIds).map(id -> {
            OrderCreateDTO.OrderItemDTO item = new OrderCreateDTO.OrderItemDTO();
            item.setProductId(id);
            item.setQuantity(1);
            return item;
        }).toList());
        return dto;
    }

    private static UserAddress address(Long id, Long userId) {
        UserAddress address = new UserAddress();
        address.setId(id);
        address.setUserId(userId);
        address.setReceiverName("Phase2");
        return address;
    }

    private static Product product(Long id, Long storeId) {
        Product product = new Product();
        product.setId(id);
        product.setStoreId(storeId);
        product.setProductName("Phase2-" + id);
        product.setPrice(BigDecimal.TEN);
        product.setStatus(1);
        return product;
    }

    private static Store store(Long id, Long userId) {
        Store store = new Store();
        store.setId(id);
        store.setUserId(userId);
        store.setStatus(1);
        store.setDeleted(0);
        return store;
    }

    private static Store storeWithStatus(Long id, Integer status, Integer deleted) {
        Store store = store(id, 11L);
        store.setStatus(status);
        store.setDeleted(deleted);
        return store;
    }

    private static PurchaseOrder paidOrder(Long id) {
        return orderWithStatus(id, 1);
    }

    private static PurchaseOrder orderWithStatus(Long id, int status) {
        PurchaseOrder order = new PurchaseOrder();
        order.setId(id);
        order.setOrderNo("PHASE2-" + id);
        order.setOrderStatus(status);
        return order;
    }

    private static OrderItem item(Long id, Long orderId, Long productId) {
        OrderItem item = new OrderItem();
        item.setId(id);
        item.setOrderId(orderId);
        item.setProductId(productId);
        return item;
    }

    private static OrderShipDTO ship(Long orderId) {
        return ship(orderId, "test-only", "PHASE2-NO");
    }

    private static OrderShipDTO ship(Long orderId, String carrier, String logisticsNo) {
        OrderShipDTO dto = new OrderShipDTO();
        dto.setOrderId(orderId);
        dto.setCarrier(carrier);
        dto.setLogisticsNo(logisticsNo);
        return dto;
    }
}
