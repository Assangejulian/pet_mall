package com.pat.demo.admin.service;

import com.artsail.admin.model.domain.SysDictType;
import com.artsail.admin.model.domain.Query.SysDictTypeQuery;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SysDictTypeService extends IService<SysDictType> {
    Page<SysDictType> search(Page<SysDictType> page, SysDictTypeQuery query);
}
