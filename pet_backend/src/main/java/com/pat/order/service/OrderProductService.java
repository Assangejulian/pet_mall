package com.pat.order.service;

import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.order.domain.dto.OrderCreateDTO;
import com.pat.order.domain.entity.OrderItem;
import com.pat.product.domain.entity.Product;
import com.pat.product.service.ProductService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品校验与库存扣减。
 * <p>只负责：验证商品存在/上架、扣库存、构建 OrderItem。</p>
 */
@Service
public class OrderProductService {

    private final ProductService productService;

    public OrderProductService(ProductService productService) {
        this.productService = productService;
    }

    /**
     * 校验商品并扣减库存，返回 OrderItem 列表及总金额。
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
            boolean stockOk = productService.deductStock(product.getId(), item.getQuantity());
            if (!stockOk) {
                throw new BusinessException(500, "库存不足或已下架: " + product.getProductName(), null);
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
     * 恢复库存（取消订单时调用）。
     */
    public void restoreStock(List<OrderItem> items) {
        for (OrderItem item : items) {
            productService.restoreStock(item.getProductId(), item.getQuantity());
        }
    }

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
