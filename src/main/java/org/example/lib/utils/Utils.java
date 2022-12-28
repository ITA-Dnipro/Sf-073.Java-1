package org.example.lib.utils;

import lombok.extern.slf4j.Slf4j;
import org.example.lib.ORManager;
import org.example.lib.ORManagerImpl;
import org.example.lib.annotations.Id;
import org.example.lib.service.Mapper;
import org.example.lib.service.MapperType;
import org.example.model.Book;
import org.example.model.Publisher;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Properties;

@Slf4j
public class Utils {
    private Utils() {
    }

    public static ORManager getORMImplementation(String filename) {
        Properties prop = new Properties();

        try {
            InputStream input = Utils.class.getClassLoader()
                    .getResourceAsStream(filename);
            prop.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties from file: " + filename, e);
        }

        JdbcDataSource datasource = new JdbcDataSource();
        datasource.setURL(prop.getProperty("jdbc-url"));
        datasource.setUser(prop.getProperty("jdbc-username"));
        datasource.setPassword(prop.getProperty("jdbc-password"));

        return ORManager.withDataSource(datasource);
    }

    public static ORManager getORMImplementation(DataSource dataSource) {
        return new ORManagerImpl(dataSource);
    }

    private static String firstUpperCase(String word) {
        if (word == null || word.isEmpty()) return "";//или return word;
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    public static <T> void copyFieldsOfObject(T o, Object objFrom) {
        var currClass = o.getClass();
        Field[] declaredFields = currClass.getDeclaredFields();
        for (Field field : declaredFields) {
            copyValueOfFieldForObject(o, objFrom, field);
        }
    }

    public static void copyValueOfFieldForObject(Object objTo, Object objFrom, Field idField) {
        var currentValue = getValueOfFieldForObject(objFrom, idField);
        if (currentValue != null) {
            setValueOfFieldForObject(objTo, idField, currentValue);
        }
    }

    public static void setValueOfFieldForObject(Object o, Field field, Object value) {
        var methodName = Utils.firstUpperCase(field.getName());
        var className = o.getClass().getName();
        try {
            var methodObject = o.getClass().getDeclaredMethod("set" + Utils.firstUpperCase(field.getName()), value.getClass());
            methodObject.invoke(o, value);
        } catch (NoSuchMethodException e) {
            log.error("Method " + methodName + " for " + className + " is not found! " + e);
        } catch (InvocationTargetException | IllegalAccessException e) {
            log.error("Calling " + methodName + " for " + className + " led to an error:" + e);
        }
    }

    public static Object getValueOfFieldForObject(Object o, Field field) {
        var methodName = Utils.firstUpperCase(field.getName());
        var className = o.getClass().getName();

        try {
            var methodObject = o.getClass().getDeclaredMethod("get" + Utils.firstUpperCase(field.getName()));
            return methodObject.invoke(o);
        } catch (NoSuchMethodException e) {
            log.error("Method " + methodName + " for " + className + " is not found! " + e);
            return null;
        } catch (InvocationTargetException | IllegalAccessException e) {
            log.error("Calling " + methodName + " for " + className + " led to an error:" + e);
            return null;
        }
    }

    public static boolean checkIfObjectInDB(Object o) {
        var field = AnnotationsUtils.getFieldByAnnotation(o, Id.class);
        if (field == null) return false;

        var primaryKey = getValueOfFieldForObject(o, field);
        return primaryKey != null;
    }

    public static <T> Mapper<T> getMapperForClass(Class<?> cls) {
        if (cls == Book.class) {
            return MapperType.BOOK.make();
        }
        if (cls == Publisher.class) {
            return MapperType.PUBLISHER.make();
        }
        log.error("An error while getting mapper for class " + cls.getSimpleName());
        return null;
    }
}
