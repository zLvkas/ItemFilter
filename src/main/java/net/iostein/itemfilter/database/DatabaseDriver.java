package net.iostein.itemfilter.database;

import com.zaxxer.hikari.HikariDataSource;
import net.iostein.itemfilter.config.DatabaseConfig;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseDriver {

    private final HikariDataSource hikariDataSource;

    public DatabaseDriver(@NotNull DatabaseConfig databaseConfig) {
        System.out.println(databaseConfig.getJdbcUrl());
        this.hikariDataSource = new HikariDataSource();

        this.hikariDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        this.hikariDataSource.setJdbcUrl(databaseConfig.getJdbcUrl());

        this.hikariDataSource.validate();
    }

    @NotNull
    public Connection getConnection() throws SQLException {
        return this.hikariDataSource.getConnection();
    }

    public void close() {
        this.hikariDataSource.close();
    }

}