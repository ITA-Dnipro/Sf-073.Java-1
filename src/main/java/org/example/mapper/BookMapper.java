package org.example.mapper;

import org.example.lib.service.Mapper;

import org.example.model.Book;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookMapper implements Mapper {
    @Override
    public Book mapRow(ResultSet resultSet) throws SQLException {
        Book currBook = new Book();
        currBook.setTitle(resultSet.getString("title"));
        currBook.setId(resultSet.getLong("id"));
        currBook.setPublishedAt(resultSet.getDate("published_at").toLocalDate());
        return currBook;
    }

    @Override
    public List<Book> mapRows(ResultSet resultSet) throws SQLException {
        List<Book> bookList = new ArrayList<>();

        while(resultSet.next()){
            bookList.add(mapRow(resultSet));
        }

        return bookList;
    }
}
