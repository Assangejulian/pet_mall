package com.pat.payment.service;

import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.common.domain.ErrorCode;
import com.pat.common.exception.BusinessException;
import com.pat.order.domain.dto.PayNotifyDTO;
import com.pat.order.domain.entity.OrderItem;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.domain.enums.OrderStatus;
import com.pat.order.domain.vo.OrderPaymentVO;
import com.pat.order.helper.OrderStateMachine;
import com.pat.order.mapper.OrderItemMapper;
import com.pat.order.service.base.PurchaseOrderBaseService;
import com.pat.product.service.ProductService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service("mockPaymentService")
public class MockPaymentService implements PaymentService {

    private final PurchaseOrderBaseService baseService;
    private final OrderItemMapper orderItemMapper;
    private final ProductService productService;

    public MockPaymentService(PurchaseOrderBaseService baseService,
                              OrderItemMapper orderItemMapper,
                              ProductService productService) {
        this.baseService = baseService;
        this.orderItemMapper = orderItemMapper;
        this.productService = productService;
    }

    @Override
    public OrderPaymentVO pay(PurchaseOrder order) {
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.PAID.getCode());

        // 扣减库存（原子操作，行级锁+库存足量校验）
        List<OrderItem> items = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", order.getId()));
        for (OrderItem item : items) {
            boolean ok = productService.deductStock(item.getProductId(), item.getQuantity());
            if (!ok) {
                throw new BusinessException(500, "商品库存不足或已下架: " + item.getProductName(), null);
            }
        }

        order.setOrderStatus(OrderStatus.PAID.getCode());
        order.setPayTime(LocalDateTime.now());
        baseService.updateById(order);
        log.info("模拟支付成功 orderNo={}", order.getOrderNo());
        return new OrderPaymentVO(order.getId(), order.getOrderNo(),
                OrderStatus.PAID.getCode(), order.getPayAmount());
    }

    @Override
    public void handleNotify(PayNotifyDTO dto) {
        PurchaseOrder order = baseService.lambdaQuery()
                .eq(PurchaseOrder::getOrderNo, dto.getOutTradeNo())
                .one();
        if (order == null) {
            log.warn("支付回调：订单不存在 {}", dto.getOutTradeNo());
            return;
        }
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.PAID.getCode());
        order.setOrderStatus(OrderStatus.PAID.getCode());
        order.setPayTime(LocalDateTime.now());
        baseService.updateById(order);
        log.info("模拟支付回调成功 orderNo={}", dto.getOutTradeNo());
    }
}
