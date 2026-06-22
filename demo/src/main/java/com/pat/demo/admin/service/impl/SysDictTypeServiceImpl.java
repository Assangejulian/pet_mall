package com.pat.demo.admin.service.impl;

import com.artsail.admin.mapper.SysDictTypeMapper;
import com.artsail.admin.model.domain.SysDictType;
import com.artsail.admin.model.domain.Query.SysDictTypeQuery;
import com.artsail.admin.service.SysDictTypeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements SysDictTypeService {

    @Override
    public Page<SysDictType> search(Page<SysDictType> page, SysDictTypeQuery query) {
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(StringUtils.isNotBlank(query.getDictType()), SysDictType::getDictType, query.getDictType())
               .like(StringUtils.isNotBlank(query.getDictName()), SysDictType::getDictName, query.getDictName())
               .eq(query.getStatus() != null, SysDictType::getStatus, query.getStatus())
               .orderByDesc(SysDictType::getCreateTime);
        
        return this.page(page, wrapper);
    }
}
