package com.weddingchat.api.dto;

import java.util.UUID;

public record JoinWeddingResponse(
        UUID weddingId,
        UUID guestId,
        String displayName,
        String sessionToken
) {
}
