package com.weddingchat.api.dto;

import java.util.UUID;

public record DeleteMessageResponse(UUID messageId, String status) {
}
