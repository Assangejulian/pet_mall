package com.pat.demo.admin.controller;

import com.artsail.admin.model.domain.SysDictData;
import com.artsail.admin.model.domain.Query.SysDictDataQuery;
import com.artsail.admin.model.domain.VO.DictDataVO;
import com.artsail.admin.service.SysDictDataService;
import com.artsail.common.controller.BaseController;
import com.artsail.common.domain.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/dict/data")
public class SysDictDataController extends BaseController<SysDictDataService, SysDictData, SysDictData, SysDictDataQuery> {

    @Autowired
    private SysDictDataService sysDictDataService;

    @Override
    public Result<Page<SysDictData>> search(Page<SysDictData> page, SysDictDataQuery query) {
        return Result.success(sysDictDataService.search(page, query));
    }
    
    /**
     * 获取所有字典数据，按类型分组
     */
    @GetMapping("/dropdown/options")
    public Result<Map<String, List<DictDataVO>>> getDictData() {
        return Result.success(sysDictDataService.getAllDictData());
    }
}
