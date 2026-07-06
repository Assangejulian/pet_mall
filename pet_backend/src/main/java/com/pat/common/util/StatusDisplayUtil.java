package com.pat.common.util;

import com.pat.store.domain.enums.StoreStatus;
import com.pat.product.domain.enums.ProductStatus;
import com.pat.product.domain.enums.ProductType;

/**
 * 状态/类型 显示文本工具类，统一入口供全项目共享。
 * 实际数据定义在各业务包枚举中，本类仅做委托。
 */
public final class StatusDisplayUtil {

    private static final String UNKNOWN = "--";

    private StatusDisplayUtil() {}

    public static String storeStatus(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        try { return StoreStatus.of(code).getDesc(); }
        catch (Exception e) { return String.valueOf(code); }
    }

    public static String productStatus(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        try { return ProductStatus.of(code).getDesc(); }
        catch (Exception e) { return String.valueOf(code); }
    }

    public static String productType(Integer type) {
        if (type == null) {
            return UNKNOWN;
        }
        try { return ProductType.of(type).getDesc(); }
        catch (Exception e) { return String.valueOf(type); }
    }
}