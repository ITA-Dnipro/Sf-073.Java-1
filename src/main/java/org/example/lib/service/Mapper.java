package org.example.lib.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public interface Mapper <T> {
    T mapID(ResultSet resultSet) throws SQLException;
    T mapRow(ResultSet resultSet) throws SQLException;

    default List<T> mapRows(ResultSet resultSet) throws SQLException {
        List<T> objectList = new ArrayList<>();
        while (resultSet.next()) {
            objectList.add(mapRow(resultSet));
        }
        return objectList;
    }
}
