package org.example.lib.service;

import lombok.extern.slf4j.Slf4j;
import org.example.lib.utils.SQLUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
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
            setParametersForPrepareStatement(pstmt, params);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException se) {
            //Handle errors for JDBC
            log.error("An error while update DB " + se.getMessage());
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
            log.error("An error while update DB " + se.getMessage());
        }
        return false;
    }

    public <T> T queryForObject(String sql, List<Object> params, Mapper<T> mapper) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.info(sql);
            setParametersForPrepareStatement(pstmt, params);
            ResultSet resultSet = pstmt.executeQuery();

            if (resultSet.next()) return mapper.mapRow(resultSet);
            log.warn("resultSet is empty!");
            return null;
        } catch (SQLException se) {
            //Handle errors for JDBC
            log.error("An error while queryForObject " + se.getMessage());
        }
        return null;
    }

    public <T> List<T> queryForList(String sql, List<Object> params, Mapper<T> mapper) {
        List<T> objectList = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParametersForPrepareStatement(pstmt, params);
            ResultSet resultSet = pstmt.executeQuery();
            return mapper.mapRows(resultSet);
        } catch (SQLException se) {
            //Handle errors for JDBC
            log.error("An error while query queryForList " + se.getMessage());
        }

        return objectList;
    }

    private void setParametersForPrepareStatement(PreparedStatement pstmt, List<Object> params) throws SQLException {
        int index = 1;
        for (Object param : params) {
            if (param == null) {
                pstmt.setNull(index++, 0);
            } else {
                pstmt.setObject(index++, param, SQLUtils.getSQLType(param.getClass()));
            }
        }
    }
}
