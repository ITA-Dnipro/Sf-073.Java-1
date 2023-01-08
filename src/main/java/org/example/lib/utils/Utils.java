package org.example.lib.utils;

import lombok.extern.slf4j.Slf4j;
import org.example.lib.ORManager;
import org.example.lib.ORManagerImpl;
import org.example.lib.annotations.Id;
import org.example.lib.annotations.ManyToOne;
import org.example.lib.annotations.OneToMany;
import org.example.lib.exceptions.IncorrectPropertiesFileException;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Properties;

@Slf4j
public class Utils {
    private static ORManager orm;

    private Utils() {
    }

    public static ORManager getORMImplementation(String filename) throws IncorrectPropertiesFileException {
        var datasource = getDataSourceFromFilename(filename);
        return getORMImplementation(datasource);
    }

    public static ORManager getORMImplementation(DataSource dataSource) {
        orm = new ORManagerImpl(dataSource);
        return orm;
    }

    public static JdbcDataSource getDataSourceFromFilename(String filename) throws IncorrectPropertiesFileException {
        Properties prop = new Properties();

        try {
            InputStream input = Utils.class.getClassLoader()
                    .getResourceAsStream(filename);
            prop.load(input);
        } catch (IOException e) {
            var message = "Error reading file "+filename+": "+e;
            log.error(message);
            throw new IncorrectPropertiesFileException(message);
        }

        if (prop.isEmpty()){
            var message = "Error reading file "+filename+": property is empty!";
            log.error(message);
            throw new IncorrectPropertiesFileException(message);
        }

        JdbcDataSource datasource = new JdbcDataSource();
        datasource.setURL(prop.getProperty("jdbc-url"));
        datasource.setUser(prop.getProperty("jdbc-username"));
        datasource.setPassword(prop.getProperty("jdbc-password"));

        return datasource;
    }

    public static <T> void copyFieldsOfObject(T o, Object objFrom) {
        var currClass = o.getClass();
        Field[] declaredFields = currClass.getDeclaredFields();
        for (Field field : declaredFields) {
            if (Utils.IsServiceField(field)) continue;
            copyValueOfFieldForObject(o, objFrom, field);
        }
    }

    public static void copyValueOfFieldForObject(Object objTo, Object objFrom, Field idField) {
        var currentValue = getValueOfFieldForObject(objFrom, idField);
        if (currentValue != null) {
            setValueOfFieldForObject(objTo, idField, currentValue);
        }
    }

    public static void setNullToFieldForObject(Object o, Field field) {
        setValueOfFieldForObject(o, field, null);
    }

    public static void setValueOfFieldForObject(Object o, Field field, Object value) {
        field.setAccessible(true);
        var type = field.getType();
            if (AnnotationsUtils.isAnnotationPresent(field, ManyToOne.class)){
                var currRef = orm.findById((Serializable) value,type) ;
                if (currRef.isPresent()){
                    value = currRef.get();
                } else{
                    log.error("An error while setting value for " + field.getName() + " can not find reference for "+type);
                }
            }
            if (AnnotationsUtils.isAnnotationPresent(field, OneToMany.class)){
              return; //to do
            }
       setValueOfFieldForObjectByType(o,field,type,value);
    }

    private static void setValueOfFieldForObjectByType(Object o, Field field, Class<?> type, Object value) {
        var className = o.getClass().getSimpleName();
        try {
            if (type == int.class) {
                field.setInt(o, (int) value);
            } else if (type == long.class) {
                field.setLong(o, (long) value);
            } else if (type == short.class) {
                field.setShort(o, (short) value);
            } else if (type == byte.class) {
                field.setByte(o, (byte) value);
            } else if (type == float.class) {
                field.setFloat(o, (float) value);
            } else if (type == double.class) {
                field.setDouble(o, (double) value);
            } else if (type == boolean.class) {
                field.setBoolean(o, (boolean) value);
            } else if (type == char.class) {
                field.setChar(o, (char) value);
            } else {
                field.set(o, value);
            }
        } catch (IllegalAccessException | IllegalArgumentException e) {
            log.error("An error while setting value for " + field.getName() + " class " + className + "! " + e);
        }
    }

    public static Object getValueOfFieldForObject(Object o, Field field) {
        var className = o.getClass().getSimpleName();
        field.setAccessible(true);
        var type = field.getType();
        try {
            if (type == int.class) {
                return field.getInt(o);
            } else if (type == long.class) {
                return field.getLong(o);
            } else if (type == short.class) {
                return field.getShort(o);
            } else if (type == byte.class) {
                return field.getByte(o);
            } else if (type == float.class) {
                return field.getFloat(o);
            } else if (type == double.class) {
                return field.getDouble(o);
            } else if (type == boolean.class) {
                return field.getBoolean(o);
            } else if (type == char.class) {
                return field.getChar(o);
            }
            return field.get(o);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            log.error("An error while getting value for " + field.getName() + " class " + className + "! " + e);
        }
        return null;
    }

    public static boolean checkIfObjectInDB(Object o) {
        var field = AnnotationsUtils.getFieldByAnnotation(o, Id.class);
        if (field == null) return false;

        var primaryKey = getValueOfFieldForObject(o, field);
        return primaryKey != null;
    }

    public static boolean IsServiceField(Field field) {
        return field.getName().equals("this$0");
    }
}
