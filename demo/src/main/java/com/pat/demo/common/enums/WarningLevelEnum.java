package com.pat.demo.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 预警级别枚举
 * 水产养殖管理系统专用枚举
 */
@Getter
@AllArgsConstructor
public enum WarningLevelEnum {
    INFO("info", "信息"),
    WARNING("warning", "警告"),
    ERROR("error", "错误"),
    SUCCESS("success", "成功");

    @EnumValue
    private final String code;
    private final String description;
}