package com.pat.demo.common.constant;

public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 30L;

    // 店铺缓存key前缀
    public static final String CACHE_SHOP_KEY = "cache:shop:";
    // 店铺缓存空值TTL（2分钟）
    public static final Long CACHE_NULL_TTL = 2L;
    // 店铺缓存TTL（30分钟）
    public static final Long CACHE_SHOP_TTL = 30L;
    // 店铺锁key前缀
    public static final String LOCK_SHOP_KEY = "lock:shop:";
    // 店铺锁超时时间（10秒）
    public static final Long LOCK_SHOP_TTL = 10L;
    // 店铺类型缓存key
    public static final String CACHE_SHOP_TYPE_KEY = "cache:shop:type";
    // 店铺类型缓存TTL
    public static final Long CACHE_SHOP_TYPE_TTL = 60L;

    // 秒杀券相关
    public static final String SECKILL_STOCK_KEY = "seckill:stock:"; // 后接voucherId
    public static final String SECKILL_ORDER_USER_KEY = "seckill:order:user:"; // 后接voucherId, 用于记录已下单用户Set
    public static final String SECKILL_ORDER_STREAM = "stream.orders"; // 可选：异步下单Stream
}