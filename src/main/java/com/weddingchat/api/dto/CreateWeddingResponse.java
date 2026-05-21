package com.weddingchat.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateWeddingResponse(
        UUID id,
        String slug,
        String name,
        OffsetDateTime createdAt
) {
}
