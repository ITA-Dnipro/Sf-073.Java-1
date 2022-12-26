package org.example.lib.utils;

import org.example.lib.annotations.Column;
import org.example.lib.annotations.Enumerated;
import org.example.lib.annotations.Id;
import org.example.lib.annotations.Table;
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
        var annFieldType = field.getAnnotation(Column.class);
        return annFieldType.value().isEmpty() ? nameOfColumn : annFieldType.value();
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

    public static Field getFieldByAnnotation(Object o,Class<? extends Annotation> AnnotationClass) {
        var currClass = o.getClass();
        Field[] declaredFields = currClass.getDeclaredFields();
        for (Field field : declaredFields) {
            if (AnnotationsUtils.isAnnotationPresent(field,AnnotationClass)) {
                return field;
            }
        }
        return null;
    }
}
