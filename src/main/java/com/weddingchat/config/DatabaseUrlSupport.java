package com.weddingchat.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

final class DatabaseUrlSupport {

    private static final String PROPERTY_SOURCE = "normalizedDatabaseUrl";

    private DatabaseUrlSupport() {
    }

    static void apply(ConfigurableEnvironment environment) {
        String rawUrl = firstNonBlank(
                environment.getProperty("DB_URL"),
                environment.getProperty("DATABASE_URL")
        );
        if (rawUrl == null) {
            return;
        }

        String jdbcUrl = toJdbcUrl(rawUrl);
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("spring.datasource.url", jdbcUrl);

        if (isBlank(environment.getProperty("DB_USERNAME"))) {
            credentialsFromUrl(jdbcUrl).ifPresent(credentials -> {
                properties.put("spring.datasource.username", credentials.username());
                properties.put("spring.datasource.password", credentials.password());
            });
        }

        environment.getPropertySources().remove(PROPERTY_SOURCE);
        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE, properties));
    }

    static String toJdbcUrl(String url) {
        String trimmed = url.trim();
        if (trimmed.startsWith("jdbc:")) {
            return trimmed;
        }
        if (trimmed.startsWith("postgres://") || trimmed.startsWith("postgresql://")) {
            return "jdbc:" + trimmed;
        }
        throw new IllegalArgumentException(
                "Database URL must start with jdbc:, postgresql://, or postgres:// (got: " + trimmed + ")"
        );
    }

    private static java.util.Optional<Credentials> credentialsFromUrl(String jdbcUrl) {
        try {
            URI uri = URI.create(jdbcUrl.substring("jdbc:".length()));
            String userInfo = uri.getUserInfo();
            if (userInfo == null || userInfo.isBlank()) {
                return java.util.Optional.empty();
            }
            String[] parts = userInfo.split(":", 2);
            String username = decode(parts[0]);
            String password = parts.length > 1 ? decode(parts[1]) : "";
            return java.util.Optional.of(new Credentials(username, password));
        } catch (IllegalArgumentException ex) {
            return java.util.Optional.empty();
        }
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record Credentials(String username, String password) {
    }
}
