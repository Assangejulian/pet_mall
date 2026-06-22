package com.pat.demo.admin.service;

import com.artsail.admin.model.domain.SysDictData;
import com.artsail.admin.model.domain.Query.SysDictDataQuery;
import com.artsail.admin.model.domain.VO.DictDataVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;
import java.util.List;

public interface SysDictDataService extends IService<SysDictData> {
    Page<SysDictData> search(Page<SysDictData> page, SysDictDataQuery query);
    
    /**
     * 获取所有字典数据，按字典类型分组
     * @return Map<字典类型, List<字典数据>>
     */
    Map<String, List<DictDataVO>> getAllDictData();
}
