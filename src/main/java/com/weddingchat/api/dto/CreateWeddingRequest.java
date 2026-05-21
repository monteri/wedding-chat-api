package com.weddingchat.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateWeddingRequest(
        @NotBlank
        @Size(min = 3, max = 80)
        @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug must be lowercase letters, numbers, and hyphens.")
        String slug,
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(min = 4, max = 120) String accessCode
) {
}
