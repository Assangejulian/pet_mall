package com.pat.report.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 营业额统计 VO（适配 ECharts 折线图）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TurnoverReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 日期列表，以逗号分隔，例如：2026-07-01,2026-07-02,2026-07-03 */
    private String dateList;

    /** 营业额列表，以逗号分隔，例如：1000.00,1500.00,1200.00 */
    private String turnoverList;

}
