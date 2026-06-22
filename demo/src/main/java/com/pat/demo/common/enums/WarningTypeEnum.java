package com.pat.demo.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 预警类型枚举
 * 水产养殖管理系统专用枚举
 */
@Getter
@AllArgsConstructor
public enum WarningTypeEnum {
    WATER_QUALITY(1, "水质"),
    FEEDING(2, "投喂"),
    MEDICINE(3, "用药"),
    HARVEST(4, "收获"),
    EQUIPMENT(5, "设备"),
    WEATHER(6, "天气");

    @EnumValue
    private final int code;
    private final String description;
}