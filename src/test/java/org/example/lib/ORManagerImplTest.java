package org.example.lib;


import org.example.lib.utils.Utils;
import org.example.model.Book;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ORManagerImplTest {

    @Test
    void given_fileName_ORMangerWithPropertiesFrom_then_connect() {

        String fileName = "db.properties";
        // load properties from file
        ORManager orm = ORManager.withPropertiesFrom(fileName);

        // make sure the connection is valid
        assertTrue(orm.checkConnectionToDB());
    }

    @Test
    void given_datasource_when_getORMImplementation_then_connect()  {

        JdbcDataSource dataSource = new JdbcDataSource();

        dataSource.setURL("jdbc:h2:file:./ORMAnager");
        dataSource.setUser("sa");
        dataSource.setPassword("password");
        ORManager res = Utils.getORMImplementation(dataSource);


        assertTrue(res.checkConnectionToDB());
    }


    @Test
    void given_empty_fileName_ORMangerWithPropertiesFrom_then_connection_invalid() {
        // empty file name
        String fileName = "";

        ORManager orm = ORManager.withPropertiesFrom(fileName);

        assertThrows(NullPointerException.class,
                orm::checkConnectionToDB);
    }


    @Test
    void given_empty_datasource_when_getORMImplementation_then_connection_invalid() {

        JdbcDataSource dataSource = new JdbcDataSource();

        ORManager res = Utils.getORMImplementation(dataSource);

        assertFalse(res.checkConnectionToDB());
    }

    @Test
    void testFindById() {
        //Setup ORManager
        String fileName = "db.properties";
        ORManager orm = ORManager.withPropertiesFrom(fileName);

        // Create a new Book object with a unique id and title

        long id = System.currentTimeMillis();
        String title = "My Book";
        Book book = new Book(title, LocalDate.now());
        orm.persist(book);
        book.setId(id);
        orm.merge(book);

        // Find the Book object by id
        Optional<Book> foundBook = orm.findById(id, Book.class);

        // Assert that the found Book object is present and has the same id and title as the original Book object
        assertTrue(foundBook.isPresent());
        assertEquals(id, foundBook.get().getId());
        assertEquals(title, foundBook.get().getTitle());
    }

    @Test
    void testFindAll() {

        //Setup ORManager
        String fileName = "db.properties";
        ORManager orm = ORManager.withPropertiesFrom(fileName);

        // Create a list of new Book objects with unique ids and titles
        List<Book> books = Arrays.asList(
                new Book("Book 1", LocalDate.now()),
                new Book("Book 2", LocalDate.now()),
                new Book("Book 3", LocalDate.now())
        );

        orm.persist(books.get(0));
        orm.persist(books.get(1));
        orm.persist(books.get(2));

        // Find all the Book objects in the database
        List<Book> foundBooks = orm.findAll(Book.class);

        // Assert that the found Book objects have the same number of elements as the original list of Book objects
        assertEquals(books.size(), foundBooks.size());

        // Assert that the found Book objects have the same ids and titles as the original Book objects
        for (int i = 0; i < books.size(); i++) {
            assertEquals(books.get(i).getId(), foundBooks.get(i).getId());
            assertEquals(books.get(i).getTitle(), foundBooks.get(i).getTitle());
        }
    }

}