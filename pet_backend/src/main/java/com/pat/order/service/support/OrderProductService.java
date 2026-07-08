package com.pat.order.service.support;

import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.common.service.StockDeductionService;
import com.pat.order.domain.dto.OrderCreateDTO;
import com.pat.order.domain.entity.OrderItem;
import com.pat.order.mapper.OrderItemMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pat.product.domain.entity.Product;
import com.pat.product.service.ProductService;
import com.pat.store.domain.entity.Store;
import com.pat.store.service.IStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 商品校验与库存扣除。
 *
 * <p>负责验证商品是否存在/上架、Redis 预扣库存 + DB 最终扣除，
 * 以及取消订单时恢复库存（Redis + DB 双写）。</p>
 */
@Service
public class OrderProductService {

    private static final Logger log = LoggerFactory.getLogger(OrderProductService.class);

    private final ProductService productService;
    private final OrderItemMapper orderItemMapper;
    private final StockDeductionService stockDeductionService;
    private final IStoreService storeService;

    public OrderProductService(ProductService productService,
                               OrderItemMapper orderItemMapper,
                               StockDeductionService stockDeductionService,
                               IStoreService storeService) {
        this.productService = productService;
        this.orderItemMapper = orderItemMapper;
        this.stockDeductionService = stockDeductionService;
        this.storeService = storeService;
    }

    /**
     * 校验商品并扣除库存（Redis 预扣 -> DB 最终扣除）。
     */
    public ValidateResult validateAndDeduct(List<OrderCreateDTO.OrderItemDTO> items) {
        Long orderStoreId = validateItems(items);
        requireOperatingStore(orderStoreId);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (OrderCreateDTO.OrderItemDTO item : items) {
            OrderItem oi = deductForItem(item);
            orderItems.add(oi);
            total = total.add(oi.getPrice().multiply(BigDecimal.valueOf(oi.getQuantity())));
        }
        return new ValidateResult(orderItems, total);
    }

    private Long validateItems(List<OrderCreateDTO.OrderItemDTO> items) {
        Long orderStoreId = null;
        for (OrderCreateDTO.OrderItemDTO item : items) {
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BusinessException(ErrorCode.FARAMS_ERROR, "购买数量必须大于0");
            }
            Product product = productService.getById(item.getProductId());
            if (product == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在");
            }
            if (product.getStatus() == null || product.getStatus() != 1) {
                throw new BusinessException(ErrorCode.FARAMS_ERROR, "商品已下架: " + product.getProductName());
            }
            if (orderStoreId == null) {
                orderStoreId = product.getStoreId();
            } else if (!Objects.equals(orderStoreId, product.getStoreId())) {
                throw new BusinessException(ErrorCode.FARAMS_ERROR, "一张订单只能购买同一门店的商品，请分开结算");
            }
        }
        return orderStoreId;
    }

    private OrderItem deductForItem(OrderCreateDTO.OrderItemDTO item) {
        Product product = productService.getById(item.getProductId());

        Integer quantity = item.getQuantity();
        if (quantity == null || quantity <= 0) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "购买数量不合法: " + product.getProductName());
        }

        if (stockDeductionService.getStock(product.getId()) == null) {
            Integer dbStock = product.getStock();
            stockDeductionService.syncStock(product.getId(), dbStock != null ? dbStock : 0);
        }

        // Step 1: Redis 预扣库存（第一道防线）
        boolean redisOk = stockDeductionService.preDeduct(product.getId(), quantity);
        if (!redisOk) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "库存不足（售罄）: " + product.getProductName());
        }

        // Step 2: DB 最终扣除（第二道防线，持久化兜底）
        try {
            boolean dbOk = productService.deductStock(product.getId(), item.getQuantity());
            if (!dbOk) {
                // DB 扣减失败，归还 Redis 预扣
                stockDeductionService.restore(product.getId(), item.getQuantity());
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "库存不足或已下架: " + product.getProductName());
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            // 未知异常（如 DB 连接超时），归还 Redis 预扣
            stockDeductionService.restore(product.getId(), item.getQuantity());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下单失败，请重试: " + product.getProductName());
        }

        OrderItem oi = new OrderItem();
        oi.setProductId(product.getId());
        oi.setProductName(product.getProductName());
        oi.setProductImage(product.getMainImage());
        oi.setPrice(product.getPrice());
        oi.setQuantity(item.getQuantity());
        return oi;
    }
    /**
     * 恢复库存（取消订单、退款时调用）。
     * Redis 库存 + DB 库存同时恢复。
     */
    public void restoreStock(List<OrderItem> items) {
        for (OrderItem item : items) {
            stockDeductionService.restore(item.getProductId(), item.getQuantity());
            productService.restoreStock(item.getProductId(), item.getQuantity());
            log.info("恢复库存 productId={}, quantity={}", item.getProductId(), item.getQuantity());
        }
    }

    /**
     * 根据订单 ID 恢复库存（取消订单、退款时调用）。
     */
    public void restoreStockByOrderId(Long orderId) {
        List<OrderItem> items = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", orderId));
        restoreStock(items);
    }

    private void requireOperatingStore(Long storeId) {
        if (storeId == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品所属门店不存在");
        }
        Store store = storeService.getById(storeId);
        if (store == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品所属门店不存在");
        }
        if (store.getDeleted() != null && store.getDeleted() != 0) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "商品所属门店已删除，不能下单");
        }
        if (!Integer.valueOf(1).equals(store.getStatus())) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "商品所属门店未营业，不能下单");
        }
    }

    /** 商品校验结果。 */
    public static class ValidateResult {
        private final List<OrderItem> orderItems;
        private final BigDecimal total;

        public ValidateResult(List<OrderItem> orderItems, BigDecimal total) {
            this.orderItems = orderItems;
            this.total = total;
        }

        public List<OrderItem> getOrderItems() { return orderItems; }
        public BigDecimal getTotal() { return total; }
    }
}
