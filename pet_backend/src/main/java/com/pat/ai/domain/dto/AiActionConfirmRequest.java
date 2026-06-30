package com.pat.ai.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiActionConfirmRequest {
    private Long userId;

    @NotBlank(message = "actionId cannot be blank")
    private String actionId;
}
