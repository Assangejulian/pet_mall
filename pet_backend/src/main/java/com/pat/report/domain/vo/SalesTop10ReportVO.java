package com.pat.report.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 销量排名 Top10 VO（适配 ECharts 柱状图）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesTop10ReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 商品名称列表，以逗号分隔，例如：鱼香肉丝,宫保鸡丁,水煮鱼 */
    private String nameList;

    /** 销量列表，以逗号分隔，例如：260,215,200 */
    private String numberList;

}
