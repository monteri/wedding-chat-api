package com.weddingchat.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class DatabaseUrlSupportTest {

    @Test
    void leavesJdbcUrlUnchanged() {
        String url = "jdbc:postgresql://localhost:5433/wedding_chat?sslmode=disable";
        assertThat(DatabaseUrlSupport.toJdbcUrl(url)).isEqualTo(url);
    }

    @Test
    void prefixesPostgresqlUrlWithJdbc() {
        assertThat(DatabaseUrlSupport.toJdbcUrl("postgresql://user:pass@db.example.com:5432/railway"))
                .isEqualTo("jdbc:postgresql://user:pass@db.example.com:5432/railway");
    }

    @Test
    void rejectsUnsupportedScheme() {
        assertThatThrownBy(() -> DatabaseUrlSupport.toJdbcUrl("mysql://localhost/db"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("jdbc:");
    }
}
