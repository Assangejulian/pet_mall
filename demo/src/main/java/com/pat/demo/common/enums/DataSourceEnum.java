package com.pat.demo.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据来源枚举
 * 水产养殖管理系统专用枚举
 */
@Getter
@AllArgsConstructor
public enum DataSourceEnum {
    MANUAL("manual", "手动录入"),
    AUTO("auto", "自动采集"),
    API("api", "接口获取");

    @EnumValue
    private final String code;
    private final String description;
}