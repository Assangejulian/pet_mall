package com.pat.report.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单统计 VO（适配 ECharts 折线图）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 日期列表，以逗号分隔 */
    private String dateList;

    /** 每日订单总数，以逗号分隔 */
    private String orderCountList;

    /** 每日有效订单数（排除取消/退款），以逗号分隔 */
    private String validOrderCountList;

    /** 每日已完成订单数（已收货/已评价），以逗号分隔 */
    private String completedOrderCountList;

}
