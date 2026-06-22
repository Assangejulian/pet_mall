package com.pat.demo.admin.service.impl;

import com.artsail.admin.mapper.SysNewsMapper;
import com.artsail.admin.model.domain.SysNews;
import com.artsail.admin.service.SysNewsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SysNewsServiceImpl extends ServiceImpl<SysNewsMapper, SysNews> implements SysNewsService {
}
