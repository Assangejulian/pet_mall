package com.pat.common.service;

import com.pat.common.constant.RedisConstants;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * Redis 库存预扣服务 —— 高并发下防超卖第一道防线。
 *
 * <p>使用 Lua 脚本保证「查库存 → 扣库存」两步原子性，避免并发超扣。
 * DB 库存作为最终持久化依据，Redis 预扣只是流量缓冲层。</p>
 */
@Service
@ConditionalOnProperty(name = "stock.deduction", havingValue = "redis", matchIfMissing = true)
public class RedisStockService implements StockDeductionService {

    private final RedisTemplate<String, Object> redisTemplate;

    /** Lua 脚本：预扣库存，库存不足时自动回滚。返回剩余库存（< 0 表示不足）。 */
    private static final String LUA_DEDUCT =
            "local stock = redis.call('GET', KEYS[1])\n" +
            "if not stock then return -2 end\n" +
            "stock = tonumber(stock)\n" +
            "local qty = tonumber(ARGV[1])\n" +
            "if stock < qty then return -1 end\n" +
            "redis.call('DECRBY', KEYS[1], qty)\n" +
            "return stock - qty";

    /** Lua 脚本：归还库存。 */
    private static final String LUA_RESTORE =
            "redis.call('INCRBY', KEYS[1], ARGV[1])\n" +
            "return redis.call('GET', KEYS[1])";

    public RedisStockService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ==================== 公开 API ====================

    /**
     * 预扣库存（原子操作）。
     *
     * @param productId 商品 ID
     * @param quantity  扣减数量
     * @return true 扣减成功；false 库存不足或未初始化
     */
    public boolean preDeduct(Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            return false;
        }
        String key = stockKey(productId);
        Long result = redisTemplate.execute(
                new DefaultRedisScript<>(LUA_DEDUCT, Long.class),
                Collections.singletonList(key),
                String.valueOf(quantity));
        return result != null && result >= 0;
    }

    /**
     * 归还库存（取消订单/下单失败时调用）。
     *
     * @param productId 商品 ID
     * @param quantity  归还数量
     */
    public void restore(Long productId, Integer quantity) {
        String key = stockKey(productId);
        redisTemplate.execute(
                new DefaultRedisScript<>(LUA_RESTORE, Long.class),
                Collections.singletonList(key),
                String.valueOf(quantity));
    }

    /**
     * 将 DB 库存同步到 Redis（商品创建/编辑/上架时调用）。
     *
     * @param productId 商品 ID
     * @param stock     最新库存量
     */
    public void syncStock(Long productId, Integer stock) {
        byte[] key = stockKey(productId).getBytes(StandardCharsets.UTF_8);
        byte[] value = String.valueOf(stock == null ? 0 : stock).getBytes(StandardCharsets.UTF_8);
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            connection.stringCommands().set(key, value);
            return null;
        });
    }

    /**
     * 查询 Redis 中的当前库存。
     *
     * @param productId 商品 ID
     * @return 库存量，未初始化返回 null
     */
    public Integer getStock(Long productId) {
        byte[] key = stockKey(productId).getBytes(StandardCharsets.UTF_8);
        byte[] raw = redisTemplate.execute((RedisCallback<byte[]>) connection -> connection.stringCommands().get(key));
        if (raw != null) {
            try {
                return Integer.parseInt(new String(raw, StandardCharsets.UTF_8));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        Object val = redisTemplate.opsForValue().get(stockKey(productId));
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        if (val instanceof String str) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    /** 移除 Redis 库存缓存（商品下架/删除时调用）。 */
    public void removeStock(Long productId) {
        redisTemplate.delete(stockKey(productId));
    }

    // ==================== 内部方法 ====================

    private static String stockKey(Long productId) {
        return RedisConstants.SECKILL_STOCK_KEY + productId;
    }
}
