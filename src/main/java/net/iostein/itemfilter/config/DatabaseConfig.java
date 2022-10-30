package net.iostein.itemfilter.config;

import org.jetbrains.annotations.NotNull;

public class DatabaseConfig {

    private final String jdbcUrl;

    public DatabaseConfig() {
        this.jdbcUrl = "jdbc:postgresql://localhost/test?user=fred&password=secret&ssl=true";
    }

    @NotNull
    public String getJdbcUrl() {
        return this.jdbcUrl;
    }

}
