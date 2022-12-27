package org.example.lib;

import org.example.lib.ORManager;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class ORManagerImplTest {

    @Test
    void getConnection() throws SQLException {
//TO DO
        // load properties from file
//        ORManager orm = ORManager.withPropertiesFrom("src/main/java/resources/db.properties");
//
//        // create a connection to the database
//        Connection conn = orm.getConnection();
//
//        // make sure the connection is valid
//        assertTrue(conn.isValid(10));
//
//        // close the connection
//        conn.close();

    }

    @Test
    void given_Datasource_when_geH2DataSource_then_can_connect() throws Exception {

        JdbcDataSource dataSource = new JdbcDataSource();

        dataSource.setURL("jdbc:h2:file:./ormdb");
        dataSource.setUser("");
        dataSource.setPassword("");

        ORManager orm = ORManager.withDataSource(dataSource);


        //Connection connection = ds.getConnection();

        //assertTrue(connection.isValid(1000));
    }

    @Test
    void updateConnection() {

    }
}