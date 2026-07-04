package com.pat.report.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户统计 VO（适配 ECharts 折线图）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 日期列表，以逗号分隔 */
    private String dateList;

    /** 每日新增用户数，以逗号分隔 */
    private String newUserList;

    /** 截止每日总用户数，以逗号分隔 */
    private String totalUserList;

}
