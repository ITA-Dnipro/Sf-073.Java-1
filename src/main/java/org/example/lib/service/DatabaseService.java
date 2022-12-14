package org.example.lib.service;

import lombok.extern.slf4j.Slf4j;
import org.example.lib.utils.SQLUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DatabaseService {
    private final DataSource dataSource;

    public DatabaseService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public boolean checkConnection() {
        try (Connection conn = getConnection()){
            return conn.isValid(10);
        } catch (SQLException e) {
            log.error("An error while getting connection " + e);
            return false;
        }
    }

    public boolean update(String sql, List<Object> params) {
        log.info(sql);
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParametersForPrepareStatement(pstmt, params);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException se) {
            //Handle errors for JDBC
            log.error("An error while update object DB " + se);
        }
        return false;
    }

    public <T> T updateAndGetObjectWithID(String sql, List<Object> params, Mapper<T> mapper) {
        log.info(sql);
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            setParametersForPrepareStatement(pstmt, params);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                //to do - remove SQLException throw in this case - use logger and return
                throw new SQLException("Updating object failed, no rows affected.");
            }
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return mapper.mapID(generatedKeys);
                } else {
                    throw new SQLException("Updating object failed, no ID obtained.");
                }
            }
        } catch (SQLException se) {
            //Handle errors for JDBC
            log.error("An error while update and get ID for object DB " + se);
        }
        return null;
    }

    public boolean update(String sql) {
        if (sql.isEmpty()) return false;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            log.info(sql);
            return stmt.executeUpdate(sql) > 0;
        } catch (SQLException se) {
            //Handle errors for JDBC
            log.error("An error while update DB " + se);
        }
        return false;
    }

    public <T> T queryForObject(String sql, List<Object> params, Mapper<T> mapper) {
        log.info(sql);
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParametersForPrepareStatement(pstmt, params);
            ResultSet resultSet = pstmt.executeQuery();

            if (resultSet.next()) return mapper.mapRow(resultSet);
            log.warn("resultSet is empty!");
            return null;
        } catch (SQLException se) {
            //Handle errors for JDBC
            log.error("An error while queryForObject " + se);
        }
        return null;
    }

    public <T> List<T> queryForList(String sql, List<Object> params, Mapper<T> mapper) {
        log.info(sql);
        List<T> objectList = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParametersForPrepareStatement(pstmt, params);
            ResultSet resultSet = pstmt.executeQuery();
            return mapper.mapRows(resultSet);
        } catch (SQLException se) {
            //Handle errors for JDBC
            log.error("An error while query queryForList " + se);
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

    public long count(String sql){
        long count = 0;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                count = rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }
}
