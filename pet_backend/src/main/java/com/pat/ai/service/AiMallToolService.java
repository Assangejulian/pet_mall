package com.pat.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pat.order.domain.entity.Cart;
import com.pat.order.service.ICartService;
import com.pat.product.domain.dto.ProductQueryDTO;
import com.pat.product.domain.entity.Product;
import com.pat.product.domain.vo.ProductVO;
import com.pat.product.service.ProductService;
import com.pat.store.domain.entity.Store;
import com.pat.store.service.IStoreService;
import com.pat.video.domain.entity.Comment;
import com.pat.video.domain.entity.Video;
import com.pat.video.service.ICommentService;
import com.pat.video.service.IVideoService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiMallToolService {

    private final ProductService productService;
    private final IStoreService storeService;
    private final IVideoService videoService;
    private final ICommentService commentService;
    private final ICartService cartService;
    private final PendingAiActionService pendingActionService;
    private final ObjectMapper objectMapper;

    public AiMallToolService(ProductService productService,
                             IStoreService storeService,
                             IVideoService videoService,
                             ICommentService commentService,
                             ICartService cartService,
                             PendingAiActionService pendingActionService,
                             ObjectMapper objectMapper) {
        this.productService = productService;
        this.storeService = storeService;
        this.videoService = videoService;
        this.commentService = commentService;
        this.cartService = cartService;
        this.pendingActionService = pendingActionService;
        this.objectMapper = objectMapper;
    }

    public Object toolsFor(Long userId) {
        return new Tools(userId);
    }

    public class Tools {
        private final Long userId;

        private Tools(Long userId) {
            this.userId = userId;
        }

        @Tool(name = "search_products", value = "Search public products by keyword, category, product type, page and size.")
        public String searchProducts(@P(value = "Search keyword, such as cat food, kitten, toy", required = false) String keyword,
                                     @P(value = "Product category, such as cat, dog, food, care", required = false) String category,
                                     @P(value = "Product type: 1 for pets, 2 for supplies", required = false) Integer productType,
                                     @P(value = "Page number, default 1", required = false) Integer page,
                                     @P(value = "Page size, default 5", required = false) Integer size) {
            ProductQueryDTO query = new ProductQueryDTO();
            query.setKeyword(keyword);
            query.setCategory(category);
            query.setProductType(productType);
            query.setPage(page == null ? 1L : page.longValue());
            query.setSize(size == null ? 5L : Math.min(size.longValue(), 10L));
            IPage<ProductVO> result = productService.pagePublicProducts(query);
            return json(Map.of(
                    "total", result.getTotal(),
                    "records", result.getRecords()
            ));
        }

        @Tool(name = "get_product_detail", value = "Get public product detail by product id.")
        public String getProductDetail(@P("Product id") Long productId) {
            return json(productService.getPublicDetail(productId));
        }

        @Tool(name = "search_stores", value = "Search open stores by keyword and city.")
        public String searchStores(@P(value = "Store keyword", required = false) String keyword,
                                   @P(value = "City name", required = false) String city,
                                   @P(value = "Page number, default 1", required = false) Integer page,
                                   @P(value = "Page size, default 5", required = false) Integer size) {
            QueryWrapper<Store> wrapper = new QueryWrapper<Store>()
                    .eq("status", 1)
                    .like(StringUtils.hasText(keyword), "store_name", keyword)
                    .eq(StringUtils.hasText(city), "city", city)
                    .orderByDesc("create_time");
            Page<Store> result = storeService.page(new Page<>(page == null ? 1 : page, size == null ? 5 : Math.min(size, 10)), wrapper);
            return json(Map.of(
                    "total", result.getTotal(),
                    "records", result.getRecords()
            ));
        }

        @Tool(name = "get_store_detail", value = "Get public store detail by store id.")
        public String getStoreDetail(@P("Store id") Long storeId) {
            Store store = storeService.getOne(new QueryWrapper<Store>()
                    .eq("id", storeId)
                    .eq("status", 1), false);
            return json(store == null ? Map.of("error", "store not found or closed") : store);
        }

        @Tool(name = "get_video_feed", value = "Get public video feed.")
        public String getVideoFeed(@P(value = "Page number, default 1", required = false) Integer page,
                                   @P(value = "Page size, default 5", required = false) Integer size) {
            Page<Video> result = videoService.page(
                    new Page<>(page == null ? 1 : page, size == null ? 5 : Math.min(size, 10)),
                    new QueryWrapper<Video>().eq("status", 1).orderByDesc("create_time")
            );
            return json(Map.of(
                    "total", result.getTotal(),
                    "records", result.getRecords()
            ));
        }

        @Tool(name = "get_video_detail", value = "Get video detail by video id.")
        public String getVideoDetail(@P("Video id") Long videoId) {
            return json(videoService.getById(videoId));
        }

        @Tool(name = "get_video_comments", value = "Get comments for a video.")
        public String getVideoComments(@P("Video id") Long videoId) {
            List<Comment> comments = commentService.list(new QueryWrapper<Comment>()
                    .eq("video_id", videoId)
                    .orderByDesc("create_time"));
            return json(comments);
        }

        @Tool(name = "list_cart", value = "List current user's cart items.")
        public String listCart() {
            if (userId == null) {
                return json(Map.of("error", "login required"));
            }
            List<Cart> carts = cartService.list(new QueryWrapper<Cart>()
                    .eq("user_id", userId)
                    .orderByDesc("create_time"));
            List<Map<String, Object>> records = new ArrayList<>();
            for (Cart cart : carts) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("cart", cart);
                Product product = productService.getById(cart.getProductId());
                item.put("product", product);
                records.add(item);
            }
            return json(records);
        }

        @Tool(name = "request_add_to_cart", value = "Prepare adding a product to cart. This only creates a pending action and requires user confirmation.")
        public String requestAddToCart(@P("Product id") Long productId,
                                       @P(value = "Quantity, default 1", required = false) Integer quantity) {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("productId", productId);
            payload.put("quantity", quantity == null ? 1 : quantity);
            return pending(PendingAiActionService.ADD_CART, "Add to cart",
                    "Add product " + productId + " x " + payload.get("quantity") + " to cart", payload);
        }

        @Tool(name = "request_update_cart", value = "Prepare updating a cart item. This only creates a pending action and requires user confirmation.")
        public String requestUpdateCart(@P("Cart item id") Long cartId,
                                        @P(value = "New quantity", required = false) Integer quantity,
                                        @P(value = "Checked state: 1 checked, 0 unchecked", required = false) Integer checked) {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("cartId", cartId);
            if (quantity != null) {
                payload.put("quantity", quantity);
            }
            if (checked != null) {
                payload.put("checked", checked);
            }
            return pending(PendingAiActionService.UPDATE_CART, "Update cart",
                    "Update cart item " + cartId, payload);
        }

        @Tool(name = "request_delete_cart", value = "Prepare deleting a cart item. This only creates a pending action and requires user confirmation.")
        public String requestDeleteCart(@P("Cart item id") Long cartId) {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("cartId", cartId);
            return pending(PendingAiActionService.DELETE_CART, "Delete cart item",
                    "Delete cart item " + cartId, payload);
        }

        @Tool(name = "request_create_order", value = "Prepare creating an order. This only creates a pending action and requires user confirmation. itemsJson must be a JSON array with productId and quantity.")
        public String requestCreateOrder(@P("Address id") Long addressId,
                                         @P("JSON array, e.g. [{\"productId\":1,\"quantity\":2}]") String itemsJson,
                                         @P(value = "Order remark, optional", required = false) String remark) {
            List<Map<String, Object>> items;
            try {
                items = objectMapper.readValue(itemsJson, new TypeReference<>() {});
            } catch (JsonProcessingException ex) {
                return json(Map.of("error", "itemsJson must be a valid JSON array"));
            }
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("addressId", addressId);
            payload.put("items", items);
            if (StringUtils.hasText(remark)) {
                payload.put("remark", remark);
            }
            return pending(PendingAiActionService.CREATE_ORDER, "Create order",
                    "Create order with " + items.size() + " item(s)", payload);
        }

        private String pending(String type, String label, String summary, Map<String, Object> payload) {
            return json(Map.of(
                    "requiresConfirmation", true,
                    "pendingAction", pendingActionService.register(userId, type, label, summary, payload)
            ));
        }
    }

    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return "{\"error\":\"failed to serialize tool result\"}";
        }
    }
}
