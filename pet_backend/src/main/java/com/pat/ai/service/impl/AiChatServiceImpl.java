package com.pat.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pat.ai.domain.dto.AiChatRequest;
import com.pat.ai.domain.entity.AiChatRecord;
import com.pat.ai.service.AiCustomerAgent;
import com.pat.ai.service.AiMallToolService;
import com.pat.ai.service.IAiChatRecordService;
import com.pat.ai.service.AiChatService;
import com.pat.ai.service.PendingAiActionService;
import com.pat.ai.domain.vo.AiChatResponse;
import com.pat.common.exception.BusinessException;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class AiChatServiceImpl implements AiChatService {

    private final IAiChatRecordService aiChatRecordService;
    private final AiMallToolService aiMallToolService;
    private final PendingAiActionService pendingAiActionService;
    private final Resource systemPromptResource;
    private String systemPromptTemplate;

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

    @Value("${ai.chat.memory-turns:10}")
    private Integer memoryTurns;

    @Value("${ai.chat.max-tokens:2048}")
    private Integer maxTokens;

    public AiChatServiceImpl(IAiChatRecordService aiChatRecordService,
                             AiMallToolService aiMallToolService,
                             PendingAiActionService pendingAiActionService,
                             @Value("classpath:ai/system-prompt.txt") Resource systemPromptResource) {
        this.aiChatRecordService = aiChatRecordService;
        this.aiMallToolService = aiMallToolService;
        this.pendingAiActionService = pendingAiActionService;
        this.systemPromptResource = systemPromptResource;
    }

    @PostConstruct
    public void initSystemPrompt() {
        this.systemPromptTemplate = loadSystemPromptTemplate();
    }

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        if (!isLoggedIn(request)) {
            return buildLoginRequiredResponse(resolveSessionId(request));
        }
        ensureAiAvailable();
        String sessionId = resolveSessionId(request);
        request.setSessionId(sessionId);
        List<AiChatRecord> context = loadRecentContext(sessionId);

        List<AiChatResponse.PendingAction> pendingActions;
        String reply;
        pendingAiActionService.beginCollecting();
        try {
            reply = callAgent(request, context, chooseModelName(request.getModelMode()));
        } finally {
            pendingActions = pendingAiActionService.drainCollectedActions();
        }
        saveConversationTurn(request.getUserId(), sessionId, request.getMessage(), reply);

        return buildResponse(sessionId, reply, request.getMessage(), pendingActions);
    }

    @Override
    public void chatStream(AiChatRequest request, StreamListener listener) {
        String sessionId = resolveSessionId(request);
        request.setSessionId(sessionId);
        listener.onMeta(sessionId);

        AiChatResponse response = chat(request);
        emitChunks(response.getReply(), listener);
        listener.onComplete(response);
    }

    @Override
    public void deleteSession(String sessionId) {
        if (StringUtils.hasText(sessionId)) {
            aiChatRecordService.lambdaUpdate()
                    .eq(AiChatRecord::getSessionId, sessionId)
                    .remove();
        }
    }

    private String resolveSessionId(AiChatRequest request) {
        if (StringUtils.hasText(request.getSessionId())) {
            return request.getSessionId();
        }
        return UUID.randomUUID().toString().replace("-", "");
    }

    private List<AiChatRecord> loadRecentContext(String sessionId) {
        int messageLimit = Math.max(memoryTurns == null ? 10 : memoryTurns, 1) * 2;
        Page<AiChatRecord> page = aiChatRecordService.page(
                new Page<>(1, messageLimit, false),
                new LambdaQueryWrapper<AiChatRecord>()
                        .eq(AiChatRecord::getSessionId, sessionId)
                        .orderByDesc(AiChatRecord::getCreateTime)
        );
        List<AiChatRecord> records = new ArrayList<>(page.getRecords());
        Collections.reverse(records);
        return records;
    }

    private void saveConversationTurn(Long userId, String sessionId, String userMessage, String assistantReply) {
        aiChatRecordService.saveBatch(List.of(
                buildRecord(userId, sessionId, "user", userMessage),
                buildRecord(userId, sessionId, "assistant", assistantReply)
        ));
    }

    private AiChatRecord buildRecord(Long userId, String sessionId, String role, String content) {
        AiChatRecord record = new AiChatRecord();
        record.setUserId(userId);
        record.setSessionId(sessionId);
        record.setRole(role);
        record.setContent(content);
        return record;
    }

    private String callAgent(AiChatRequest request, List<AiChatRecord> context, String selectedModelName) {
        try {
            OpenAiChatModel model = OpenAiChatModel.builder()
                    .apiKey(apiKey)
                    .baseUrl(normalizeBaseUrl(baseUrl))
                    .modelName(selectedModelName)
                    .temperature(temperature)
                    .maxTokens(maxTokens)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .build();

            AiCustomerAgent agent = AiServices.builder(AiCustomerAgent.class)
                    .chatModel(model)
                    .systemMessage(buildSystemPrompt(request.getPetProfile(), context))
                    .tools(aiMallToolService.toolsFor(request.getUserId()))
                    .maxToolCallingRoundTrips(4)
                    .build();

            String content = agent.chat(request.getMessage());
            if (!StringUtils.hasText(content)) {
                throw new BusinessException(503, "AI服务调用失败，请稍后重试", "AI model returned empty response");
            }
            return content;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(503, "AI服务调用失败，请稍后重试", ex.getMessage());
        }
    }

    private List<ChatMessage> buildChatMessages(AiChatRequest request, List<AiChatRecord> context) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(SystemMessage.from(buildSystemPrompt(request.getPetProfile(), context)));
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

    private String buildSystemPrompt(String petProfile, List<AiChatRecord> context) {
        String profile = StringUtils.hasText(petProfile) ? "\n当前宠物档案：" + petProfile + "。" : "";
        return systemPromptTemplate + profile + toolInstructions() + recentContext(context);
    }

    private String toolInstructions() {
        return """

                你可以使用已提供的商城工具查询真实数据，包括商品搜索、商品详情、店铺搜索、店铺详情、视频 feed、视频详情、视频评论和购物车查询。
                当用户要求加购物车、修改购物车、删除购物车或创建订单时，只能调用 request_* 工具生成待确认动作；不要声称已经完成操作。
                工具返回 requiresConfirmation 时，必须提醒用户确认后才会执行。
                不要编造商品库存、价格、店铺、视频、购物车或订单信息；需要这些信息时优先调用工具。
                """;
    }

    private String recentContext(List<AiChatRecord> context) {
        if (context == null || context.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder("\n最近对话上下文：\n");
        for (AiChatRecord record : context) {
            builder.append(record.getRole()).append(": ").append(record.getContent()).append('\n');
        }
        return builder.toString();
    }

    private String loadSystemPromptTemplate() {
        try {
            return new String(systemPromptResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (IOException ex) {
            throw new BusinessException(500, "AI系统提示词加载失败", ex.getMessage());
        }
    }

    private AiChatResponse buildResponse(String sessionId,
                                         String reply,
                                         String message,
                                         List<AiChatResponse.PendingAction> pendingActions) {
        AiChatResponse response = new AiChatResponse();
        response.setSessionId(sessionId);
        response.setReply(reply);
        response.setSuggestions(List.of("继续追问照护步骤", "生成一周照护计划", "推荐需要准备的用品"));
        response.setRecommendations(defaultRecommendations(message));
        response.setPendingActions(pendingActions == null ? List.of() : pendingActions);
        return response;
    }

    private AiChatResponse buildLoginRequiredResponse(String sessionId) {
        AiChatResponse response = new AiChatResponse();
        response.setSessionId(sessionId);
        response.setReply("请先登录后再使用智能客服。我可以帮你查询商品、店铺、视频、购物车和订单，但需要登录后才能确认你的身份。");
        response.setSuggestions(List.of("登录后继续咨询", "查看商品推荐", "返回首页"));
        response.setRecommendations(List.of());
        response.setPendingActions(List.of());
        return response;
    }

    private boolean isLoggedIn(AiChatRequest request) {
        return request != null && request.getUserId() != null;
    }

    private void emitChunks(String reply, StreamListener listener) {
        String text = reply == null ? "" : reply;
        int chunkSize = 12;
        for (int index = 0; index < text.length(); index += chunkSize) {
            listener.onDelta(text.substring(index, Math.min(index + chunkSize, text.length())));
        }
        if (!StringUtils.hasText(text)) {
            listener.onDelta("");
        }
    }

    private List<AiChatResponse.Recommendation> defaultRecommendations(String message) {
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

    private void ensureAiAvailable() {
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException(503, "AI功能目前不可用", "未配置 AI API Key");
        }
    }

    private String chooseModelName(String modelMode) {
        if ("pro".equalsIgnoreCase(modelMode)) {
            return StringUtils.hasText(proModelName) ? proModelName : modelName;
        }
        return StringUtils.hasText(flashModelName) ? flashModelName : modelName;
    }
}
