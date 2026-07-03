package com.pat.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pat.ai.domain.vo.AiChatResponse;
import com.pat.common.domain.Result;
import com.pat.common.exception.BusinessException;
import com.pat.order.domain.entity.Cart;
import com.pat.order.domain.entity.OrderItem;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.service.ICartService;
import com.pat.order.service.IOrderItemService;
import com.pat.order.service.base.PurchaseOrderBaseService;
import com.pat.product.domain.entity.Product;
import com.pat.product.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class PendingAiActionService {

    public static final String ADD_CART = "ADD_CART";
    public static final String UPDATE_CART = "UPDATE_CART";
    public static final String DELETE_CART = "DELETE_CART";
    public static final String CREATE_ORDER = "CREATE_ORDER";

    private static final long EXPIRE_SECONDS = 15 * 60;
    private static final ThreadLocal<List<AiChatResponse.PendingAction>> CURRENT_ACTIONS =
            ThreadLocal.withInitial(ArrayList::new);

    private final Map<String, StoredAction> pendingActions = new ConcurrentHashMap<>();
    private final ICartService cartService;
    private final ProductService productService;
    private final PurchaseOrderBaseService orderService;
    private final IOrderItemService orderItemService;

    public PendingAiActionService(ICartService cartService,
                                  ProductService productService,
                                  PurchaseOrderBaseService orderService,
                                  IOrderItemService orderItemService) {
        this.cartService = cartService;
        this.productService = productService;
        this.orderService = orderService;
        this.orderItemService = orderItemService;
    }

    public void beginCollecting() {
        CURRENT_ACTIONS.set(new ArrayList<>());
    }

    public List<AiChatResponse.PendingAction> drainCollectedActions() {
        List<AiChatResponse.PendingAction> actions = new ArrayList<>(CURRENT_ACTIONS.get());
        CURRENT_ACTIONS.remove();
        return actions;
    }

    public AiChatResponse.PendingAction register(Long userId,
                                                 String type,
                                                 String label,
                                                 String summary,
                                                 Map<String, Object> payload) {
        if (userId == null) {
            throw new BusinessException(401, "Please login before using this action", null);
        }
        String id = UUID.randomUUID().toString().replace("-", "");
        AiChatResponse.PendingAction action = new AiChatResponse.PendingAction(id, type, label, summary, payload);
        pendingActions.put(id, new StoredAction(userId, action, Instant.now().plusSeconds(EXPIRE_SECONDS)));
        CURRENT_ACTIONS.get().add(action);
        return action;
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<?> confirm(Long userId, String actionId) {
        if (userId == null) {
            return Result.error(401, "Please login first");
        }
        StoredAction stored = pendingActions.remove(actionId);
        if (stored == null || stored.expiresAt().isBefore(Instant.now())) {
            return Result.error(410, "Action expired, please ask AI to generate it again");
        }
        if (!stored.userId().equals(userId)) {
            return Result.error(403, "Action does not belong to current user");
        }

        AiChatResponse.PendingAction action = stored.action();
        Map<String, Object> payload = asMap(action.getPayload());
        return switch (action.getType()) {
            case ADD_CART -> Result.success(addCart(userId, payload));
            case UPDATE_CART -> Result.success(updateCart(userId, payload));
            case DELETE_CART -> Result.success(deleteCart(userId, payload));
            case CREATE_ORDER -> Result.success(createOrder(userId, payload));
            default -> Result.error(400, "Unsupported action type: " + action.getType());
        };
    }

    private Cart addCart(Long userId, Map<String, Object> payload) {
        Long productId = longValue(payload.get("productId"));
        Integer quantity = intValue(payload.getOrDefault("quantity", 1));
        Product product = requireProduct(productId);

        Cart existing = cartService.getOne(new QueryWrapper<Cart>()
                .eq("user_id", userId)
                .eq("product_id", productId), false);
        if (existing != null) {
            existing.setQuantity((existing.getQuantity() == null ? 0 : existing.getQuantity()) + quantity);
            cartService.updateById(existing);
            return existing;
        }

        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setProductId(product.getId());
        cart.setQuantity(quantity);
        cart.setChecked(1);
        cartService.save(cart);
        return cart;
    }

    private Cart updateCart(Long userId, Map<String, Object> payload) {
        Long cartId = longValue(payload.get("cartId"));
        Cart cart = requireUserCart(userId, cartId);
        if (payload.containsKey("quantity")) {
            cart.setQuantity(intValue(payload.get("quantity")));
        }
        if (payload.containsKey("checked")) {
            cart.setChecked(intValue(payload.get("checked")));
        }
        cartService.updateById(cart);
        return cart;
    }

    private Boolean deleteCart(Long userId, Map<String, Object> payload) {
        Long cartId = longValue(payload.get("cartId"));
        requireUserCart(userId, cartId);
        return cartService.removeById(cartId);
    }

    @SuppressWarnings("unchecked")
    private Long createOrder(Long userId, Map<String, Object> payload) {
        Long addressId = longValue(payload.get("addressId"));
        List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");
        if (items == null || items.isEmpty()) {
            throw new BusinessException(400, "Order items cannot be empty", null);
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> item : items) {
            Long productId = longValue(item.get("productId"));
            Integer quantity = intValue(item.getOrDefault("quantity", 1));
            Product product = requireProduct(productId);
            if (product.getStatus() == null || product.getStatus() != 1) {
                throw new BusinessException(400, "Product is offline: " + product.getProductName(), null);
            }
            if (product.getStock() == null || product.getStock() < quantity) {
                throw new BusinessException(400, "Insufficient stock: " + product.getProductName(), null);
            }

            boolean stockOk = productService.deductStock(product.getId(), quantity);
            if (!stockOk) throw new BusinessException(400, "Insufficient stock: " + product.getProductName(), null);

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getProductName());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setPrice(product.getPrice());
            orderItem.setQuantity(quantity);
            orderItems.add(orderItem);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        }

        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNo(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + String.format("%08d", new Random().nextInt(100000000)));
        order.setUserId(userId);
        order.setAddressId(addressId);
        order.setTotalAmount(total);
        order.setPayAmount(total);
        order.setOrderStatus(0);
        orderService.save(order);

        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(order.getId());
        }
        orderItemService.saveBatch(orderItems);

        List<Long> productIds = items.stream()
                .map(item -> longValue(item.get("productId")))
                .collect(Collectors.toList());
        cartService.lambdaUpdate()
                .eq(Cart::getUserId, userId)
                .in(Cart::getProductId, productIds)
                .remove();

        return order.getId();
    }

    private Product requireProduct(Long productId) {
        Product product = productService.getById(productId);
        if (product == null) {
            throw new BusinessException(404, "Product not found: " + productId, null);
        }
        return product;
    }

    private Cart requireUserCart(Long userId, Long cartId) {
        Cart cart = cartService.getById(cartId);
        if (cart == null || !userId.equals(cart.getUserId())) {
            throw new BusinessException(404, "Cart item not found", null);
        }
        return cart;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object payload) {
        if (payload instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return new LinkedHashMap<>();
    }

    private static Long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    private static Integer intValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private record StoredAction(Long userId, AiChatResponse.PendingAction action, Instant expiresAt) {
    }
}
