package com.pat.store.domain.enums;

import com.pat.common.exception.BusinessException;
import com.pat.common.domain.ErrorCode;
import lombok.Getter;

@Getter
public enum StoreStatus {

    PENDING(0, "待审核"),
    OPEN(1, "营业中"),
    CLOSED(2, "已关闭"),
    REJECTED(3, "审核驳回");

    private final int code;
    private final String desc;

    StoreStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static StoreStatus of(Integer code) {
        if (code == null) {
            throw new BusinessException(ErrorCode.FARAMS_ERROR, "门店状态不能为空");
        }
        for (StoreStatus s : values()) {
            if (s.code == code) return s;
        }
        throw new BusinessException(ErrorCode.FARAMS_ERROR, "未知门店状态: " + code);
    }
}