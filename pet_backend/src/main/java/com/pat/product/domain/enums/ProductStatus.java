package com.pat.product.domain.enums;

import com.pat.common.exception.BusinessException;
import com.pat.common.domain.ErrorCode;
import lombok.Getter;

@Getter
public enum ProductStatus {

    OFFLINE(0, "下架"),
    ONLINE(1, "上架"),
    SOLD(2, "已售出");

    private final int code;
    private final String desc;

    ProductStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ProductStatus of(Integer code) {
        if (code == null) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "商品状态不能为空");
        }
        for (ProductStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new BusinessException(ErrorCode.FARAMS_ERROR, "未知商品状态: " + code);
    }
}