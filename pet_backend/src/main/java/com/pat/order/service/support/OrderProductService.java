package com.pat.order.service.support;

import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.order.domain.dto.OrderCreateDTO;
import com.pat.order.domain.entity.OrderItem;
import com.pat.order.mapper.OrderItemMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pat.product.domain.entity.Product;
import com.pat.product.service.ProductService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品校验与库存扣减。
 *
 * <p>负责验证商品是否存在/上架、扣减库存、以及取消订单时恢复库存。
 * 库存操作与订单流程紧密绑定，与 {@link com.pat.product.service.ProductService} 配合完成最终一致性。</p>
 */
@Service
public class OrderProductService {

    private final ProductService productService;
    private final OrderItemMapper orderItemMapper;

    public OrderProductService(ProductService productService, OrderItemMapper orderItemMapper) {
        this.productService = productService;
        this.orderItemMapper = orderItemMapper;
    }

    /**
     * 校验商品并扣减库存。
     *
     * <p>遍历下单商品列表，逐一校验商品存在性和上架状态，执行原子库存扣减。
     * 任一商品校验失败则抛出异常中断整个下单流程。</p>
     *
     * @param items 下单商品列表
     * @return 校验结果，包含 OrderItem 列表和原价合计
     * @throws BusinessException 商品不存在、已下架或库存不足时抛出
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
                throw new BusinessException(ErrorCode.FARAMS_ERROR, "商品已下架" + product.getProductName());
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
     * 恢复库存（取消订单、退款时调用）。
     *
     * @param items 订单明细列表
     */
    public void restoreStock(List<OrderItem> items) {
        for (OrderItem item : items) {
            productService.restoreStock(item.getProductId(), item.getQuantity());
        }
    }

    /**
     * 根据订单 ID 恢复库存（取消订单、退款时调用）。
     *
     * <p>内部自动查询订单明细，然后恢复商品库存，避免编排层直接依赖 OrderItemMapper。</p>
     *
     * @param orderId 订单 ID
     */
    public void restoreStockByOrderId(Long orderId) {
        List<OrderItem> items = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", orderId));
        restoreStock(items);
    }

    /** 商品校验结果。包含构建完成的 OrderItem 列表和原价合计金额。 */
    public static class ValidateResult {
        private final List<OrderItem> orderItems;
        private final BigDecimal total;

        public ValidateResult(List<OrderItem> orderItems, BigDecimal total) {
            this.orderItems = orderItems;
            this.total = total;
        }

        /** 用于落库的订单明细列表 */
        public List<OrderItem> getOrderItems() { return orderItems; }
        /** 商品原价合计金额 */
        public BigDecimal getTotal() { return total; }
    }
}
