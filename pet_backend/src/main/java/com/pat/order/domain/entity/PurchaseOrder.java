package com.pat.order.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.pat.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("purchase_order")
public class PurchaseOrder extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private String orderNo;
    private Long userId;
    private Long addressId;
    private String addressSnapshot;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;
    private Integer orderStatus;
    private String remark;
    private String cancelReason;
    private LocalDateTime cancelTime;
    private LocalDateTime payTime;
    private LocalDateTime shipTime;
    private LocalDateTime receiveTime;
    private LocalDateTime evaluateTime;
    private LocalDateTime refundApplyTime;
    private LocalDateTime refundAuditTime;

    @TableField(exist = false)
    private List<OrderItem> items;
}
