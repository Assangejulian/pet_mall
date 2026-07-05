package com.pat.common.util;

/**
 * 状态/类型 显示文本工具类。
 * 将数字编码转换为中文描述，避免各模块重复定义。
 */
public final class StatusDisplayUtil {

    private static final String UNKNOWN = "--";

    private StatusDisplayUtil() {}

    /** 门店状态：0-待审核，1-营业中，2-已关闭，3-审核驳回 */
    public static String storeStatus(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        return switch (code) {
            case 0 -> "待审核";
            case 1 -> "营业中";
            case 2 -> "已关闭";
            case 3 -> "审核驳回";
            default -> String.valueOf(code);
        };
    }

    /** 商品状态：0-下架，1-上架，2-已售出 */
    public static String productStatus(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        return switch (code) {
            case 0 -> "下架";
            case 1 -> "上架";
            case 2 -> "已售出";
            default -> String.valueOf(code);
        };
    }

    /** 商品类型：1-活体宠物，2-宠物用品/周边 */
    public static String productType(Integer type) {
        if (type == null) {
            return UNKNOWN;
        }
        return switch (type) {
            case 1 -> "活体宠物";
            case 2 -> "宠物用品/周边";
            default -> String.valueOf(type);
        };
    }
}
