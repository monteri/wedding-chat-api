package com.weddingchat.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.core.env.Environment;

public final class DatabaseUrlSupport {

    private static final String LOCAL_JDBC_URL =
            "jdbc:postgresql://localhost:5433/wedding_chat?sslmode=disable";

    private DatabaseUrlSupport() {
    }

    public static String resolveJdbcUrl(Environment environment) {
        String rawUrl = firstNonBlank(
                environment.getProperty("DB_URL"),
                environment.getProperty("DATABASE_URL")
        );
        if (rawUrl != null) {
            return ensureSslParams(toJdbcUrl(rawUrl));
        }
        return environment.getProperty("spring.datasource.url", LOCAL_JDBC_URL);
    }

    public static String resolveUsername(Environment environment, String jdbcUrl) {
        String configured = environment.getProperty("DB_USERNAME");
        if (!isBlank(configured)) {
            return configured.trim();
        }
        return credentialsFromUrl(jdbcUrl)
                .map(Credentials::username)
                .orElseGet(() -> environment.getProperty("spring.datasource.username", "wedding_chat"));
    }

    public static String resolvePassword(Environment environment, String jdbcUrl) {
        String configured = environment.getProperty("DB_PASSWORD");
        if (!isBlank(configured)) {
            return configured;
        }
        return credentialsFromUrl(jdbcUrl)
                .map(Credentials::password)
                .orElseGet(() -> environment.getProperty("spring.datasource.password", "wedding_chat"));
    }

    /** Safe for logs: host and database only. */
    public static String describeConnection(String jdbcUrl) {
        try {
            URI uri = URI.create(jdbcUrl.substring("jdbc:".length()));
            String host = uri.getHost() == null ? "unknown" : uri.getHost();
            int port = uri.getPort() > 0 ? uri.getPort() : 5432;
            String path = uri.getPath() == null ? "" : uri.getPath();
            return host + ":" + port + path;
        } catch (RuntimeException ex) {
            return "(invalid jdbc url)";
        }
    }

    private static String ensureSslParams(String jdbcUrl) {
        if (jdbcUrl.contains("localhost")
                || jdbcUrl.contains("127.0.0.1")
                || jdbcUrl.contains(".railway.internal")) {
            return jdbcUrl;
        }
        if (jdbcUrl.contains("sslmode=")) {
            return jdbcUrl;
        }
        return jdbcUrl + (jdbcUrl.contains("?") ? "&" : "?") + "sslmode=require";
    }

    public static String toJdbcUrl(String url) {
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

    private static Optional<Credentials> credentialsFromUrl(String jdbcUrl) {
        try {
            URI uri = URI.create(jdbcUrl.substring("jdbc:".length()));
            String userInfo = uri.getUserInfo();
            if (userInfo == null || userInfo.isBlank()) {
                return Optional.empty();
            }
            String[] parts = userInfo.split(":", 2);
            String username = decode(parts[0]);
            String password = parts.length > 1 ? decode(parts[1]) : "";
            return Optional.of(new Credentials(username, password));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
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
