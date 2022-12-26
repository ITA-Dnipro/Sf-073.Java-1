package org.example.lib.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface Mapper <T> {
    T mapRow(ResultSet resultSet) throws SQLException;
    List<T> mapRows(ResultSet resultSet) throws SQLException;
}
