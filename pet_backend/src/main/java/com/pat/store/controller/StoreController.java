package com.pat.store.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pat.common.controller.BaseController;
import com.pat.store.entity.Store;
import com.pat.store.service.IStoreService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/store")
public class StoreController extends BaseController<Store, Store, Store> {

    public StoreController(IStoreService service) {
        super(service);
    }

    @Override
    protected Store toVO(Store entity) {
        return entity;
    }

    @Override
    protected Store toDO(Store param) {
        return param;
    }

    @Override
    protected QueryWrapper<Store> buildQueryWrapper(Store param) {
        return new QueryWrapper<>();
    }
}