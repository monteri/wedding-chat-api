package com.weddingchat.api.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID weddingId,
        UUID guestId,
        String guestDisplayName,
        String content,
        OffsetDateTime createdAt,
        List<MessageReactionResponse> reactions
) {
}
