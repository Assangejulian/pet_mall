package com.pat.ai.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.pat.ai.domain.dto.AiActionConfirmRequest;
import com.pat.ai.domain.dto.AiChatRequest;
import com.pat.ai.service.AiChatService;
import com.pat.ai.service.PendingAiActionService;
import com.pat.ai.domain.vo.AiChatResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pat.common.domain.Result;
import com.pat.user.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.core.task.TaskExecutor;
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

@RestController
@Tag(name = "AI 智能客服", description = "AI 聊天同步/流式对话")
@RequestMapping("/api/ai")
public class AiChatController {

    private final AiChatService aiChatService;
    private final PendingAiActionService pendingAiActionService;
    private final TaskExecutor taskExecutor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiChatController(AiChatService aiChatService,
                            PendingAiActionService pendingAiActionService,
                            TaskExecutor taskExecutor) {
        this.aiChatService = aiChatService;
        this.pendingAiActionService = pendingAiActionService;
        this.taskExecutor = taskExecutor;
    }

    @Operation(summary = "AI 对话（同步）")
    @PostMapping("/chat")
    public Result<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request, HttpServletRequest httpRequest) {
        attachUserIdFromToken(request, httpRequest);
        return Result.success(aiChatService.chat(request));
    }

    @Operation(summary = "AI 对话（SSE 流式）")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@Valid @RequestBody AiChatRequest request, HttpServletRequest httpRequest) {
        attachUserIdFromToken(request, httpRequest);
        SseEmitter emitter = new SseEmitter(120_000L);
        taskExecutor.execute(() -> {
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
            } catch (Throwable ex) {
                sendEventSilently(emitter, "delta", Map.of("content", "AI 服务暂时不可用，请重启后端并重新加载 Maven 依赖后再试。"));
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

    @Operation(summary = "确认并执行 AI 待处理动作")
    @PostMapping("/action/confirm")
    public Result<?> confirmAction(@Valid @RequestBody AiActionConfirmRequest request, HttpServletRequest httpRequest) {
        Long userId = resolveUserId(httpRequest);
        if (userId == null) {
            userId = request.getUserId();
        }
        return pendingAiActionService.confirm(userId, request.getActionId());
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

    private void attachUserIdFromToken(AiChatRequest request, HttpServletRequest httpRequest) {
        Long userId = resolveUserId(httpRequest);
        if (userId != null) {
            request.setUserId(userId);
        }
    }

    private Long resolveUserId(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return null;
        }
        try {
            Claims claims = JwtUtil.parseToken(auth.substring("Bearer ".length()));
            return claims.get("userId", Long.class);
        } catch (Exception ex) {
            return null;
        }
    }
}
