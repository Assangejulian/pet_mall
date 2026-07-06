package com.pat.common.service;

/**
 * 库存预扣抽象接口 —— 方便切换防超卖策略。
 *
 * <p>当前实现：{@link RedisStockService}（Redis Lua 预扣 + DB 兜底）</p>
 * <p>如需切回纯 DB、或改成其他方案，只需新增一个实现类，无需改业务代码。</p>
 */
public interface StockDeductionService {

    /**
     * 预扣库存（在高并发下快速拦截超量请求）。
     *
     * @param productId 商品 ID
     * @param quantity  扣减数量
     * @return true 扣减成功；false 库存不足
     */
    boolean preDeduct(Long productId, Integer quantity);

    /**
     * 归还预扣的库存（取消订单、下单失败时调用）。
     *
     * @param productId 商品 ID
     * @param quantity  归还数量
     */
    void restore(Long productId, Integer quantity);

    /**
     * 与 DB 同步库存（商品创建/编辑/上架时调用）。
     *
     * @param productId 商品 ID
     * @param stock     最新库存量
     */
    void syncStock(Long productId, Integer stock);

    /**
     * 移除库存缓存（商品下架/删除时调用）。
     *
     * @param productId 商品 ID
     */
    void removeStock(Long productId);
}
