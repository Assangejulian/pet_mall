package com.pat.product.domain.enums;

import com.pat.common.exception.BusinessException;
import com.pat.common.domain.ErrorCode;
import lombok.Getter;

@Getter
public enum ProductType {

    PET(1, "活体宠物"),
    SUPPLIES(2, "宠物用品/周边");

    private final int code;
    private final String desc;

    ProductType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ProductType of(Integer code) {
        if (code == null) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "商品类型不能为空");
        }
        for (ProductType t : values()) {
            if (t.code == code) return t;
        }
        throw new BusinessException(ErrorCode.FARAMS_ERROR, "未知商品类型: " + code);
    }
}