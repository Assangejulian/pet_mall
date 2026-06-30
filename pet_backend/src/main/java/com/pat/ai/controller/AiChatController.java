package com.pat.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.pat.ai.domain.dto.AiChatRequest;
import com.pat.ai.service.AiChatService;
import com.pat.ai.domain.vo.AiChatResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pat.common.domain.Result;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@RestController
@Tag(name = "AI 智能客服", description = "AI 聊天同步/流式对话")
@RequestMapping("/api/ai")
public class AiChatController {

    private final AiChatService aiChatService;
    private final ThreadPoolTaskExecutor executor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiChatController(AiChatService aiChatService, ThreadPoolTaskExecutor executor) {
        this.aiChatService = aiChatService;
        this.executor = executor;
    }

        @Operation(summary = "AI 对话（同步）")
@PostMapping("/chat")
    public Result<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        return Result.success(aiChatService.chat(request));
    }

        @Operation(summary = "AI 对话（SSE 流式）")
@PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@Valid @RequestBody AiChatRequest request) {
        SseEmitter emitter = new SseEmitter(120_000L);
        executor.submit(() -> {
            try {
                aiChatService.chatStream(request, new AiChatService.StreamListener() {
                    @Override
                    public void onMeta(String sessionId) {
                        sendEventSilently(emitter, "meta", Map.of("sessionId", sessionId));
                    }

                    @Override
                    public void onDelta(String content) {
                        sendEventSilently(emitter, "delta", Map.of("content", content));
                    }

                    @Override
                    public void onComplete(AiChatResponse response) {
                        sendEventSilently(emitter, "done", response);
                    }
                });
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }

        @Operation(summary = "删除对话会话")
@DeleteMapping("/session/{sessionId}")
    public Result<Void> deleteSession(@PathVariable String sessionId) {
        aiChatService.deleteSession(sessionId);
        return Result.success();
    }

    private void sendEvent(SseEmitter emitter, String type, Object payload) throws IOException {
        emitter.send(SseEmitter.event()
                .name(type)
                .data(objectMapper.writeValueAsString(payload), MediaType.APPLICATION_JSON));
    }

    private void sendEventSilently(SseEmitter emitter, String type, Object payload) {
        try {
            sendEvent(emitter, type, payload);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to send AI stream event", ex);
        }
    }
}
