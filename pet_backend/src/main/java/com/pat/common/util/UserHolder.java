package com.pat.common.util;

public class
UserHolder {
    private static final ThreadLocal<java.util.Map<String, Object>> TL = new ThreadLocal<>();

    /**
     * 存入线程上下文。
     *
     * @param key   键
     * @param value 值
     */
    public static void save(String key, Object value) {
        var map = TL.get();
        if (map == null) { map = new java.util.HashMap<>(); TL.set(map); }
        map.put(key, value);
    }

    @SuppressWarnings("unchecked")
    /**
     * 从线程上下文取值。
     *
     * @param key 键
     * @param <T> 值类型
     * @return 值
     */
    public static <T> T get(String key) {
        var map = TL.get();
        return map == null ? null : (T) map.get(key);
    }

    /**
     * 获取当前用户 ID。
     *
     * @return 用户 ID，未登录返回 null
     */
    public static Long getUserId() { return get("userId"); }
    /**
     * 获取当前用户名。
     *
     * @return 用户名
     */
    public static String getUsername() { return get("username"); }
    /**
     * 获取当前用户角色。
     *
     * @return 角色标识
     */
    public static String getRole() { return get("role"); }

    /**
     * 清理线程上下文（请求结束后调用）。
     */
    public static void remove() { TL.remove(); }
}