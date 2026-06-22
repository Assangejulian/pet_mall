package com.pat.demo.admin.service.impl;

import com.artsail.admin.mapper.SysDictDataMapper;
import com.artsail.admin.model.domain.SysDictData;
import com.artsail.admin.model.domain.Query.SysDictDataQuery;
import com.artsail.admin.model.domain.VO.DictDataVO;
import com.artsail.admin.service.SysDictDataService;
import com.artsail.aquaculture.service.BaseInfoService;
import com.artsail.aquaculture.model.domain.BaseInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements SysDictDataService {

    private final BaseInfoService baseInfoService;

    @Override
    public Page<SysDictData> search(Page<SysDictData> page, SysDictDataQuery query) {
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(StringUtils.isNotBlank(query.getDictType()), SysDictData::getDictType, query.getDictType())
               .like(StringUtils.isNotBlank(query.getDictLabel()), SysDictData::getDictLabel, query.getDictLabel())
               .eq(StringUtils.isNotBlank(query.getDictValue()), SysDictData::getDictValue, query.getDictValue())
               .eq(query.getStatus() != null, SysDictData::getStatus, query.getStatus())
               .orderByAsc(SysDictData::getSortOrder);
        
        return this.page(page, wrapper);
    }
    
    @Override
    public Map<String, List<DictDataVO>> getAllDictData() {
        // 查询所有启用的字典数据
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDictData::getStatus, 1)
               .orderByAsc(SysDictData::getDictType)
               .orderByAsc(SysDictData::getSortOrder);
        
        List<SysDictData> dictDataList = this.list(wrapper);
        
        // 按字典类型分组，并转换为 VO
        Map<String, List<DictDataVO>> result = dictDataList.stream()
                .collect(Collectors.groupingBy(
                        SysDictData::getDictType,
                        Collectors.mapping(dictData -> {
                            DictDataVO vo = new DictDataVO();
                            vo.setLabel(dictData.getDictLabel());
                            vo.setValue(dictData.getDictValue());
                            return vo;
                        }, Collectors.toList())
                ));
        
        // 动态添加基地列表
        try {
            List<BaseInfo> baseList = baseInfoService.list(
                new LambdaQueryWrapper<BaseInfo>()
                    .eq(BaseInfo::getStatus, 1)
                    .orderByAsc(BaseInfo::getBaseName)
            );
            
            if (baseList != null && !baseList.isEmpty()) {
                List<DictDataVO> baseDictList = baseList.stream()
                        .map(base -> {
                            DictDataVO vo = new DictDataVO();
                            vo.setLabel(base.getBaseName());
                            vo.setValue(String.valueOf(base.getId()));
                            return vo;
                        })
                        .collect(Collectors.toList());
                
                result.put("base_list", baseDictList);
                log.info("成功加载 {} 个基地数据", baseDictList.size());
            } else {
                log.warn("基地数据为空");
            }
        } catch (Exception e) {
            log.error("加载基地数据失败", e);
        }
        
        return result;
    }
}
