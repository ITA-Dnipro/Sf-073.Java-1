package org.example.lib.utils;

import lombok.extern.slf4j.Slf4j;
import org.example.lib.annotations.Entity;
import org.example.lib.annotations.Enumerated;
import org.example.lib.annotations.Id;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

        if (typeOfField.isEmpty()) {
            log.error("Cannot get SQL string for field "+field.getName());
            return "";
        }

        return nameOfField +
                " " + typeOfField;
    }

    public static String getSQLStringForIdField(Field field) {
        String typeOfPKEYField;
        try {
            typeOfPKEYField = getTypeOfPrimaryKeyFieldSQL(field);
        } catch (UnsupportedOperationException e) {
            log.error(e.getMessage());
            return "";
        }
        return AnnotationsUtils.getNameOfColumn(field) + " " + typeOfPKEYField + " PRIMARY KEY";
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
            log.error("Cannot get type SQL for type "+type.getSimpleName());
            return "";
        }
        return getNameJdbcTypeById(intTypeSQL);
    }

    private static Integer getJDBCTypeNumber(Class<?> type) {
        var mapOfTypes = getMapTypesForSQL();
        var currNumberOfType = mapOfTypes.get(type);
        return currNumberOfType == null ? 0 : currNumberOfType;
    }

    private static String getTypeForEnum(Field field) {
        Enumerated.EnumType currType;
        try {
            var method = Enumerated.class.getDeclaredMethod("value");
            currType = (Enumerated.EnumType) method.invoke(null);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException se) {
            log.error("An error while get type of field " + field.getName() + ":" + se);
            return "";
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
        mapOfTypes.put(LocalDateTime.class, Types.TIMESTAMP);
        mapOfTypes.put(Instant.class, Types.TIMESTAMP);
        mapOfTypes.put(BigDecimal.class, Types.VARCHAR);
        mapOfTypes.put(String.class, Types.NVARCHAR);
        mapOfTypes.put(UUID.class, Types.VARCHAR);
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
                log.error("An error while get type from JDBC types " + field.getName() + ":" + e);
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
        if (intTypeSQL.equals(Types.INTEGER)
                || intTypeSQL.equals(Types.BIGINT)) {
            var currType = AnnotationsUtils.getIdType(field);
            return currType == Id.IDType.SERIAL;
        }
        return false;
    }

    private static String getTypeOfPrimaryKeyFieldSQL(Field field) throws UnsupportedOperationException {
        var type = field.getType();
        Integer intTypeSQL = getJDBCTypeNumber(type);
        String typeSQL = getNameJdbcTypeById(intTypeSQL);
        if (intTypeSQL.equals(Types.INTEGER)
                || intTypeSQL.equals(Types.BIGINT)) {
            var currType = AnnotationsUtils.getIdType(field);
            if (currType == Id.IDType.SERIAL) {
                typeSQL = typeSQL + " AUTO_INCREMENT";
            } else typeSQL = getNameJdbcTypeById(Types.VARCHAR);
        } else if (type != UUID.class) {
            throw new UnsupportedOperationException("For ID type "+type.getSimpleName()+" is not supported");
        }
        return typeSQL;
    }

    public static Object getValueFieldFromObjectToSQLType(Object o, Field field) {
        var currData = Utils.getValueOfFieldForObject(o, field);
        var type = field.getType();
        if (currData == null) return null;

        return getValueFromJavaToSQLType(currData,type);
    }

    public static Object getValueFromJavaToSQLType(Object currData, Class<?> type) {
        if (type == BigDecimal.class) {
            return currData.toString();
        } else if (type == UUID.class) {
            return convertUUIDToString((UUID) currData); // to do - use BINARY not string
        } else if (type == LocalDate.class) {
            return Date.valueOf((LocalDate) currData);
        } else if (type == Instant.class) {
            return Timestamp.from((Instant) currData);
        } else if (type == LocalDateTime.class) {
            return Timestamp.valueOf((LocalDateTime) currData);
        } else if (type == LocalTime.class) {
            return Time.valueOf((LocalTime) currData);
        }
        return currData;
    }

    private static String convertUUIDToString(UUID currUUID){
        return currUUID.toString().replaceAll("-","");
    }

    private static UUID convertStringToUUID(String currString){
        StringBuilder sb = new StringBuilder(currString);
        sb.insert(23,"-");
        sb.insert(18,"-");
        sb.insert(13,"-");
        sb.insert(8,"-");
        return UUID.fromString(sb.toString());
    }

    private static Object getValueFieldFromSQLToJavaType(Object currData, Field field) {
        var type = field.getType();
        if (currData == null) return null;

        if (type == BigDecimal.class) {
            return new BigDecimal((String) currData);
        } else if (type == UUID.class) {
            return convertStringToUUID((String) currData);
        } else if (type == LocalDate.class) {
            return ((Date) currData).toLocalDate();
        } else if (type == Instant.class) {
            return ((Timestamp) currData).toInstant();
        } else if (type == LocalDateTime.class) {
            return ((Timestamp) currData).toLocalDateTime();
        } else if (type == LocalTime.class) {
            return ((Time) currData).toLocalTime();
        }
       return currData;
    }

    public static boolean objectHasAutoIncrementID(Object o) {
        var field = AnnotationsUtils.getFieldByAnnotation(o, Id.class);
        if (field == null) return false;

        return idFieldIsAutoIncrementOnDBSide(field);
    }

    public static Object getValueForFieldFromResultSet(ResultSet resultSet, Field field) {
        var columnName = AnnotationsUtils.getNameOfColumn(field);
        var type = field.getType();
        try {
            if (type == int.class || type == Integer.class) {
                return resultSet.getInt(columnName);
            } else if (type == long.class || type == Long.class) {
                return resultSet.getLong(columnName);
            } else if (type == String.class) {
                return resultSet.getString(columnName);
            } else if (type == UUID.class) {
                return getValueFieldFromSQLToJavaType(resultSet.getString(columnName),field);
            } else if (type == double.class || type == Double.class) {
                return resultSet.getDouble(columnName);
            } else if (type == boolean.class || type == Boolean.class) {
                return resultSet.getBoolean(columnName);
            } else if (type == LocalDate.class || type == LocalDateTime.class) {
                return getValueFieldFromSQLToJavaType(resultSet.getDate(columnName),field);
            } else if (type == Instant.class) {
                return getValueFieldFromSQLToJavaType(resultSet.getTimestamp(columnName),field);
            } else if (type == LocalTime.class) {
                return getValueFieldFromSQLToJavaType(resultSet.getTime(columnName),field);
            } else if (type == BigDecimal.class) {
                return getValueFieldFromSQLToJavaType(resultSet.getString(columnName),field);
            }
            return resultSet.getObject(columnName);
        } catch (SQLException e) {
            log.error("An error while getting value for " + field.getName() + "! " + e);
        }
        return null;
    }

    public static String getSQLStringForFieldManyToOne(Field field) {
        var type = field.getType();
        var fieldId = AnnotationsUtils.getFieldByAnnotation(type,Id.class);
        if (fieldId == null){
            log.error("Cannot get SQL query with foreign key for field "+field.getName()+": class "+type.getSimpleName()+" has no id field");
            return "";
        }

        String nameOfField = AnnotationsUtils.getNameOfColumn(field);
        String typeOfField = SQLUtils.getTypeOfFieldSQL(fieldId);

        if (typeOfField.isEmpty()) {
            log.error("Cannot get type SQL for field "+nameOfField);
            return "";
        }

        return nameOfField +
                " " + typeOfField;
    }

    public static String getSQLStringForForeignKey(Field field) {
        var type = field.getType();
        var fieldId = AnnotationsUtils.getFieldByAnnotation(type,Id.class);
        if (fieldId == null){
            log.error("Cannot get SQL query with foreign key for field "+field.getName()+": class "+type.getSimpleName()+" has no id field");
            return "";
        }

        if (!AnnotationsUtils.isAnnotationPresent(type, Entity.class)){
            log.error("Cannot get SQL query with foreign key for class "+type.getSimpleName()+": class has no entity annotation");
            return "";
        }

        String nameOfField = AnnotationsUtils.getNameOfColumn(field);
        String nameOfFieldId = AnnotationsUtils.getNameOfColumn(fieldId);
        String nameOfTable = AnnotationsUtils.getNameOfTable(type);

        return "FOREIGN KEY ("+nameOfField+") references "+nameOfTable+"("+nameOfFieldId+")";
    }
}
