package org.example.lib.utils;

import lombok.Getter;
import lombok.Value;
import org.example.lib.ORManager;
import org.example.lib.ORManagerImpl;
import org.example.lib.annotations.Column;
import org.example.lib.annotations.Entity;
import org.example.lib.annotations.Id;

import javax.sql.DataSource;
import java.lang.reflect.Field;

public class Utils {
    private Utils() {
    }

    public static ORManager getORMImplementation(DataSource dataSource) {
        return new ORManagerImpl(dataSource);
    }

    public static boolean entityAnnotationIsPresent(Class<?> cls) {
        return cls.isAnnotationPresent(Entity.class);
    }

    public static FieldInfo getIdField(Class<?> cls) {
        FieldInfo result = null;
        for (var field : cls.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                result = new FieldInfo(field);
                break;
            }

        }
        return result;

    }
    public static String createTableDdl(Class<?> cls) {
        String sql = String.format("create table if not exists %s ( %n", cls.getSimpleName());
        if (cls.isAnnotationPresent(Entity.class)) {
            Field[] declaredFields = cls.getDeclaredFields();
            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(Id.class)) {
                    sql += String.format("  id bigint not null,%n  primary key (id)%n);");
                }
            }
        }
        return sql;
    }

}


class FieldInfo {
    String columnname;
    public FieldInfo(Field field) {
        columnname = extractColumnName(field);

    }
    public String getColumnName() {
        return columnname;
    }
    private String extractColumnName(Field field) {
        Column columnAnnotation = field.getAnnotation(Column.class);
        if (columnAnnotation != null) {
            String name = columnAnnotation.value();
            if (!name.equals("")) {
                return name;
            }
        }
        return field.getName();
    }
}

