package com.pat.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pat.common.controller.BaseController;
import com.pat.ai.entity.AiChatRecord;
import com.pat.ai.service.IAiChatRecordService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai/record")
public class AiChatRecordController extends BaseController<AiChatRecord, AiChatRecord, AiChatRecord> {

    public AiChatRecordController(IAiChatRecordService service) {
        super(service);
    }

    @Override
    protected AiChatRecord toVO(AiChatRecord entity) {
        return entity;
    }

    @Override
    protected AiChatRecord toDO(AiChatRecord param) {
        return param;
    }

    @Override
    protected QueryWrapper<AiChatRecord> buildQueryWrapper(AiChatRecord param) {
        return new QueryWrapper<>();
    }
}