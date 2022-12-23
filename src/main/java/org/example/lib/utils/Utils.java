package org.example.lib.utils;
import org.example.lib.ORManager;
import org.example.lib.ORManagerImpl;

import javax.sql.DataSource;

public class Utils {
    private Utils() {
    }

    public static ORManager getORMImplementation(DataSource dataSource){
           return new ORManagerImpl(dataSource);
    }

}
