package com.pat.ai.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.pat.ai.domain.dto.AiChatRequest;
import com.pat.ai.domain.entity.AiChatRecord;
import com.pat.ai.service.AiChatService;
import com.pat.ai.domain.vo.AiChatResponse;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class AiChatServiceImpl implements AiChatService {

    private final JdbcTemplate jdbcTemplate;

    @Value("${ai.chat.api-key:}")
    private String apiKey;

    @Value("${ai.chat.base-url:https://api.deepseek.com/v1}")
    private String baseUrl;

    @Value("${ai.chat.model-name:deepseek-chat}")
    private String modelName;

    @Value("${ai.chat.flash-model-name:deepseek-chat}")
    private String flashModelName;

    @Value("${ai.chat.pro-model-name:deepseek-reasoner}")
    private String proModelName;

    @Value("${ai.chat.temperature:0.3}")
    private Double temperature;

    @Value("${ai.chat.timeout-seconds:60}")
    private Long timeoutSeconds;

    public AiChatServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        String sessionId = resolveSessionId(request);
        List<AiChatRecord> context = loadRecentContext(sessionId);
        saveRecord(request.getUserId(), sessionId, "user", request.getMessage());

        String reply = callModelOrMock(request, context, chooseModelName(request.getModelMode()));
        saveRecord(request.getUserId(), sessionId, "assistant", reply);

        return buildResponse(sessionId, reply, request.getMessage());
    }

    @Override
    public void chatStream(AiChatRequest request, StreamListener listener) {
        String sessionId = resolveSessionId(request);
        listener.onMeta(sessionId);

        List<AiChatRecord> context = loadRecentContext(sessionId);
        saveRecord(request.getUserId(), sessionId, "user", request.getMessage());

        if (!StringUtils.hasText(apiKey)) {
            String reply = mockReply(request.getMessage());
            emitChunks(reply, listener);
            saveRecord(request.getUserId(), sessionId, "assistant", reply);
            listener.onComplete(buildResponse(sessionId, reply, request.getMessage()));
            return;
        }

        List<ChatMessage> messages = buildChatMessages(request, context);
        StringBuilder reply = new StringBuilder();
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        OpenAiStreamingChatModel model = OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(normalizeBaseUrl(baseUrl))
                .modelName(chooseModelName(request.getModelMode()))
                .temperature(temperature)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .build();

        model.chat(messages, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String partialResponse) {
                if (!StringUtils.hasText(partialResponse)) {
                    return;
                }
                reply.append(partialResponse);
                listener.onDelta(partialResponse);
            }

            @Override
            public void onCompleteResponse(ChatResponse response) {
                String content = reply.toString();
                if (!StringUtils.hasText(content) && response != null && response.aiMessage() != null) {
                    content = response.aiMessage().text();
                }
                if (!StringUtils.hasText(content)) {
                    content = mockReply(request.getMessage());
                    listener.onDelta(content);
                }
                saveRecord(request.getUserId(), sessionId, "assistant", content);
                listener.onComplete(buildResponse(sessionId, content, request.getMessage()));
                done.countDown();
            }

            @Override
            public void onError(Throwable throwable) {
                error.set(throwable);
                done.countDown();
            }
        });

        try {
            done.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("AI streaming interrupted", ex);
        }

        if (error.get() != null) {
            String fallback = mockReply(request.getMessage());
            emitChunks(fallback, listener);
            saveRecord(request.getUserId(), sessionId, "assistant", fallback);
            listener.onComplete(buildResponse(sessionId, fallback, request.getMessage()));
        }
    }

    @Override
    public void deleteSession(String sessionId) {
        if (StringUtils.hasText(sessionId)) {
            jdbcTemplate.update("DELETE FROM ai_chat_record WHERE session_id = ?", sessionId);
        }
    }

    private String resolveSessionId(AiChatRequest request) {
        if (StringUtils.hasText(request.getSessionId())) {
            return request.getSessionId();
        }
        return UUID.randomUUID().toString().replace("-", "");
    }

    private List<AiChatRecord> loadRecentContext(String sessionId) {
        List<AiChatRecord> records = jdbcTemplate.query("""
                        SELECT id, user_id, session_id, role, content, create_time
                        FROM ai_chat_record
                        WHERE session_id = ?
                        ORDER BY create_time DESC
                        LIMIT 8
                        """,
                (rs, rowNum) -> {
                    AiChatRecord record = new AiChatRecord();
                    record.setId(rs.getLong("id"));
                    long userId = rs.getLong("user_id");
                    record.setUserId(rs.wasNull() ? null : userId);
                    record.setSessionId(rs.getString("session_id"));
                    record.setRole(rs.getString("role"));
                    record.setContent(rs.getString("content"));
                    record.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
                    return record;
                },
                sessionId);

                java.util.Collections.reverse(records);

                return records;
    }

    private void saveRecord(Long userId, String sessionId, String role, String content) {
        jdbcTemplate.update("""
                        INSERT INTO ai_chat_record(id, user_id, session_id, role, content, create_time)
                        VALUES (?, ?, ?, ?, ?, NOW(3))
                        """,
                IdWorker.getId(), userId, sessionId, role, content);
    }

    private String callModelOrMock(AiChatRequest request, List<AiChatRecord> context, String selectedModelName) {
        if (!StringUtils.hasText(apiKey)) {
            return mockReply(request.getMessage());
        }

        try {
            OpenAiChatModel model = OpenAiChatModel.builder()
                    .apiKey(apiKey)
                    .baseUrl(normalizeBaseUrl(baseUrl))
                    .modelName(selectedModelName)
                    .temperature(temperature)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .build();

            ChatResponse response = model.chat(buildChatMessages(request, context));
            String content = response.aiMessage().text();
            return StringUtils.hasText(content) ? content : mockReply(request.getMessage());
        } catch (Exception ignored) {
            return mockReply(request.getMessage());
        }
    }

    private List<ChatMessage> buildChatMessages(AiChatRequest request, List<AiChatRecord> context) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(SystemMessage.from(buildSystemPrompt(request.getPetProfile())));
        messages.addAll(buildContextMessages(context));
        messages.add(UserMessage.from(request.getMessage()));
        return messages;
    }

    private List<ChatMessage> buildContextMessages(List<AiChatRecord> context) {
        List<ChatMessage> messages = new ArrayList<>();
        for (AiChatRecord record : context) {
            if ("assistant".equals(record.getRole())) {
                messages.add(AiMessage.from(record.getContent()));
            } else {
                messages.add(UserMessage.from(record.getContent()));
            }
        }
        return messages;
    }

    private String buildSystemPrompt(String petProfile) {
        String profile = StringUtils.hasText(petProfile) ? "当前宠物档案：" + petProfile + "。" : "";
        return "你是 PetNest 暖窝商城的智能客服，负责宠物日常照护、饮食、洗护、行为训练和用品选择建议。"
                + profile
                + "回答要具体、温和、可执行。涉及急性症状、持续恶化、明显疼痛、频繁呕吐腹泻或疑似误食时，必须提醒用户及时联系宠物医生。"
                + "不要编造平台库存、订单状态或无法确认的物流信息；如果需要商品、订单或门店数据，先说明需要用户补充信息。";
    }

    private void emitChunks(String reply, StreamListener listener) {
        for (String chunk : splitReply(reply, 12)) {
            listener.onDelta(chunk);
            try {
                Thread.sleep(45);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private List<String> splitReply(String reply, int chunkSize) {
        String text = reply == null ? "" : reply;
        List<String> chunks = new ArrayList<>();
        for (int index = 0; index < text.length(); index += chunkSize) {
            chunks.add(text.substring(index, Math.min(index + chunkSize, text.length())));
        }
        if (chunks.isEmpty()) {
            chunks.add("");
        }
        return chunks;
    }

    private String mockReply(String message) {
        String text = message == null ? "" : message;
        if (text.contains("猫粮") || text.contains("换粮") || text.contains("饮食")) {
            return "可以先按 7 天换粮法处理：前两天旧粮 75% + 新粮 25%，中间两三天各一半，最后逐步提高新粮比例。观察便便、呕吐和精神状态，如果明显异常，先暂停换粮并咨询宠物医生。";
        }
        if (text.contains("呕吐") || text.contains("不吃") || text.contains("拉稀") || text.contains("精神")) {
            return "先记录持续时间、呕吐或排便次数、是否喝水、精神状态和体温变化。轻微且短时间可以观察，但如果持续加重、精神差、频繁呕吐腹泻或疑似误食，请及时联系宠物医生。";
        }
        if (text.contains("第一次") || text.contains("新手") || text.contains("接") || text.contains("回家")) {
            return "第一次接宠物回家，先准备安静隔离区、食盆水碗、主粮、猫砂盆或尿垫、航空箱和基础清洁用品。第一晚不要频繁打扰，让它自己探索，稳定吃喝和排便比立刻亲近更重要。";
        }
        return "可以的。建议你先说清宠物的年龄、体重、品种、当前饮食、精神状态和最近变化，我会帮你拆成日常照护、风险观察和用品准备三部分处理。";
    }

    private AiChatResponse buildResponse(String sessionId, String reply, String message) {
        AiChatResponse response = new AiChatResponse();
        response.setSessionId(sessionId);
        response.setReply(reply);
        response.setSuggestions(List.of("继续追问照护步骤", "生成一周照护计划", "推荐需要准备的用品"));
        response.setRecommendations(mockRecommendations(message));
        return response;
    }

    private List<AiChatResponse.Recommendation> mockRecommendations(String message) {
        String text = message == null ? "" : message;
        if (text.contains("狗")) {
            return List.of(
                    new AiChatResponse.Recommendation("耐咬训练玩具", "用于消耗精力，辅助行为训练", "训练"),
                    new AiChatResponse.Recommendation("慢食碗", "帮助进食过快的狗狗放慢速度", "饮食")
            );
        }
        return List.of(
                new AiChatResponse.Recommendation("低敏主粮", "适合肠胃敏感或换粮观察期优先考虑", "饮食"),
                new AiChatResponse.Recommendation("观察记录卡", "记录饮食、排便和精神状态，便于复盘", "照护")
        );
    }

    private String normalizeBaseUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return "https://api.deepseek.com/v1";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private String chooseModelName(String modelMode) {
        if ("pro".equalsIgnoreCase(modelMode)) {
            return StringUtils.hasText(proModelName) ? proModelName : modelName;
        }
        return StringUtils.hasText(flashModelName) ? flashModelName : modelName;
    }
}
