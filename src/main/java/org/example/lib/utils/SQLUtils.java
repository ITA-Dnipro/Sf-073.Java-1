package org.example.lib.utils;

import lombok.extern.slf4j.Slf4j;
import org.example.lib.annotations.Enumerated;
import org.example.lib.annotations.Id;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
public class SQLUtils {

    private SQLUtils() {
    }

    public static int getSQLType(Class<?> type) {
        return getJDBCTypeNumber(type);
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
        return field.getName() + " " + typeOfPKEYField + " PRIMARY KEY";
    }

    private static String getTypeOfFieldSQL(Field field) {
        var type = field.getType();
        String typeSQL;
        if (type == Enum.class) {
            typeSQL = getTypeForEnum(field);
        } else {
            typeSQL = getJDBCType(type);
        }
        return typeSQL;
    }

    private static String getJDBCType(Class<?> type) {
        int intTypeSQL = getJDBCTypeNumber(type);
        if (intTypeSQL == 0) {
            return "";
        }
        return getNameJdbcTypeById(intTypeSQL);
    }

    private static Integer getJDBCTypeNumber(Class<?> type) {
        var mapOfTypes = getMapTypesForSQL();
        var currNumberOfType = mapOfTypes.get(type);
        return currNumberOfType == null ? 0 : currNumberOfType;
    }

