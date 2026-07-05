package com.pat.report.mapper;

import com.pat.report.domain.vo.SalesTop10ReportVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {

    @Select("SELECT CAST(create_time AS DATE) AS date, COALESCE(SUM(total_amount), 0) AS turnover " +
            "FROM purchase_order " +
            "WHERE order_status IN (3, 4) " +
            "AND create_time >= #{beginTime} AND create_time < #{endTime} " +
            "GROUP BY CAST(create_time AS DATE) " +
            "ORDER BY date")
    List<Map<String, Object>> selectDailyTurnover(@Param("beginTime") LocalDateTime beginTime,
                                                  @Param("endTime") LocalDateTime endTime);

    @Select("SELECT CAST(create_time AS DATE) AS date, COUNT(*) AS cnt " +
            "FROM user " +
            "WHERE create_time >= #{beginTime} AND create_time < #{endTime} " +
            "GROUP BY CAST(create_time AS DATE) " +
            "ORDER BY date")
    List<Map<String, Object>> selectDailyNewUsers(@Param("beginTime") LocalDateTime beginTime,
                                                  @Param("endTime") LocalDateTime endTime);

    @Select("SELECT COUNT(*) FROM user WHERE create_time < #{endTime}")
    Long selectTotalUserCount(@Param("endTime") LocalDateTime endTime);

    @Select("SELECT CAST(create_time AS DATE) AS date, COUNT(*) AS cnt " +
            "FROM purchase_order " +
            "WHERE create_time >= #{beginTime} AND create_time < #{endTime} " +
            "GROUP BY CAST(create_time AS DATE) " +
            "ORDER BY date")
    List<Map<String, Object>> selectDailyOrderCount(@Param("beginTime") LocalDateTime beginTime,
                                                    @Param("endTime") LocalDateTime endTime);

    @Select("SELECT CAST(create_time AS DATE) AS date, COUNT(*) AS cnt " +
            "FROM purchase_order " +
            "WHERE order_status >= 0 " +
            "AND create_time >= #{beginTime} AND create_time < #{endTime} " +
            "GROUP BY CAST(create_time AS DATE) " +
            "ORDER BY date")
    List<Map<String, Object>> selectDailyValidOrderCount(@Param("beginTime") LocalDateTime beginTime,
                                                         @Param("endTime") LocalDateTime endTime);

    @Select("SELECT CAST(create_time AS DATE) AS date, COUNT(*) AS cnt " +
            "FROM purchase_order " +
            "WHERE order_status IN (3, 4) " +
            "AND create_time >= #{beginTime} AND create_time < #{endTime} " +
            "GROUP BY CAST(create_time AS DATE) " +
            "ORDER BY date")
    List<Map<String, Object>> selectDailyCompletedOrderCount(@Param("beginTime") LocalDateTime beginTime,
                                                             @Param("endTime") LocalDateTime endTime);

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM purchase_order " +
            "WHERE order_status IN (3, 4) " +
            "AND create_time >= #{beginTime} AND create_time < #{endTime}")
    BigDecimal selectPeriodTurnover(@Param("beginTime") LocalDateTime beginTime,
                                    @Param("endTime") LocalDateTime endTime);

    List<Map<String, Object>> selectSalesTop10(@Param("beginTime") LocalDateTime beginTime,
                                               @Param("endTime") LocalDateTime endTime,
                                               @Param("storeIds") List<Long> storeIds);

    // ===== Merchant dashboard queries =====

    @Select("SELECT COALESCE(SUM(po.total_amount), 0) FROM purchase_order po " +
            "WHERE po.order_status IN (1, 2, 3, 4) " +
            "AND EXISTS (SELECT 1 FROM order_item oi JOIN product p ON oi.product_id = p.id " +
            "WHERE oi.order_id = po.id AND p.store_id = #{storeId})")
    BigDecimal selectMerchantRevenue(@Param("storeId") Long storeId);

    @Select("SELECT COUNT(DISTINCT po.id) FROM purchase_order po " +
            "WHERE po.create_time >= #{today} " +
            "AND EXISTS (SELECT 1 FROM order_item oi JOIN product p ON oi.product_id = p.id " +
            "WHERE oi.order_id = po.id AND p.store_id = #{storeId})")
    long selectMerchantTodayOrders(@Param("storeId") Long storeId, @Param("today") LocalDateTime today);

    @Select("SELECT po.order_status, COUNT(DISTINCT po.id) AS cnt FROM purchase_order po " +
            "WHERE EXISTS (SELECT 1 FROM order_item oi JOIN product p ON oi.product_id = p.id " +
            "WHERE oi.order_id = po.id AND p.store_id = #{storeId}) " +
            "GROUP BY po.order_status")
    List<Map<String, Object>> selectMerchantOrderStatusCount(@Param("storeId") Long storeId);

    // ===== Merchant report queries (filtered by storeIds) =====

    @Select("<script>" +
            "SELECT CAST(o.create_time AS DATE) AS date, COALESCE(SUM(o.total_amount), 0) AS turnover " +
            "FROM purchase_order o " +
            "WHERE o.order_status IN (3, 4) " +
            "AND o.create_time &gt;= #{beginTime} AND o.create_time &lt; #{endTime} " +
            "<if test='storeIds != null and !storeIds.isEmpty()'>" +
            "AND EXISTS (SELECT 1 FROM order_item oi JOIN product p ON oi.product_id = p.id " +
            "WHERE oi.order_id = o.id AND p.store_id IN " +
            "<foreach collection='storeIds' item='sid' open='(' separator=',' close=')'>#{sid}</foreach>) " +
            "</if>" +
            "GROUP BY CAST(o.create_time AS DATE) ORDER BY date" +
            "</script>")
    List<Map<String, Object>> selectMerchantDailyTurnover(@Param("beginTime") LocalDateTime beginTime,
                                                          @Param("endTime") LocalDateTime endTime,
                                                          @Param("storeIds") List<Long> storeIds);

    @Select("<script>" +
            "SELECT CAST(o.create_time AS DATE) AS date, COUNT(*) AS cnt " +
            "FROM purchase_order o " +
            "WHERE o.create_time &gt;= #{beginTime} AND o.create_time &lt; #{endTime} " +
            "<if test='storeIds != null and !storeIds.isEmpty()'>" +
            "AND EXISTS (SELECT 1 FROM order_item oi JOIN product p ON oi.product_id = p.id " +
            "WHERE oi.order_id = o.id AND p.store_id IN " +
            "<foreach collection='storeIds' item='sid' open='(' separator=',' close=')'>#{sid}</foreach>) " +
            "</if>" +
            "GROUP BY CAST(o.create_time AS DATE) ORDER BY date" +
            "</script>")
    List<Map<String, Object>> selectMerchantDailyOrderCount(@Param("beginTime") LocalDateTime beginTime,
                                                            @Param("endTime") LocalDateTime endTime,
                                                            @Param("storeIds") List<Long> storeIds);

    @Select("<script>" +
            "SELECT CAST(o.create_time AS DATE) AS date, COUNT(*) AS cnt " +
            "FROM purchase_order o " +
            "WHERE o.order_status &gt;= 0 " +
            "AND o.create_time &gt;= #{beginTime} AND o.create_time &lt; #{endTime} " +
            "<if test='storeIds != null and !storeIds.isEmpty()'>" +
            "AND EXISTS (SELECT 1 FROM order_item oi JOIN product p ON oi.product_id = p.id " +
            "WHERE oi.order_id = o.id AND p.store_id IN " +
            "<foreach collection='storeIds' item='sid' open='(' separator=',' close=')'>#{sid}</foreach>) " +
            "</if>" +
            "GROUP BY CAST(o.create_time AS DATE) ORDER BY date" +
            "</script>")
    List<Map<String, Object>> selectMerchantDailyValidOrderCount(@Param("beginTime") LocalDateTime beginTime,
                                                                 @Param("endTime") LocalDateTime endTime,
                                                                 @Param("storeIds") List<Long> storeIds);

    @Select("<script>" +
            "SELECT CAST(o.create_time AS DATE) AS date, COUNT(*) AS cnt " +
            "FROM purchase_order o " +
            "WHERE o.order_status IN (3, 4) " +
            "AND o.create_time &gt;= #{beginTime} AND o.create_time &lt; #{endTime} " +
            "<if test='storeIds != null and !storeIds.isEmpty()'>" +
            "AND EXISTS (SELECT 1 FROM order_item oi JOIN product p ON oi.product_id = p.id " +
            "WHERE oi.order_id = o.id AND p.store_id IN " +
            "<foreach collection='storeIds' item='sid' open='(' separator=',' close=')'>#{sid}</foreach>) " +
            "</if>" +
            "GROUP BY CAST(o.create_time AS DATE) ORDER BY date" +
            "</script>")
    List<Map<String, Object>> selectMerchantDailyCompletedOrderCount(@Param("beginTime") LocalDateTime beginTime,
                                                                     @Param("endTime") LocalDateTime endTime,
                                                                     @Param("storeIds") List<Long> storeIds);

    List<Map<String, Object>> selectMerchantSalesTop10(@Param("beginTime") LocalDateTime beginTime,
                                                       @Param("endTime") LocalDateTime endTime,
                                                       @Param("storeIds") List<Long> storeIds);
}
