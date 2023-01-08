package org.example.lib.utils;

import org.example.lib.ORManager;
import org.example.model.TestDBWithID;
import org.example.model.TestDBWithoutID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.SQLException;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    Class<?> clss;
    Field tableLong;
    Field tableString;
    Field tableInt;
    Field tableBoolean;
    Field tableByte;
    Field tableChar;
    Field tableShort;
    Field tableDouble;
    Field tableFloat;
    Field wrapperLong;

    private ORManager orm;
    private String propertiesFileName;

    private TestClassUtils testClass;


    @BeforeEach
    void setUp() throws Exception {
        clss = Class.forName("org.example.lib.utils.TestClassUtils");
        tableLong = clss.getDeclaredField("tableLong");
        tableString = clss.getDeclaredField("tableString");
        tableInt = clss.getDeclaredField("tableInt");
        tableBoolean = clss.getDeclaredField("tableBoolean");
        tableByte = clss.getDeclaredField("tableByte");
        tableDouble = clss.getDeclaredField("tableDouble");
        tableChar = clss.getDeclaredField("tableChar");
        tableShort = clss.getDeclaredField("tableShort");
        tableFloat = clss.getDeclaredField("tableFloat");
        wrapperLong = clss.getDeclaredField("wraperLong");
        testClass = new TestClassUtils();

        this.propertiesFileName = "db_test.properties";
        // load properties from file
        this.orm = ORManager.withPropertiesFrom(propertiesFileName);
    }

    @AfterEach
    void tearDown() throws SQLException {
        DbUtils.clearDatabase(propertiesFileName);
    }


    @Test
    void given_object_long_value_when_setValueOfFieldForObject_then_set_value() {
        Object value = 5L;
        Utils.setValueOfFieldForObject(testClass, tableLong, value);

        var res = Utils.getValueOfFieldForObject(testClass, tableLong);

        assertThat(res).isEqualTo(5L);
    }

    @Test
    void given_object_int_value_when_setValueOfFieldForObject_then_set_value() {
        Object value = 5;
        Utils.setValueOfFieldForObject(testClass, tableInt, value);

        var res = Utils.getValueOfFieldForObject(testClass, tableInt);

        assertThat(res).isEqualTo(5);
    }

    @Test
    void given_object_float_value_when_setValueOfFieldForObject_then_set_value() {
        Object value = 5.0f;
        Utils.setValueOfFieldForObject(testClass, tableFloat, value);

        var res = Utils.getValueOfFieldForObject(testClass, tableFloat);

        assertThat(res).isEqualTo(5.0f);
    }

    @Test
    void given_object_boolean_value_when_setValueOfFieldForObject_then_set_value() {
        Object value = true;
        Utils.setValueOfFieldForObject(testClass, tableBoolean, value);

        var res = Utils.getValueOfFieldForObject(testClass, tableBoolean);

        assertThat(res).isEqualTo(true);
    }

    @Test
    void given_object_string_value_when_setValueOfFieldForObject_then_set_value() {
        Object value = "String";
        Utils.setValueOfFieldForObject(testClass, tableString, value);

        var res = Utils.getValueOfFieldForObject(testClass, tableString);

        assertThat(res).isEqualTo("String");
    }

    @Test
    void given_object_char_value_when_setValueOfFieldForObject_then_set_value() {
        Object value = 'c';
        Utils.setValueOfFieldForObject(testClass, tableChar, value);

        var res = Utils.getValueOfFieldForObject(testClass, tableChar);

        assertThat(res).isEqualTo('c');
    }

    @Test
    void given_object_double_value_when_setValueOfFieldForObject_then_set_value() {
        Object value = 5.5;
        Utils.setValueOfFieldForObject(testClass, tableDouble, value);

        var res = Utils.getValueOfFieldForObject(testClass, tableDouble);

        assertThat(res).isEqualTo(5.5);
    }

    @Test
    void given_object_short_value_when_setValueOfFieldForObject_then_set_value() {
        Object value = (short) 5;
        Utils.setValueOfFieldForObject(testClass, tableShort, value);

        var res = Utils.getValueOfFieldForObject(testClass, tableShort);
        short expected = 5;
        assertThat(res).isEqualTo(expected);
    }

    @Test
    void given_object_byte_value_when_setValueOfFieldForObject_then_set_value() {
        Object value = (byte) 5;
        Utils.setValueOfFieldForObject(testClass, tableByte, value);

        var res = Utils.getValueOfFieldForObject(testClass, tableByte);
        byte expected = 5;
        assertThat(res).isEqualTo(expected);
    }

    @Test
    void given_object_value_null_when_setValueOfFieldForObject_then_set_null() {
        Object value = null;
        Utils.setValueOfFieldForObject(testClass, tableLong, value);

        var res = Utils.getValueOfFieldForObject(testClass, tableLong);

        assertThat(res).isEqualTo(null);
    }

    @Test
    void given_object_with_float_field_when_getValueOfFieldForObject_then_return_float() {
        var res = Utils.getValueOfFieldForObject(testClass, tableFloat);

        assertThat(res).isEqualTo(0.0f);
    }

    @Test
    void given_object_with_double_field_when_getValueOfFieldForObject_then_return_double() {
        var res = Utils.getValueOfFieldForObject(testClass, tableDouble);

        assertThat(res).isEqualTo(0.0);
    }

    @Test
    void given_object_with_string_field_when_getValueOfFieldForObject_then_return_string() {
        var res = Utils.getValueOfFieldForObject(testClass, tableString);

        assertThat(res).isEqualTo("String");
    }

    @Test
    void given_object_with_char_field_when_getValueOfFieldForObject_then_return_char() {
        var res = Utils.getValueOfFieldForObject(testClass, tableChar);

        assertThat(res).isEqualTo('c');
    }

    @Test
    void given_object_with_boolean_field_when_getValueOfFieldForObject_then_return_boolean() {
        var res = Utils.getValueOfFieldForObject(testClass, tableBoolean);

        assertThat(res).isEqualTo(false);
    }

    @Test
    void given_object_with_short_field_when_getValueOfFieldForObject_then_return_short() {
        var res = Utils.getValueOfFieldForObject(testClass, tableShort);

        short b = 0;

        assertThat(res).isEqualTo(b);
    }

    @Test
    void given_object_with_byte_field_when_getValueOfFieldForObject_then_return_byte() {
        var res = Utils.getValueOfFieldForObject(testClass, tableByte);

        byte b = 0;

        assertThat(res).isEqualTo(b);
    }

    @Test
    void given_object_with_wrapperLong_field_when_getValueOfFieldForObject_then_return_null() {
        var res = Utils.getValueOfFieldForObject(testClass, wrapperLong);

        assertNull(res);
    }

    @Test
    void given_object_with_annotation_id_when_checkIfObjectInDB_then_return_true() {
        orm.register(TestDBWithID.class);
        TestDBWithID newObject = new TestDBWithID("Name 1");
        orm.persist(newObject);

        boolean res = Utils.checkIfObjectInDB(newObject);

        assertTrue(res);
    }

    @Test
    void given_object_without_annotation_id_when_checkIfObjectInDB_then_return_false() {
        orm.register(TestDBWithoutID.class);
        TestDBWithoutID newObject = new TestDBWithoutID("Name 1");
        orm.persist(newObject);

        boolean res = Utils.checkIfObjectInDB(newObject);

        assertFalse(res);
    }
}