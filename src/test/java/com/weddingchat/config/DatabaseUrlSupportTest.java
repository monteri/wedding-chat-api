package com.weddingchat.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

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
    void prefersDbUrlOverDatabaseUrl() {
        MockEnvironment env = new MockEnvironment()
                .withProperty("DB_URL", "postgresql://postgres:secret@postgres.railway.internal:5432/railway")
                .withProperty("DATABASE_URL", "postgresql://ignored:ignored@ignored:5432/ignored");

        String jdbcUrl = DatabaseUrlSupport.resolveJdbcUrl(env);

        assertThat(jdbcUrl).isEqualTo("jdbc:postgresql://postgres:secret@postgres.railway.internal:5432/railway");
        assertThat(DatabaseUrlSupport.describeConnection(jdbcUrl))
                .isEqualTo("postgres.railway.internal:5432/railway");
    }

    @Test
    void usesLocalDefaultWhenNoEnvUrl() {
        MockEnvironment env = new MockEnvironment();

        assertThat(DatabaseUrlSupport.resolveJdbcUrl(env)).contains("localhost:5433");
    }

    @Test
    void extractsCredentialsFromUrlWhenUsernameNotSet() {
        MockEnvironment env = new MockEnvironment()
                .withProperty("DB_URL", "postgresql://postgres:secret@postgres.railway.internal:5432/railway");
        String jdbcUrl = DatabaseUrlSupport.resolveJdbcUrl(env);

        assertThat(DatabaseUrlSupport.resolveUsername(env, jdbcUrl)).isEqualTo("postgres");
        assertThat(DatabaseUrlSupport.resolvePassword(env, jdbcUrl)).isEqualTo("secret");
    }

    @Test
    void prefersUrlCredentialsOverRailwayDbUsername() {
        MockEnvironment env = new MockEnvironment()
                .withProperty("DB_URL", "postgresql://postgres:secret@postgres.railway.internal:5432/railway")
                .withProperty("DB_USERNAME", "railway")
                .withProperty("DB_PASSWORD", "wrong-password");
        String jdbcUrl = DatabaseUrlSupport.resolveJdbcUrl(env);

        assertThat(DatabaseUrlSupport.resolveUsername(env, jdbcUrl)).isEqualTo("postgres");
        assertThat(DatabaseUrlSupport.resolvePassword(env, jdbcUrl)).isEqualTo("secret");
    }

    @Test
    void rejectsUnsupportedScheme() {
        assertThatThrownBy(() -> DatabaseUrlSupport.toJdbcUrl("mysql://localhost/db"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("jdbc:");
    }
}
