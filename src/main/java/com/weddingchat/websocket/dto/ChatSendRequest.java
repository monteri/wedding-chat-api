package com.weddingchat.websocket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ChatSendRequest(
        @NotNull UUID weddingId,
        @NotBlank @Size(max = 80) String sessionToken,
        @NotBlank @Size(max = 1000) String content
) {
}
