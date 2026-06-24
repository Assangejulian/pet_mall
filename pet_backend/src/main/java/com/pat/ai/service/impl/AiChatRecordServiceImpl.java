package com.pat.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pat.ai.entity.AiChatRecord;
import com.pat.ai.mapper.AiChatRecordMapper;
import com.pat.ai.service.IAiChatRecordService;
import org.springframework.stereotype.Service;

@Service
public class AiChatRecordServiceImpl extends ServiceImpl<AiChatRecordMapper, AiChatRecord> implements IAiChatRecordService {
}