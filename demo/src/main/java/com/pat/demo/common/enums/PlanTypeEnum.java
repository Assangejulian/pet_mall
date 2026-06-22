package com.pat.demo.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 计划类型枚举
 * 水产养殖管理系统专用枚举
 */
@Getter
@AllArgsConstructor
public enum PlanTypeEnum {
    SEEDING(1, "放苗"),
    FEEDING(2, "投喂"),
    MEDICINE(3, "用药"),
    WATER_CHANGE(4, "换水"),
    HARVEST(5, "收获"),
    DEEP_SEA(6, "深远海作业");

    @EnumValue
    private final int code;
    private final String description;
}