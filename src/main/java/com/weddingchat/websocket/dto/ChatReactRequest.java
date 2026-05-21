package com.weddingchat.websocket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ChatReactRequest(
        @NotNull UUID weddingId,
        @NotNull UUID messageId,
        @NotBlank @Size(max = 80) String sessionToken,
        @NotBlank @Size(max = 20) String reaction
) {
}
