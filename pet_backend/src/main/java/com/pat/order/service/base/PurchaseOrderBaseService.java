package com.pat.order.service.base;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pat.order.domain.entity.PurchaseOrder;
import com.pat.order.mapper.PurchaseOrderMapper;
import org.springframework.stereotype.Service;

/** 基础 CRUD 类，不包含业务逻辑 */
@Service
public class PurchaseOrderBaseService extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder> {
}
