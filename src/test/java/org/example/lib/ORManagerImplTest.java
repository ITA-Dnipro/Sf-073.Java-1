package org.example.lib;

import static org.assertj.core.api.Assertions.assertThat;

import org.example.lib.annotations.*;
import org.example.lib.exceptions.ObjectAlreadyExistException;
import org.example.lib.exceptions.RegisterClassException;
import org.example.lib.utils.AnnotationsUtils;
import org.example.lib.utils.DbUtils;
import org.example.lib.utils.SQLUtils;
import org.example.lib.utils.Utils;
import org.example.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ORManagerImplTest {
    private ORManager orm;
    private String propertiesFileName;

    @BeforeEach
    void setUp() {
        this.propertiesFileName = "db_test.properties";
        // load properties from file
        this.orm = Utils.getORMImplementation(propertiesFileName);
    }

    @AfterEach
    void deleteDatabase() throws SQLException {
        DbUtils.clearDatabase(propertiesFileName);
    }

    @Test
    void register_Not_All_Classes_With_Reference_And_Get_Exception() {
        var status = false;
        try {
            orm.register(Book.class);
        } catch (RegisterClassException e) {
            status = true;
        }
        assertTrue(status);
    }

    @Test
    void register_Class_With_Incorrect_Reference_And_Get_Exception() {
        var status = false;
        @Entity
        class TestClassWithReference {
            @Id
            private int idTable;
            private String name;
            @ManyToOne
            private TestDBWithoutID reference;
        }

        try {
            orm.register(TestClassWithReference.class,TestDBWithoutID.class);
        } catch (RegisterClassException e) {
            status = true;
        }
        assertTrue(status);
    }

    @Test
    void register_Class_Without_Entity_And_Get_Exception() {
        var status = false;
        class TestClassWithReference {
            @Id
            private UUID idTable;
        }

        try {
            orm.register(TestClassWithReference.class);
        } catch (RegisterClassException e) {
            status = true;
        }
        assertTrue(status);
    }

    @Test
    void persist_Object_With_AutoIncrementID_And_Get_Not_Null_ID() {
        orm.register(TestDBWithID.class);

        TestClass newObject = new TestDBWithID("Name 1");

        orm.persist(newObject);

        assertNotNull(newObject.getId());
    }

    @Test
    void persist_Object_With_UUID_And_Get_Not_Null_ID() {
        orm.register(TestDBWithUUID.class);

        TestClass newObject = new TestDBWithUUID("Name 1");

        orm.persist(newObject);

        assertNotNull(newObject.getId());
    }

    @Test
    void persist_Object_WithoutID_And_Get_Null_ID() {
        orm.register(TestDBWithoutID.class);

        TestClass newObject = new TestDBWithoutID("Name 1");

        orm.persist(newObject);

        Field idField = AnnotationsUtils.getFieldByAnnotation(newObject, Id.class);
        assertNull(idField);
    }

    @Test
    void persist_AnExisting_Object_WithID_And_Get_Exception() {
        orm.register(TestDBWithID.class);

        TestClass newObject = new TestDBWithID("Name 1");

        orm.persist(newObject);
        var status = false;
        try {
            orm.persist(newObject);
        } catch (ObjectAlreadyExistException e) {
            status = true;
        }
        assertTrue(status);
    }

    @Test
    void save_One_Object_To_Database_And_Return_Id() {
        orm.register(TestDBWithID.class);

        TestClass savedObject = orm.save(new TestDBWithID("Name 1"));

        assertNotNull(savedObject.getId());
    }

    @Test
    void save_Two_Object_To_Database_And_Return_Different_Ids() {
        orm.register(TestDBWithID.class);

        TestClass savedObject1 = orm.save(new TestDBWithID("Name 1"));
        TestClass savedObject2 = orm.save(new TestDBWithID("Name 2"));

        assertNotNull(savedObject1.getId());

        assertNotNull(savedObject2.getId());

        assertNotEquals(savedObject1.getId(), savedObject2.getId());
    }

    @Test
    void save_One_Object_To_DatabaseTwo_Times_And_Return_Same_Id() {
        orm.register(TestDBWithID.class);

        TestClass savedObject = new TestDBWithID("Name 1");

        orm.save(savedObject);
        var currId = savedObject.getId();
        assertNotNull(currId);
        orm.save(savedObject);
        assertNotNull(savedObject.getId());
        assertEquals(currId, savedObject.getId());
    }

    @Test
    void can_Find_Object_By_Id() {
        orm.register(TestDBWithID.class);

        TestClass savedObject = orm.save(new TestDBWithID("Name 1"));

        Optional<TestDBWithID> foundObject = orm.findById((Serializable) savedObject.getId(), TestDBWithID.class);
        assertTrue(foundObject.isPresent());
        assertNotEquals(foundObject.get(), savedObject);
    }

    @Test
    void can_Not_Find_Object_By_Id() {
        orm.register(TestDBWithID.class);

        Optional<TestDBWithID> objectToBeFound = orm.findById(-1L, TestDBWithID.class);
        assertFalse(objectToBeFound.isPresent());
    }

    @Test
    void Merge_Object_To_DB_And_Check_Return_True() {
        orm.register(TestDBWithID.class);

        TestClass savedObject = orm.save(new TestDBWithID("Name 1"));
        var currId = savedObject.getId();

        savedObject.setName("Name 2");
        orm.merge(savedObject);

        var searchResult = orm.findById((Serializable) savedObject.getId(), TestDBWithID.class);
        assertTrue(searchResult.isPresent());
        TestClass foundObject = searchResult.get();

        assertEquals(currId, foundObject.getId());
        assertEquals("Name 2", foundObject.getName());
    }

    @Test
    void Refresh_Object_From_DB_And_Check_Return_True() {
        orm.register(TestDBWithID.class);

        TestClass savedObject = orm.save(new TestDBWithID("Name 1"));
        var currId = savedObject.getId();

        savedObject.setName("Name 2");
        orm.refresh(savedObject);

        var searchResult = orm.findById((Serializable) savedObject.getId(), TestDBWithID.class);
        assertTrue(searchResult.isPresent());
        TestClass foundObject = searchResult.get();

        assertEquals(currId, foundObject.getId());
        assertEquals("Name 1", foundObject.getName());
    }

    @Test
    void testFindById() {
        String title = "My Book";
        Book book = new Book(title, LocalDate.now());
        orm.register(Book.class,Publisher.class,Author.class);
        orm.persist(book);
        book.setTitle("New Book");
        orm.merge(book);

        var idField = AnnotationsUtils.getFieldByAnnotation(book, Id.class);
        assertNotNull(idField);

        var id = SQLUtils.getValueFieldFromObjectToSQLType(book, idField);

        // Find the Book object by id
        Optional<Book> foundBook = orm.findById((Serializable) id, Book.class);

        // Assert that the found Book object is present and has the same title as the original Book object
        assertTrue(foundBook.isPresent());
        assertEquals(book.getTitle(), foundBook.get().getTitle());
    }

    @Test
    void testFindAll() {
        // Create a list of new Book objects with unique ids and titles
        List<Book> books = Arrays.asList(
                new Book("Book 1", LocalDate.now()),
                new Book("Book 2", LocalDate.now()),
                new Book("Book 3", LocalDate.now())
        );
        orm.register(Book.class,Publisher.class,Author.class);

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

    @Test
    void given_book_when_save_then_book_saved_to_database() {
        var bookOne = new Book("LOTR", LocalDate.of(1961, 1, 1));
        orm.register(Book.class,Publisher.class,Author.class);
        orm.save(bookOne);

        assertThat(bookOne.getId()).isGreaterThan(0);
        assertThat(bookOne.getPublishedAt()).isEqualTo(LocalDate.of(1961, 1, 1));
        assertThat(bookOne.getTitle()).isEqualTo("LOTR");
    }

    @Test
    void given_two_books_when_save_then_books_saved_to_database() {
        var bookOne = new Book("Book 1", LocalDate.now());
        var bookTwo = new Book("Book 2", LocalDate.now());

        orm.register(Book.class,Publisher.class,Author.class);
        orm.save(bookOne);
        orm.save(bookTwo);

        assertThat(bookOne.getId()).isNotEqualTo(bookTwo.getId());
    }

    @Test
    void given_the_same_book_twice_when_persisted_then_book_not_saved_custom_exception_thrown() {
        var bookOne = new Book("Book 1", LocalDate.now());
        orm.register(Book.class,Publisher.class,Author.class);
        orm.persist(bookOne);
        Exception exception = assertThrows(
                ObjectAlreadyExistException.class,
                () -> orm.persist(bookOne)
        );

        assertEquals("Try to persist an existing object. Object " + bookOne + " already exist in database!", exception.getMessage());
    }

    @Test
    void given_book_id_when_delete_then_remove_from_database() {
        var bookOne = new Book("Book 1", LocalDate.now());
        orm.register(Book.class,Publisher.class,Author.class);
        orm.persist(bookOne);
        boolean res = orm.delete(bookOne);
        assertTrue(res);
    }

    @Test
    void given_book_id_null_when_delete_then_error() {
        var bookOne = new Book("Book 1", LocalDate.now());

        bookOne.setId(null);

        boolean res = orm.delete(bookOne);

        assertFalse(res);
    }

    @Test
    void given_multiple_books_when_delete_then_remove_from_database() {
        var bookOne = new Book("Book 1", LocalDate.now());
        var bookTwo = new Book("Book 2", LocalDate.now());
        orm.register(Book.class,Publisher.class,Author.class);
        orm.persist(bookOne);
        orm.persist(bookTwo);

        int numberOfDeletions = 2;

        var startCount = orm.getCount(bookOne);
        orm.delete(bookOne);
        orm.delete(bookTwo);
        var endCount = orm.getCount(bookOne);


        assertThat(endCount).isEqualTo(startCount - numberOfDeletions);
    }

}

