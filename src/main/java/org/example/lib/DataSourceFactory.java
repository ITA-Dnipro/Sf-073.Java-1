package org.example.lib;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.util.Properties;

public class DataSourceFactory {
        private static DataSource getH2DataSource(Properties props) {
            DataSource dataSource = new JdbcDataSource();
           // dataSource.setURL(props.getProperty("MYSQL_DB_URL"));
           // dataSource.setUser(props.getProperty("MYSQL_DB_USERNAME"));
           // dataSource.setPassword(props.getProperty("MYSQL_DB_PASSWORD"));

            return dataSource;
        }

        private static DataSource getPostgreDataSource(Properties props){
            DataSource dataSource = new JdbcDataSource();
            // dataSource.setURL(props.getProperty("MYSQL_DB_URL"));
            // dataSource.setUser(props.getProperty("MYSQL_DB_USERNAME"));
            // dataSource.setPassword(props.getProperty("MYSQL_DB_PASSWORD"));

            return dataSource;
        }

        public static DataSource getDataSource(Properties props,DBTypes type){
            if (type==DBTypes.H2) return getH2DataSource(props);
            if (type==DBTypes.POSTGRE) return getPostgreDataSource(props);
            //to do
            //Make exception unknown type DB
            return null;
        };

}
