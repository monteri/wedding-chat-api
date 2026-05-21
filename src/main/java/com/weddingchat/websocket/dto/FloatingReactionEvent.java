package com.weddingchat.websocket.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FloatingReactionEvent(
        UUID weddingId,
        UUID guestId,
        String guestDisplayName,
        String reaction,
        OffsetDateTime createdAt
) {
}
