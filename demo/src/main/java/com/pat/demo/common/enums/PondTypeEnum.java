package com.pat.demo.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 塘口类型枚举
 * 水产养殖管理系统专用枚举
 */
@Getter
@AllArgsConstructor
public enum PondTypeEnum {
    TRADITIONAL(1, "传统"),
    CAGE(2, "网箱"),
    VESSEL(3, "工船");

    @EnumValue
    private final int code;
    private final String description;
}