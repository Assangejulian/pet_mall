package com.pat.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.pat.ai.entity.AiChatRecord;
import com.pat.ai.service.IAiChatRecordService;
import com.pat.common.domain.Result;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai/record")
public class AiChatRecordController {

    private final IAiChatRecordService service;

    public AiChatRecordController(IAiChatRecordService service) {
        this.service = service;
    }

    @GetMapping("/session/{sessionId}")
    public Result<List<AiChatRecord>> listBySession(@PathVariable String sessionId) {
        QueryWrapper<AiChatRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("session_id", sessionId).orderByAsc("create_time");
        return Result.success(service.list(wrapper));
    }

    @DeleteMapping("/session/{sessionId}")
    public Result<Boolean> deleteBySession(@PathVariable String sessionId) {
        QueryWrapper<AiChatRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("session_id", sessionId);
        return Result.success(service.remove(wrapper));
    }
}
