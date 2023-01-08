package org.example.lib;


import org.example.lib.exceptions.IncorrectPropertiesFileException;
import org.example.lib.utils.Utils;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestDatasourceAndConnection {
    private String propertiesFileName;

    @BeforeEach
    void setUp() {
        this.propertiesFileName = "db_test.properties";
      }


    @Test
    void given_fileName_ORMangerWithPropertiesFrom_then_connect() {

        // load properties from file
        ORManager orm = Utils.getORMImplementation(propertiesFileName);

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
        String fileName = "db_empty.properties";
        var status = false;
        try {
            Utils.getORMImplementation(fileName);
        } catch(IncorrectPropertiesFileException e){
            status = true;
        }

        assertTrue(status);
    }


    @Test
    void given_empty_fileProperties_ORMangerWithPropertiesFrom_then_Exception() {
        String fileName = "";

        ORManager orm = Utils.getORMImplementation(fileName);

        assertThrows(NullPointerException.class,
                orm::checkConnectionToDB);
    }

    @Test
    void given_empty_datasource_when_getORMImplementation_then_connection_invalid() {

        JdbcDataSource dataSource = new JdbcDataSource();

        ORManager res = Utils.getORMImplementation(dataSource);

        assertFalse(res.checkConnectionToDB());
    }
}