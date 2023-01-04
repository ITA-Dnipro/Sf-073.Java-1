package org.example.mapper;

import org.example.lib.service.Mapper;
import org.example.model.Publisher;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PublisherMapper implements Mapper<Publisher>  {
    @Override
    public Publisher mapID(ResultSet resultSet) throws SQLException {
        Publisher currPub = new Publisher();
        currPub.setId(resultSet.getLong("id"));
        return currPub;
    }

    @Override
    public Publisher mapRow(ResultSet resultSet) throws SQLException {
        Publisher currPub = new Publisher();
        currPub.setName(resultSet.getString("name"));
        currPub.setId(resultSet.getLong("id"));
        //currPub.setBooks(resultSet.getDate("published_at").toLocalDate());
        return currPub;
    }
}
