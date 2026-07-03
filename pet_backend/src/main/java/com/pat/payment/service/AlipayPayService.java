package com.pat.payment.service;

import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayApiException;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.pat.order.domain.dto.PayNotifyDTO;
import com.pat.order.domain.entity.OrderItem;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.domain.enums.OrderStatus;
import com.pat.order.helper.OrderStateMachine;
import com.pat.order.mapper.OrderItemMapper;
import com.pat.order.service.base.PurchaseOrderBaseService;
import com.pat.payment.config.AlipayConfig;
import com.pat.product.service.ProductService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 支付宝支付业务服务。
 *
 * <p>提供三大核心接口：</p>
 * <ul>
 *   <li>{@link #createPayPage} — 电脑网站支付（alipay.trade.page.pay）</li>
 *   <li>{@link #createWapPayPage} — 手机网站支付（alipay.trade.wap.pay）</li>
 *   <li>{@link #queryTrade} — 交易查询兜底（alipay.trade.query）</li>
 * </ul>
 */
@Slf4j
@Service
public class AlipayPayService {

    private final AlipayClient alipayClient;
    private final AlipayConfig alipayConfig;
    private final PurchaseOrderBaseService baseService;
    private final OrderItemMapper orderItemMapper;
    private final ProductService productService;

    public AlipayPayService(AlipayClient alipayClient,
                            AlipayConfig alipayConfig,
                            PurchaseOrderBaseService baseService,
                            OrderItemMapper orderItemMapper,
                            ProductService productService) {
        this.alipayClient = alipayClient;
        this.alipayConfig = alipayConfig;
        this.baseService = baseService;
        this.orderItemMapper = orderItemMapper;
        this.productService = productService;
    }

    /**
     * 电脑网站支付 — 返回完整 HTML 表单，前端渲染后自动跳转支付宝。
     *
     * @param outTradeNo  本地订单号
     * @param subject     商品标题
     * @param totalAmount 支付金额（元）
     * @return 自动提交的 HTML 表单
     */
    public String createPayPage(String outTradeNo, String subject, String totalAmount) throws AlipayApiException {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayConfig.getNotifyUrl());
        request.setReturnUrl(alipayConfig.getReturnUrl());

        String biz = "{\"out_trade_no\":\"" + outTradeNo
                + "\",\"product_code\":\"FAST_INSTANT_TRADE_PAY\""
                + ",\"subject\":\"" + subject
                + "\",\"total_amount\":\"" + totalAmount + "\"}";
        request.setBizContent(biz);

        AlipayTradePagePayResponse resp = alipayClient.pageExecute(request);
        if (resp.isSuccess()) {
            return resp.getBody();
        }
        throw new RuntimeException("创建电脑支付订单失败: " + resp.getSubMsg());
    }

    /**
     * 手机网站支付 — 返回完整 HTML 表单，前端渲染后自动跳转支付宝。
     *
     * @param outTradeNo  本地订单号
     * @param subject     商品标题
     * @param body        商品描述（可选）
     * @param totalAmount 支付金额（元）
     * @return 自动提交的 HTML 表单
     */
    public String createWapPayPage(String outTradeNo, String subject, String body, String totalAmount) throws AlipayApiException {
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        request.setNotifyUrl(alipayConfig.getNotifyUrl());
        request.setReturnUrl(alipayConfig.getReturnUrl());

        String biz = "{\"out_trade_no\":\"" + outTradeNo
                + "\",\"product_code\":\"QUICK_WAP_WAY\""
                + ",\"subject\":\"" + subject
                + "\",\"body\":\"" + body
                + "\",\"total_amount\":\"" + totalAmount
                + "\",\"quit_url\":\"" + alipayConfig.getReturnUrl() + "\"}";
        request.setBizContent(biz);

        AlipayTradeWapPayResponse resp = alipayClient.pageExecute(request);
        if (resp.isSuccess()) {
            return resp.getBody();
        }
        throw new RuntimeException("创建手机支付订单失败: " + resp.getSubMsg());
    }

    /**
     * 交易查询（兜底）。定时任务轮询未支付订单，主动向支付宝查询真实状态。
     *
     * @param outTradeNo 本地订单号
     * @return 查询响应
     */
    public AlipayTradeQueryResponse queryTrade(String outTradeNo) throws AlipayApiException {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        String biz = "{\"out_trade_no\":\"" + outTradeNo + "\"}";
        request.setBizContent(biz);
        return alipayClient.execute(request);
    }

    /**
     * 处理支付宝异步通知。
     *
     * <p>验签已在 Controller 层完成，此方法幂等执行业务：</p>
     * <ol>
     *   <li>校验订单存在、状态合法</li>
     *   <li>扣减库存（原子操作，行级锁 + 足量校验）</li>
     *   <li>更新订单状态为已支付</li>
     * </ol>
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleNotify(PayNotifyDTO dto) {
        log.info("支付宝回调处理 orderNo={}", dto.getOutTradeNo());
        PurchaseOrder order = baseService.lambdaQuery()
                .eq(PurchaseOrder::getOrderNo, dto.getOutTradeNo())
                .one();
        if (order == null) {
            log.warn("支付宝回调：订单不存在 {}", dto.getOutTradeNo());
            return;
        }
        // 幂等：已支付的订单跳过，防止重复扣库存
        if (order.getOrderStatus() != null && order.getOrderStatus() == OrderStatus.PAID.getCode()) {
            log.info("支付宝回调：订单已支付，跳过重复处理 {}", dto.getOutTradeNo());
            return;
        }
        OrderStateMachine.validate(order.getOrderStatus(), OrderStatus.PAID.getCode());

        // 扣减库存（原子操作，行级锁 + 库存足量校验）
        List<OrderItem> items = orderItemMapper.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", order.getId()));
        for (OrderItem item : items) {
            boolean ok = productService.deductStock(item.getProductId(), item.getQuantity());
            if (!ok) {
                log.warn("支付宝回调：库存扣减失败 productId={}, orderNo={}", item.getProductId(), dto.getOutTradeNo());
            }
        }

        order.setOrderStatus(OrderStatus.PAID.getCode());
        order.setPayTime(LocalDateTime.now());
        baseService.updateById(order);
        log.info("支付宝支付成功 orderNo={}", dto.getOutTradeNo());
    }
}
