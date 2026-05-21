package com.weddingchat.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinWeddingRequest(
        @NotBlank @Size(max = 120) String accessCode,
        @NotBlank @Size(min = 2, max = 60) String displayName
) {
}
