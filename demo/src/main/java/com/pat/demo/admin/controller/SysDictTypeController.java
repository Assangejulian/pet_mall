package com.pat.demo.admin.controller;

import com.artsail.admin.model.domain.SysDictType;
import com.artsail.admin.model.domain.Query.SysDictTypeQuery;
import com.artsail.admin.service.SysDictTypeService;
import com.artsail.common.controller.BaseController;
import com.artsail.common.domain.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/dict/type")
public class SysDictTypeController extends BaseController<SysDictTypeService, SysDictType, SysDictType, SysDictTypeQuery> {

    @Autowired
    private SysDictTypeService sysDictTypeService;

    @Override
    public Result<Page<SysDictType>> search(Page<SysDictType> page, SysDictTypeQuery query) {
        return Result.success(sysDictTypeService.search(page, query));
    }
}