    public static String getTypeOfIDField(Object o) {
        var field = AnnotationsUtils.getFieldByAnnotation(o, Id.class);
        if (field == null) return "";

        String typeOfPKEYField = getTypeOfPrimaryKeyFieldSQL(field);
        return typeOfPKEYField.toUpperCase(Locale.ROOT);
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
            return getNameJdbcTypeById(Types.NVARCHAR);
        }
        return getNameJdbcTypeById(Types.INTEGER);
    }

    private static Map<Class<?>, Integer> getMapTypesForSQL() {
        Map<Class<?>, Integer> mapOfTypes = new HashMap<>();

        mapOfTypes.put(int.class, Types.INTEGER);
        mapOfTypes.put(Integer.class, Types.INTEGER);
        mapOfTypes.put(long.class, Types.BIGINT);
        mapOfTypes.put(Long.class, Types.BIGINT);
        mapOfTypes.put(double.class, Types.DOUBLE);
        mapOfTypes.put(Double.class, Types.DOUBLE);
        mapOfTypes.put(boolean.class, Types.BIT);
        mapOfTypes.put(Boolean.class, Types.BIT);
        mapOfTypes.put(LocalDate.class, Types.DATE);
        mapOfTypes.put(LocalTime.class, Types.TIME);
        mapOfTypes.put(LocalDateTime.class, Types.DATE);
        mapOfTypes.put(Instant.class, Types.TIMESTAMP);
        mapOfTypes.put(BigDecimal.class, Types.NUMERIC);
        mapOfTypes.put(String.class, Types.NVARCHAR);

        //add types for sql.date and sql.time
        mapOfTypes.put(Date.class, Types.DATE);
        mapOfTypes.put(Time.class, Types.TIME);
        mapOfTypes.put(Timestamp.class, Types.TIMESTAMP);

        return mapOfTypes;
    }

    private static Map<Integer, String> getAllJdbcTypeNames() {

        Map<Integer, String> result = new HashMap<>();

        for (Field field : Types.class.getFields()) {
            try {
                result.put((Integer) field.get(null), field.getName());
            } catch (IllegalAccessException e) {
                log.error("An error while get type from JDBC types " + field.getName() + ":" + e.getMessage());
            }
        }

        return result;
    }

    private static String getNameJdbcTypeById(int id) {

        var mapOfTypes = getAllJdbcTypeNames();
        var currType = mapOfTypes.get(id);

        return currType == null ? "" : currType;
    }

    public static boolean idFieldIsAutoIncrementOnDBSide(Field field) {
        var type = field.getType();
        Integer intTypeSQL = getJDBCTypeNumber(type);
        String typeSQL = getNameJdbcTypeById(intTypeSQL);
        if (intTypeSQL.equals(Types.INTEGER)
                || intTypeSQL.equals(Types.BIGINT)) {
            var currType = AnnotationsUtils.getIdType(field);
            return currType == Id.IDType.SERIAL;
        }
        return false;
    }

    private static String getTypeOfPrimaryKeyFieldSQL(Field field) {
        var type = field.getType();
        Integer intTypeSQL = getJDBCTypeNumber(type);
        String typeSQL = getNameJdbcTypeById(intTypeSQL);
        if (intTypeSQL.equals(Types.INTEGER)
                || intTypeSQL.equals(Types.BIGINT)) {
            var currType = AnnotationsUtils.getIdType(field);
            if (currType == Id.IDType.SERIAL) {
                typeSQL = typeSQL + " AUTO_INCREMENT";
            } else typeSQL = getNameJdbcTypeById(Types.BINARY);
        }
        return typeSQL;
    }

    public static Object getValueFieldFromJavaToSQLType(Object o, Field field) {
        var currData = Utils.getValueOfFieldForObject(o, field); //to do
        var type = field.getType();
        if (currData == null) return null;

        if (type == LocalDate.class) {
            return Date.valueOf((LocalDate) currData);
        } else if (type == Instant.class) {
            return Timestamp.from((Instant) currData);
        } else if (type == LocalDateTime.class) {
            return convertLocalDateTimeToSQLDate((LocalDateTime) currData);
        } else if (type == LocalTime.class) {
            return Time.valueOf((LocalTime) currData);
        }
        return currData;
    }

    public static Object getValueFieldFromSQLToJavaType(Object currData, Field field) {
       return null;
    }

    private static java.sql.Date convertLocalDateTimeToSQLDate(LocalDateTime dateValue) {
        // convert from LocalDateTime to java.sql.date while retaining
        // the time part without having to make assumptions about the time-zone
        // by using java.util.Date as an intermediary
        java.util.Date utilDate;
        String dateFormat = "yyyy-MM-dd'T'HH:mm:ss";
        DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern(dateFormat);
        SimpleDateFormat sdf1 = new SimpleDateFormat(dateFormat);
        try {
            utilDate = sdf1.parse(dateValue.format(dtf1));
        } catch (ParseException e) {
            return null;
        }
        return new java.sql.Date(utilDate.getTime());
    }

    public static boolean objectHasAutoIncrementID(Object o) {
        var field = AnnotationsUtils.getFieldByAnnotation(o, Id.class);
        if (field == null) return false;

        return idFieldIsAutoIncrementOnDBSide(field);
    }

    public static Object generateIdForObject(Object o) {
        return null; //to do
    }

    public static Object getValueForFieldFromResultSet(ResultSet resultSet, Field field) {
        var columnName = AnnotationsUtils.getNameOfColumn(field);
        var type = field.getType();
        try {
            if (type == int.class) {
                return resultSet.getInt(columnName);
            } else if (type == long.class) {
                return resultSet.getLong(columnName);
            } else if (type == short.class) {
                return resultSet.getShort(columnName);
            } else if (type == byte.class) {
                return resultSet.getByte(columnName);
            } else if (type == float.class) {
                return resultSet.getFloat(columnName);
            } else if (type == double.class) {
                return resultSet.getDouble(columnName);
            } else if (type == boolean.class) {
                return resultSet.getBoolean(columnName);
            } else if (type == LocalDate.class || type == LocalDateTime.class) {
                return getValueFieldFromSQLToJavaType(resultSet.getDate(columnName),field);
            } else if (type == Instant.class) {
                return getValueFieldFromSQLToJavaType(resultSet.getTimestamp(columnName),field);
            } else if (type == LocalTime.class) {
                return getValueFieldFromSQLToJavaType(resultSet.getTime(columnName),field);
            }
            return resultSet.getObject(columnName);
        } catch (SQLException e) {
            log.error("An error while getting value for " + field.getName() + "! " + e);
        }
        return null;
    }
}
