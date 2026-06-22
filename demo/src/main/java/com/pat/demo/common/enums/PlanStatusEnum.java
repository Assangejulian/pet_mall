package com.pat.demo.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 计划状态枚举
 * 水产养殖管理系统专用枚举
 */
@Getter
@AllArgsConstructor
public enum PlanStatusEnum {
    DRAFT(1, "草稿"),
    PUBLISHED(2, "已发布"),
    EXECUTING(3, "执行中"),
    COMPLETED(4, "已完成"),
    DELAYED(5, "已延迟"),
    CANCELLED(6, "已取消"),
    PENDING_APPROVAL(7, "待审批"),
    APPROVED(8, "已审批"),
    REJECTED(9, "已驳回");

    @EnumValue
    private final int code;
    private final String description;
}
