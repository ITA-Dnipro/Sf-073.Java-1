package org.example.lib.utils;

import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

@Slf4j
public class Repository {
    private final DataSource dataSource;

    public Repository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public boolean update(String sql, List<Object> params) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.info(sql);
            int index = 1;
            for (Object param : params) {
                if (param == null){
                    pstmt.setNull(index++,0);
                }else{
                    pstmt.setObject(index++, param, SQLUtils.getSQLType(param.getClass()));
                }
            }
            return pstmt.executeUpdate(sql) > 0;
        } catch (SQLException se) {
            //Handle errors for JDBC
            log.error("An error while update DB "+se.getMessage());
        }
        return false;
    }

    public boolean update(String sql) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            log.info(sql);
            return stmt.executeUpdate(sql) > 0;
        } catch (SQLException se) {
            //Handle errors for JDBC
            log.error("An error while update DB "+se.getMessage());
        }
        return false;
    }

    public boolean checkConnection() {
        try (Connection conn = getConnection()) {
            return conn.isValid(3);
        } catch (SQLException se) {
            //Handle errors for JDBC
            log.error("An error while connection to DB "+se.getMessage());
        }
        return false;
    }
}
