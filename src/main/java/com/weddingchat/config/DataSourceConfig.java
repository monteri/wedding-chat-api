package com.weddingchat.config;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Bean
    public DataSource dataSource(Environment environment) {
        String jdbcUrl = DatabaseUrlSupport.resolveJdbcUrl(environment);
        String username = DatabaseUrlSupport.resolveUsername(environment, jdbcUrl);
        String password = DatabaseUrlSupport.resolvePassword(environment, jdbcUrl);

        log.info(
                "Connecting to PostgreSQL at {} (user={})",
                DatabaseUrlSupport.describeConnection(jdbcUrl),
                username
        );

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }
}
