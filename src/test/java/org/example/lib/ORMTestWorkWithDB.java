package org.example.lib;

import org.example.lib.annotations.Id;
import org.example.lib.exceptions.ObjectAlreadyExistException;
import org.example.lib.utils.AnnotationsUtils;
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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ORMTestWorkWithDB {
    private ORManager orm;
    private String propertiesFileName;

    private void clearDatabase() throws SQLException {

        DataSource datasource = Utils.getDataSourceFromFilename(propertiesFileName);

        Connection c = datasource.getConnection();
        Statement s = c.createStatement();

        // Disable FK
        s.execute("SET REFERENTIAL_INTEGRITY FALSE");

        // Find all tables and truncate them
        Set<String> tables = new HashSet<String>();
        ResultSet rs = s.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES  where TABLE_SCHEMA='PUBLIC'");
        while (rs.next()) {
            tables.add(rs.getString(1));
        }
        rs.close();
        for (String table : tables) {
            s.executeUpdate("TRUNCATE TABLE " + table);
        }

        // Idem for sequences
        Set<String> sequences = new HashSet<String>();
        rs = s.executeQuery("SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_SCHEMA='PUBLIC'");
        while (rs.next()) {
            sequences.add(rs.getString(1));
        }
        rs.close();
        for (String seq : sequences) {
            s.executeUpdate("ALTER SEQUENCE " + seq + " RESTART WITH 1");
        }

        // Enable FK
        s.execute("SET REFERENTIAL_INTEGRITY TRUE");
        s.close();
        c.close();
    }

    @BeforeEach
    void setUp() {
        this.propertiesFileName = "db_test.properties";
        // load properties from file
        this.orm = ORManager.withPropertiesFrom(propertiesFileName);
    }

    @AfterEach
    void deleteDatabase() throws SQLException {
        clearDatabase();
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

        Optional<TestClass> foundObject = orm.findById((Serializable) savedObject.getId(), TestClass.class);
        assertTrue(foundObject.isPresent());
        assertEquals(foundObject.get(), savedObject);
    }

    @Test
    void can_Not_Find_Object_By_Id() {
        orm.register(TestDBWithID.class);

        Optional<TestClass> objectToBeFound = orm.findById(-1L, TestClass.class);
        assertFalse(objectToBeFound.isPresent());
    }

    @Test
    void Merge_Object_To_DB_And_Check_Return_True() {
        orm.register(TestDBWithID.class);

        TestClass savedObject = orm.save(new TestDBWithID("Name 1"));
        var currId = savedObject.getId();

        savedObject.setName("Name 2");
        orm.merge(savedObject);

        var searchResult = orm.findById((Serializable) savedObject.getId(), TestClass.class);
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

        var searchResult = orm.findById((Serializable) savedObject.getId(), TestClass.class);
        assertTrue(searchResult.isPresent());
        TestClass foundObject = searchResult.get();

        assertEquals(currId, foundObject.getId());
        assertEquals("Name 1", foundObject.getName());
    }
}

