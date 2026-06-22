package com.pat.demo.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 任务状态枚举
 * 水产养殖管理系统专用枚举
 */
@Getter
@AllArgsConstructor
public enum TaskStatusEnum {
    PENDING(0, "待执行"),
    EXECUTED(1, "已执行"),
    NOT_EXECUTED(2, "未执行");

    @EnumValue
    private final int code;
    private final String description;
}