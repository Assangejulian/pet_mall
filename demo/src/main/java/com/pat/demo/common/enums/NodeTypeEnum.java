package com.pat.demo.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 节点类型枚举
 * 水产养殖管理系统专用枚举
 */
@Getter
@AllArgsConstructor
public enum NodeTypeEnum {
    SENSOR("sensor", "传感器"),
    CAMERA("camera", "摄像头"),
    CONTROLLER("controller", "控制器");

    @EnumValue
    private final String code;
    private final String description;
}