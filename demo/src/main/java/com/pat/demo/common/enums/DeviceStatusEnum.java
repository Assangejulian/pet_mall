package com.pat.demo.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 设备状态枚举
 * 水产养殖管理系统专用枚举
 */
@Getter
@AllArgsConstructor
public enum DeviceStatusEnum {
    ONLINE("online", "在线"),
    OFFLINE("offline", "离线"),
    MAINTENANCE("maintenance", "维护中");

    @EnumValue
    private final String code;
    private final String description;
}