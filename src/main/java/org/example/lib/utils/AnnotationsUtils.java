package org.example.lib.utils;

import org.example.lib.annotations.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class AnnotationsUtils {
    private AnnotationsUtils() {
    }

    public static boolean isAnnotationPresent(Field field,Class<? extends Annotation> AnnotationClass){
        return field.isAnnotationPresent(AnnotationClass);
    }

    public static boolean isAnnotationPresent(Class<?> cls, Class<? extends Annotation> AnnotationClass){
        return cls.isAnnotationPresent(AnnotationClass);
    }

    public static String getNameOfColumn(Field field){
        String nameOfColumn = field.getName();
        if (isAnnotationPresent(field, Column.class)) {
            var annFieldType = field.getAnnotation(Column.class);
            return annFieldType.value().isEmpty() ? nameOfColumn : annFieldType.value();
        }
        if (isAnnotationPresent(field, ManyToOne.class)) {
            var annFieldType = field.getAnnotation(ManyToOne.class);
            return annFieldType.columnName().isEmpty() ? nameOfColumn : annFieldType.columnName();
        }
        return  nameOfColumn;
    }

    public static Id.IDType getIdType(Field field){
        Id fld = field.getAnnotation(Id.class);
        return fld.value();
    }

    public static String getNameOfTable(Class<?> cls){
        String nameOfTable = cls.getSimpleName();
        if (isAnnotationPresent(cls, Table.class)) {
            var annFieldType = cls.getAnnotation(Table.class);
            return annFieldType.value().isEmpty() ? nameOfTable : annFieldType.value();
        }
        return nameOfTable;
    }

    public static Enumerated.EnumType getEnumType(Field field){
        Enumerated fld = field.getAnnotation(Enumerated.class);
        return fld.value();
    }

    public static Field getFieldByAnnotation(Object o, Class<? extends Annotation> AnnotationClass) {
        return getFieldByAnnotation(o.getClass(), AnnotationClass);
    }

    public static <T> Field getFieldByAnnotation(Class<T> o, Class<? extends Annotation> AnnotationClass) {
        Field[] declaredFields = o.getDeclaredFields();
        for (Field field : declaredFields) {
            if (Utils.IsServiceField(field)) continue;
            if (AnnotationsUtils.isAnnotationPresent(field,AnnotationClass)) {
                return field;
            }
        }
        return null;
    }
}
