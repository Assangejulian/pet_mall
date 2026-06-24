package com.pat.common.utils;

public class UserHolder {
    private static final ThreadLocal<java.util.Map<String, Object>> TL = new ThreadLocal<>();

    public static void save(String key, Object value) {
        var map = TL.get();
        if (map == null) { map = new java.util.HashMap<>(); TL.set(map); }
        map.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        var map = TL.get();
        return map == null ? null : (T) map.get(key);
    }

    public static Long getUserId() { return get("userId"); }
    public static String getUsername() { return get("username"); }
    public static String getRole() { return get("role"); }

    public static void remove() { TL.remove(); }
}