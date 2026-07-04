package com.pat.report.mapper;

import com.pat.report.domain.vo.SalesTop10ReportVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 统计报表 Mapper
 */
@Mapper
public interface ReportMapper {

    /**
     * 统计指定日期区间的每日营业额（已完成订单）
     */
    @Select("SELECT CAST(create_time AS DATE) AS date, COALESCE(SUM(total_amount), 0) AS turnover " +
            "FROM purchase_order " +
            "WHERE order_status IN (3, 4) " +
            "AND create_time >= #{beginTime} AND create_time < #{endTime} " +
            "GROUP BY CAST(create_time AS DATE) " +
            "ORDER BY date")
    List<Map<String, Object>> selectDailyTurnover(@Param("beginTime") LocalDateTime beginTime,
                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定日期区间的每日新增用户数
     */
    @Select("SELECT CAST(create_time AS DATE) AS date, COUNT(*) AS cnt " +
            "FROM user " +
            "WHERE create_time >= #{beginTime} AND create_time < #{endTime} " +
            "GROUP BY CAST(create_time AS DATE) " +
            "ORDER BY date")
    List<Map<String, Object>> selectDailyNewUsers(@Param("beginTime") LocalDateTime beginTime,
                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 获取截止某日期的总用户数
     */
    @Select("SELECT COUNT(*) FROM user WHERE create_time < #{endTime}")
    Long selectTotalUserCount(@Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定日期区间的每日订单总数
     */
    @Select("SELECT CAST(create_time AS DATE) AS date, COUNT(*) AS cnt " +
            "FROM purchase_order " +
            "WHERE create_time >= #{beginTime} AND create_time < #{endTime} " +
            "GROUP BY CAST(create_time AS DATE) " +
            "ORDER BY date")
    List<Map<String, Object>> selectDailyOrderCount(@Param("beginTime") LocalDateTime beginTime,
                                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定日期区间的每日有效订单数（排除取消/退款）
     */
    @Select("SELECT CAST(create_time AS DATE) AS date, COUNT(*) AS cnt " +
            "FROM purchase_order " +
            "WHERE order_status >= 0 " +
            "AND create_time >= #{beginTime} AND create_time < #{endTime} " +
            "GROUP BY CAST(create_time AS DATE) " +
            "ORDER BY date")
    List<Map<String, Object>> selectDailyValidOrderCount(@Param("beginTime") LocalDateTime beginTime,
                                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定日期区间的每日已完成订单数（已收货/已评价）
     */
    @Select("SELECT CAST(create_time AS DATE) AS date, COUNT(*) AS cnt " +
            "FROM purchase_order " +
            "WHERE order_status IN (3, 4) " +
            "AND create_time >= #{beginTime} AND create_time < #{endTime} " +
            "GROUP BY CAST(create_time AS DATE) " +
            "ORDER BY date")
    List<Map<String, Object>> selectDailyCompletedOrderCount(@Param("beginTime") LocalDateTime beginTime,
                                                             @Param("endTime") LocalDateTime endTime);

    /**
     * 每日营业额统计总数（已完成订单总和）
     */
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM purchase_order " +
            "WHERE order_status IN (3, 4) " +
            "AND create_time >= #{beginTime} AND create_time < #{endTime}")
    BigDecimal selectPeriodTurnover(@Param("beginTime") LocalDateTime beginTime,
                                    @Param("endTime") LocalDateTime endTime);

    /**
     * 查询销量排名 Top10（已完成订单的商品销量）
     */
    List<Map<String, Object>> selectSalesTop10(@Param("beginTime") LocalDateTime beginTime,
                                               @Param("endTime") LocalDateTime endTime);

}
