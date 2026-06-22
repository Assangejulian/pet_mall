package com.pat.demo.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 塘口状态枚举
 * 水产养殖管理系统专用枚举
 */
@Getter
@AllArgsConstructor
public enum PondStatusEnum {
    ENABLED(1, "启用"),
    DISABLED(0, "禁用"),
    MAINTENANCE(2, "维护中");

    @EnumValue
    private final int code;
    private final String description;
}