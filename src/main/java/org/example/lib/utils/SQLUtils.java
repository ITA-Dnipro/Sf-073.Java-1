package org.example.lib.utils;

import lombok.extern.slf4j.Slf4j;
import org.example.lib.annotations.Enumerated;
import org.example.lib.annotations.Id;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SQLUtils {
    private SQLUtils() {
    }

    public static String getSQLStringForField(Field field) {
        String nameOfField = AnnotationsUtils.getNameOfColumn(field);
        String typeOfField = SQLUtils.getTypeOfFieldSQL(field);

        if (typeOfField == null) {
            return nameOfField;
        }

        return nameOfField +
                " " + typeOfField;
    }
    
    public static String getSQLStringForIdField(Field field) {
        String typeOfPKEYField = getTypeOfPrimaryKeyFieldSQL(field);
        return field.getName() + " " + typeOfPKEYField + " PRIMARY KEY,";
    }

    private static String getTypeOfFieldSQL(Field field) {
        var type = field.getType();
        String typeSQL;
        if (type == Enum.class) {
            typeSQL = getTypeForEnum(field);
        } else {
            var mapOfTypes = getMapTypesForSQL();
            typeSQL = mapOfTypes.get(type);
        }
        return typeSQL;
    }

    private static String getTypeForEnum(Field field) {
        Enumerated.EnumType currType = null;
        try {
            var method = Enumerated.class.getDeclaredMethod("value");
            currType = (Enumerated.EnumType) method.invoke(null);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException se) {
            log.error("An error while get type of field " + field.getName() + ":" + se.getMessage());
        }

        if (AnnotationsUtils.isAnnotationPresent(field, Enumerated.class)) {
            Enumerated annFieldType = field.getAnnotation(Enumerated.class);
            currType = annFieldType.value();
        }
        if (currType == Enumerated.EnumType.STRING) {
            return "VARCHAR(255)";
        }
        return "INTEGER";
    }

    private static Map<Class<?>, String> getMapTypesForSQL() {
        Map<Class<?>, String> mapOfTypes = new HashMap<>();
        mapOfTypes.put(int.class, "INTEGER");
        mapOfTypes.put(Integer.class, "INTEGER");
        mapOfTypes.put(long.class, "BIGINT");
        mapOfTypes.put(Long.class, "BIGINT");
        mapOfTypes.put(double.class, "DOUBLE");
        mapOfTypes.put(Double.class, "DOUBLE");
        mapOfTypes.put(boolean.class, "BIT");
        mapOfTypes.put(Boolean.class, "BIT");
        mapOfTypes.put(LocalDate.class, "DATE");
        mapOfTypes.put(LocalTime.class, "TIME");
        mapOfTypes.put(LocalDateTime.class, "DATE");
        mapOfTypes.put(Instant.class, "TIMESTAMP");
        mapOfTypes.put(BigDecimal.class, "NUMERIC");
        mapOfTypes.put(String.class, "VARCHAR(255)");

        return mapOfTypes;
    }

    private static String getTypeOfPrimaryKeyFieldSQL(Field field) {
        String typeSQL = getTypeOfFieldSQL(field);

        if (typeSQL == null) {
            return "";
        }

        if (typeSQL.equals("INTEGER")) {
            var currType = AnnotationsUtils.getIdType(field);
            if (currType == Id.IDType.SERIAL) {
                typeSQL = "SERIAL";
            } else typeSQL = "BINARY(16)";
        }
        return typeSQL;
    }

 
}
