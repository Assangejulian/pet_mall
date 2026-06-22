package com.pat.demo.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 报警状态枚举
 * 水产养殖管理系统专用枚举
 */
@Getter
@AllArgsConstructor
public enum AlarmStatusEnum {
    NORMAL("normal", "正常"),
    WARNING("warning", "警告"),
    ALARM("alarm", "报警");

    @EnumValue
    private final String code;
    private final String description;
}