package com.pat.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pat.common.controller.BaseController;
import com.pat.order.entity.PurchaseOrder;
import com.pat.order.service.IPurchaseOrderService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
public class PurchaseOrderController extends BaseController<PurchaseOrder, PurchaseOrder, PurchaseOrder> {

    public PurchaseOrderController(IPurchaseOrderService service) {
        super(service);
    }

    @Override
    protected PurchaseOrder toVO(PurchaseOrder entity) {
        return entity;
    }

    @Override
    protected PurchaseOrder toDO(PurchaseOrder param) {
        return param;
    }

    @Override
    protected QueryWrapper<PurchaseOrder> buildQueryWrapper(PurchaseOrder param) {
        return new QueryWrapper<>();
    }
}