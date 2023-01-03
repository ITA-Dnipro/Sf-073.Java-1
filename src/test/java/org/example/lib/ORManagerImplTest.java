package org.example.lib;


import org.example.lib.utils.Utils;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;


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
}