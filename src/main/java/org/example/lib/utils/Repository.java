package org.example.lib.utils;

import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
public class Repository {
    private final DataSource dataSource;

    public Repository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public int update(String sql) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            log.info(sql);
            return stmt.executeUpdate(sql);
        } catch (SQLException se) {
            //Handle errors for JDBC
            log.error("An error while update DB "+se.getMessage());
        }
        return 0;
    }
}
