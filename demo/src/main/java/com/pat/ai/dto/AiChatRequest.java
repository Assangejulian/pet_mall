package com.pat.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiChatRequest {
    private Long userId;
    private String sessionId;
    private String petProfile;
    private String modelMode;

    @NotBlank(message = "消息不能为空")
    private String message;
}
