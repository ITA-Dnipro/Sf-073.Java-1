package org.example.lib;


import org.assertj.db.type.Table;
import org.example.lib.exceptions.ObjectAlreadyExistException;
import org.example.lib.utils.Utils;
import org.example.model.Book;
import org.example.model.Publisher;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;


import java.sql.SQLException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ORManagerImplTest {

    static ORManagerImpl orManager;
    static JdbcDataSource dataSource;
    Book bookOne;
    Book bookTwo;
    Publisher publisher;
    Table table;

    @BeforeAll
    static void beforeAll() throws SQLException {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:file:./ORMAnager");
        dataSource.setUser("sa");
        dataSource.setPassword("password");

        orManager = new ORManagerImpl(dataSource);
        orManager.register(Book.class, Publisher.class);
    }

    @BeforeEach
    void setUp() throws SQLException {
        table = new Table(dataSource, "BOOKS");
        bookOne = new Book("Lotr", LocalDate.of(1961, 1, 1));
        bookTwo = new Book("Got", LocalDate.of(1981, 5, 4));
        publisher = new Publisher();
    }


    @Test
    void given_fileName_ORMangerWithPropertiesFrom_then_connect() {

        String fileName = "db.properties";
        // load properties from file
        ORManager orm = ORManager.withPropertiesFrom(fileName);

        // make sure the connection is valid
        assertTrue(orm.checkConnectionToDB());
    }

    @Test
    void given_datasource_when_getORMImplementation_then_connect() {

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
    void given_book_when_persisted_then_book_saved_to_database() {

        orManager.persist(bookOne);

        assertThat(bookOne.getId()).isGreaterThan(0);
        assertThat(bookOne.getPublishedAt()).isEqualTo(LocalDate.of(1961, 1, 1));
        assertThat(bookOne.getTitle()).isEqualTo("Lotr");
    }

    @Test
    void given_two_books_when_persisted_then_books_saved_to_database() {
        orManager.persist(bookOne);
        orManager.persist(bookTwo);

        assertThat(bookOne.getId()).isNotEqualTo(bookTwo.getId());
    }

    @Test
    void given_the_same_book_twice_when_persisted_then_book_not_saved_custom_exception_thrown() {
        orManager.persist(bookOne);
        Exception exception = assertThrows(
                ObjectAlreadyExistException.class,
                () -> orManager.persist(bookOne)
        );

        assertEquals("Try to persist an existing object. Object " + bookOne + " already exist in database!", exception.getMessage());
    }

    @Test
    void given_book_id_when_delete_then_remove_from_database() {
        boolean res = orManager.delete(bookOne);

        assertTrue(res);
    }

    @Test
    void given_book_id_null_when_delete_then_error() {
        bookOne.setId(null);

        boolean res = orManager.delete(bookOne);

        assertFalse(res);
    }

    @Test
    void given_multiple_books_when_delete_then_remove_from_database() {

        orManager.persist(bookOne);
        orManager.persist(bookTwo);

        int numberOfDeletions = 2;

        var startCount = orManager.getCount(bookOne);
        orManager.delete(bookOne);
        orManager.delete(bookTwo);
        var endCount = orManager.getCount(bookOne);


        assertThat(endCount).isEqualTo(startCount - numberOfDeletions);
    }

}