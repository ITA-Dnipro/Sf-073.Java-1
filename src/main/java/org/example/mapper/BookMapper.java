package org.example.mapper;

import org.example.lib.service.Mapper;

import org.example.model.Book;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookMapper implements Mapper<Book>  {
    @Override
    public Book mapID(ResultSet resultSet) throws SQLException {
        Book currBook = new Book();
        currBook.setId(resultSet.getLong("id"));
        return currBook;
    }

    @Override
    public Book mapRow(ResultSet resultSet) throws SQLException {
        Book currBook = new Book();
        currBook.setTitle(resultSet.getString("title"));
        currBook.setId(resultSet.getLong("id"));
        currBook.setPublishedAt(resultSet.getDate("published_at").toLocalDate());
        return currBook;
    }

}
