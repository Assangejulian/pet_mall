package com.pat.demo.admin.controller;

import com.artsail.admin.model.domain.SysNews;
import com.artsail.admin.service.SysNewsService;
import com.artsail.common.domain.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/news")
public class SysNewsController {

    @Autowired
    private SysNewsService sysNewsService;

    @GetMapping("/list")
    public Result<Page<SysNews>> list(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String category) {
        Page<SysNews> page = new Page<>(current, pageSize);
        LambdaQueryWrapper<SysNews> wrapper = new LambdaQueryWrapper<SysNews>()
                .eq(SysNews::getIsPublished, true)
                .eq(category != null && !category.isEmpty(), SysNews::getCategory, category)
                .orderByDesc(SysNews::getPublishTime);
        return Result.success(sysNewsService.page(page, wrapper));
    }

    @GetMapping("/detail/{id}")
    public Result<SysNews> detail(@PathVariable Long id) {
        return Result.success(sysNewsService.getById(id));
    }
}
