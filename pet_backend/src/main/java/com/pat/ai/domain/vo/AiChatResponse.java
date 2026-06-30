package com.pat.ai.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class AiChatResponse {
    private String sessionId;
    private String reply;
    private List<String> suggestions;
    private List<Recommendation> recommendations;
    private List<PendingAction> pendingActions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Recommendation {
        private String title;
        private String reason;
        private String tag;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingAction {
        private String id;
        private String type;
        private String label;
        private String summary;
        private Object payload;
    }
}
