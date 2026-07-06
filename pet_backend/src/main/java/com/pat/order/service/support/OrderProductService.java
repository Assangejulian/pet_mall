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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    public OrderProductService(ProductService productService,
                               OrderItemMapper orderItemMapper,
                               StockDeductionService stockDeductionService) {
        this.productService = productService;
        this.orderItemMapper = orderItemMapper;
        this.stockDeductionService = stockDeductionService;
    }

    /**
     * 校验商品并扣除库存（Redis 预扣 -> DB 最终扣除）。
     */
    public ValidateResult validateAndDeduct(List<OrderCreateDTO.OrderItemDTO> items) {
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderCreateDTO.OrderItemDTO item : items) {
            Product product = productService.getById(item.getProductId());
            if (product == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在");
            }
            if (product.getStatus() == null || product.getStatus() != 1) {
                throw new BusinessException(ErrorCode.FARAMS_ERROR, "商品已下架: " + product.getProductName());
            }

            // Step 1: Redis 预扣库存（第一道防线）
            boolean redisOk = stockDeductionService.preDeduct(product.getId(), item.getQuantity());
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
            orderItems.add(oi);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return new ValidateResult(orderItems, total);
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