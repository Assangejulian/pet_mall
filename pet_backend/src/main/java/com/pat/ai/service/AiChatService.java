package com.pat.ai.service;

import com.pat.ai.domain.dto.AiChatRequest;
import com.pat.ai.domain.vo.AiChatResponse;

public interface AiChatService {
    AiChatResponse chat(AiChatRequest request);

    void chatStream(AiChatRequest request, StreamListener listener);

    void deleteSession(String sessionId);

    interface StreamListener {
        void onMeta(String sessionId);

        void onDelta(String content);

        void onComplete(AiChatResponse response);
    }
}
