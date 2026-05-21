package com.weddingchat.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MessageReactionResponse(
        UUID id,
        UUID messageId,
        UUID guestId,
        String guestDisplayName,
        String reaction,
        OffsetDateTime createdAt
) {
}
