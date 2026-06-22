package com.pat.demo.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知渠道枚举
 * 水产养殖管理系统专用枚举
 */
@Getter
@AllArgsConstructor
public enum NotifyChannelEnum {
    SMS("sms", "短信"),
    EMAIL("email", "邮件"),
    APP("app", "APP推送"),
    WECHAT("wechat", "微信");

    @EnumValue
    private final String code;
    private final String description;
}