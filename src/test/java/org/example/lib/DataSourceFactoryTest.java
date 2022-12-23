package org.example.lib;

import org.junit.jupiter.api.Test;
import javax.sql.DataSource;
import java.sql.Connection;


import static org.junit.jupiter.api.Assertions.*;

class DataSourceFactoryTest {

    @Test
    void given_Datasource_when_geH2DataSource_then_can_connect() throws Exception {
        DataSource ds = DataSourceFactory.getH2DataSource();

        Connection connection = ds.getConnection();

        assertTrue(connection.isValid(1000));
    }
}