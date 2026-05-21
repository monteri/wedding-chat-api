package com.weddingchat.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String frontendOrigins = "http://localhost:5173";
    private String adminPassword = "change-me";
    private final RateLimit rateLimit = new RateLimit();

    public String getFrontendOrigins() {
        return frontendOrigins;
    }

    public void setFrontendOrigins(String frontendOrigins) {
        this.frontendOrigins = frontendOrigins;
    }

    public List<String> getFrontendOriginList() {
        return Arrays.stream(frontendOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
    }

    public String[] getFrontendOriginArray() {
        return getFrontendOriginList().toArray(String[]::new);
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public static class RateLimit {
        private int messagePerMinute = 12;
        private int floatingPerMinute = 30;

        public int getMessagePerMinute() {
            return messagePerMinute;
        }

        public void setMessagePerMinute(int messagePerMinute) {
            this.messagePerMinute = messagePerMinute;
        }

        public int getFloatingPerMinute() {
            return floatingPerMinute;
        }

        public void setFloatingPerMinute(int floatingPerMinute) {
            this.floatingPerMinute = floatingPerMinute;
        }
    }
}
