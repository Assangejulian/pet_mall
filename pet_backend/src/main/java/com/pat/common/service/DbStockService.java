package com.pat.common.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.pat.product.domain.entity.Product;
import com.pat.product.mapper.ProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 纯 DB 库存扣减实现 —— 基于 SQL 原子更新，无需 Redis。
 *
 * <p>使用 {@code UPDATE product SET stock = stock - ? WHERE id = ? AND stock >= ?}
 * 的原子性 + 行锁保证不超卖，适合中小并发场景。</p>
 *
 * <p>通过 {@code stock.deduction: db} 启用，默认由 RedisStockService 兜底。</p>
 *
 * @see StockDeductionService
 * @see RedisStockService
 */
@Service
@ConditionalOnProperty(name = "stock.deduction", havingValue = "db")
public class DbStockService implements StockDeductionService {

    private static final Logger log = LoggerFactory.getLogger(DbStockService.class);

    private final ProductMapper productMapper;

    public DbStockService(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    /**
     * 预扣库存 — 原子 SQL 扣减。
     *
     * {@code UPDATE product SET stock = stock - ? WHERE id = ? AND stock >= ?}
     */
    @Override
    public boolean preDeduct(Long productId, Integer quantity) {
        if (productId == null || quantity == null || quantity <= 0) {
            return false;
        }
        int rows = productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .setSql("stock = stock - {0}", quantity)
                .eq(Product::getId, productId)
                .ge(Product::getStock, quantity));
        if (rows > 0) {
            log.debug("DB 扣减库存 productId={}, quantity={}", productId, quantity);
            return true;
        }
        log.warn("DB 扣减库存失败（库存不足） productId={}, quantity={}", productId, quantity);
        return false;
    }

    /**
     * 归还库存 — 原子 SQL 恢复。
     *
     * {@code UPDATE product SET stock = stock + ? WHERE id = ?}
     */
    @Override
    public void restore(Long productId, Integer quantity) {
        if (productId == null || quantity == null || quantity <= 0) {
            return;
        }
        productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .setSql("stock = stock + {0}", quantity)
                .eq(Product::getId, productId));
        log.debug("DB 恢复库存 productId={}, quantity={}", productId, quantity);
    }

    /**
     * DB 方案无需预同步，空实现。
     */
    @Override
    public void syncStock(Long productId, Integer stock) {
        // DB 方案直接读表，无需同步
    }

    /**
     * 从数据库查询当前库存。
     *
     * @return 当前库存，商品不存在返回 null
     */
    @Override
    public Integer getStock(Long productId) {
        if (productId == null) return null;
        Product product = productMapper.selectById(productId);
        return product != null ? product.getStock() : null;
    }

    /**
     * DB 方案无需移除缓存，空实现。
     */
    @Override
    public void removeStock(Long productId) {
        // DB 方案无需缓存，无需移除
    }
}
