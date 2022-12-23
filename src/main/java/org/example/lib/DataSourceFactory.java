package org.example.lib;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;


public class DataSourceFactory {
    public static DataSource getH2DataSource() {

        JdbcDataSource dataSource = new JdbcDataSource();

        dataSource.setURL("jdbc:h2:file:./ormdb");
        dataSource.setUser("");
        dataSource.setPassword("");

        return dataSource;
    }

    /*
    private static DataSource getPostgreDataSource(Properties props) {

        Properties res = new Properties();

        Path path1 = Path.of("postgre.properties");
        try {
            InputStream is = DataSourceFactory.class.getClassLoader()
                    .getResourceAsStream(path1.toString());
            res.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JdbcDataSource dataSource = new JdbcDataSource();

        dataSource.setURL(res.getProperty(""));
        dataSource.setUser(res.getProperty(""));
        dataSource.setPassword(res.getProperty(""));

        return dataSource;
    }

    public static DataSource getDataSource(Properties props, DBTypes type) {
        if (type == DBTypes.H2) return getH2DataSource(props);
        if (type == DBTypes.POSTGRE) return getPostgreDataSource(props);
        //to do
        //Make exception unknown type DB
        return null;
    }
 */
}
