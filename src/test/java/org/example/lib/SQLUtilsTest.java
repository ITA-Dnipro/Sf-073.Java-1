package org.example.lib;


import org.example.lib.utils.SQLUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SQLUtilsTest {
    Field plainLong;
    Field plainString;
    Field plainInt;
    Field plainBoolean;
    Field annotatedLong;
    Field uuidLong;
    Field titleLong;
    TestClassSqlUtils nullTest;
    TestClassSqlUtils test;
    Connection connection;
    Class<?> clss;


    @BeforeEach
    void setUp() throws Exception {
        clss = Class.forName("org.example.lib.TestClassSqlUtils");
        nullTest = new TestClassSqlUtils();
        test = new TestClassSqlUtils(1L);
        plainLong = clss.getDeclaredField("tableLong");
        plainString = clss.getDeclaredField("tableString");
        plainInt = clss.getDeclaredField("tableInt");
        plainBoolean = clss.getDeclaredField("tableBoolean");
        annotatedLong = clss.getDeclaredField("table");
        uuidLong = clss.getDeclaredField("tableUUID");
        titleLong = clss.getDeclaredField("title");
        plainLong.setAccessible(true);
        connection = DriverManager.getConnection("jdbc:h2:./ORMTest", "sa", "password");
        connection.setAutoCommit(false);
    }

    @Test
    void given_field_long_when_getSQLStringForField_then_nameOfField_plus_SqlType() {
        String res = SQLUtils.getSQLStringForField(plainLong);

        String expected = "tableLong BIGINT";

        assertThat(res).isEqualTo(expected);
    }

    @Test
    void given_field_string_when_getSQLStringForField_then_nameOfField_plus_SqlType() {
        String res = SQLUtils.getSQLStringForField(plainString);

        String expected = "tableString NVARCHAR";

        assertThat(res).isEqualTo(expected);
    }

    @Test
    void given_field_int_when_getSQLStringForField_then_nameOfField_plus_SqlType() {
        String res = SQLUtils.getSQLStringForField(plainInt);

        String expected = "tableInt INTEGER";

        assertThat(res).isEqualTo(expected);
    }

    @Test
    void given_field_boolean_when_getSQLStringForField_then_nameOfField_plus_SqlType() {
        String res = SQLUtils.getSQLStringForField(plainBoolean);

        String expected = "tableBoolean BIT";

        assertThat(res).isEqualTo(expected);
    }

    @Test
    void given_annotated_with_id_field_when_getSQLStringForField_then_nameOfField_plus_SqlType() {
        String res = SQLUtils.getSQLStringForField(plainLong);

        String expected = "tableLong BIGINT";

        assertThat(res).isEqualTo(expected);
    }

    @Test
    void given_annotated_with_id_field_when_getSQLStringForIdField_then_SqlString_for_ID() {
        String res = SQLUtils.getSQLStringForIdField(annotatedLong);

        String expected = "table BIGINT AUTO_INCREMENT PRIMARY KEY";

        assertThat(res).isEqualTo(expected);
    }

    @Test
    void given_long_field_when_getSQLStringForIdField_then_nullPointerException() {
        assertThrows(
                NullPointerException.class,
                () -> SQLUtils.getSQLStringForIdField(plainLong)
        );
    }

    @Test
    void given_annotated_long_field_when_idFieldIsAutoIncrementOnDBSide_then_true() {
        boolean res = SQLUtils.idFieldIsAutoIncrementOnDBSide(annotatedLong);

        assertTrue(res);
    }

    @Test
    void given_annotated_long_field_when_idFieldIsAutoIncrementOnDBSide_then_false() {
        boolean res = SQLUtils.idFieldIsAutoIncrementOnDBSide(uuidLong);

        assertFalse(res);
    }

    @Test
    void given_object_and_fields_when_getValueFieldFromObjectToSQLType_then_return_null() {
        var res = SQLUtils.getValueFieldFromObjectToSQLType(nullTest, annotatedLong);

        assertNull(res);
    }

    @Test
    void given_object_with_value_fields_when_getValueFieldFromObjectToSQLType_then_value() {
        var res = SQLUtils.getValueFieldFromObjectToSQLType(test, annotatedLong);

        assertThat(res).isEqualTo(1L);
    }

    @Test
    void given_object_with_value_big_decimal_when_getValueFromJavaToSQLType_then_return_Sql_type() {
        BigDecimal decimal = new BigDecimal("55.50");
        TestClassSqlUtils bigDec = new TestClassSqlUtils(decimal);
        var res = SQLUtils.getValueFromJavaToSQLType(bigDec, BigDecimal.class);

        assertThat(res).isEqualTo(bigDec.toString());
    }

    @Test
    void given_object_with_value_local_date_when_getValueFromJavaToSQLType_then_return_Sql_type() {
        LocalDate localDate = LocalDate.now();

        var res = SQLUtils.getValueFromJavaToSQLType(localDate, LocalDate.class);
        var date = Date.valueOf(localDate);

        assertThat(res).isEqualTo(date);
    }

    @Test
    void given_object_with_value_uuid_when_getValueFromJavaToSQLType_then_return_Sql_type() {
        var uuid = UUID.randomUUID();

        var res = SQLUtils.getValueFromJavaToSQLType(uuid, UUID.class);
        String uuidStr = uuid.toString().replace("-", "");

        assertThat(res).isEqualTo(uuidStr);
    }

    @Test
    void given_object_with_value_instant_time_when_getValueFromJavaToSQLType_then_return_Sql_type() {
        var timeInstant = Instant.now();

        var res = SQLUtils.getValueFromJavaToSQLType(timeInstant, Instant.class);
        var timestamp = Timestamp.from(timeInstant);

        assertThat(res).isEqualTo(timestamp);
    }

    @Test
    void given_object_with_value_local_date_time_when_getValueFromJavaToSQLType_then_return_Sql_type() {
        var localDateTime = LocalDateTime.now();

        var res = SQLUtils.getValueFromJavaToSQLType(localDateTime, LocalDateTime.class);
        var localDateTimeSql = Timestamp.valueOf(localDateTime);


        assertThat(res).isEqualTo(localDateTimeSql);
    }

    @Test
    void given_object_with_value_local_time_when_getValueFromJavaToSQLType_then_return_Sql_type() {
        var localTime = LocalTime.MIDNIGHT;

        var res = SQLUtils.getValueFromJavaToSQLType(localTime, LocalTime.class);
        var localTimeSql = Time.valueOf(localTime);

        assertThat(res).isEqualTo(localTimeSql);
    }

}