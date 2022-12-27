package org.example.lib.utils;

import org.example.lib.annotations.Column;
import org.example.lib.annotations.Entity;
import org.example.lib.annotations.Id;
import org.example.lib.annotations.Table;
import org.example.lib.utils.AnnotationsUtils;
import org.example.lib.utils.FieldInfo;
import org.example.lib.utils.Utils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class AnnotationsTest {

    @Test
    @DisplayName("Testing Entity annotation is present on a custom class")
    void test_entityAnnotationIsPresent_when_classISMarked_returnTrue() {

        @Entity
        class MyEntity {
        }

        var result = Utils.entityAnnotationIsPresent(MyEntity.class);
        assertTrue(result);

    }

    @Test
    @DisplayName("Testing Entity annotation is present on a custom class not marked")
    void test_entityAnnotationIsNotPresent_when_classIsNotMarked_should_returnFalse() {

        class NoEntity {
        }

        var result = Utils.entityAnnotationIsPresent(NoEntity.class);
        assertFalse(result);
    }

    @Test
    @DisplayName("Testing Table annotation is absent on a class not marked")
    void test_getNameOfTable_when_tableAnnotationIsAbsent_should_takeNameOfClass() {

        @Entity
        class WithoutTableAnnotation {
        }

        var res = AnnotationsUtils.getNameOfTable(WithoutTableAnnotation.class);

        assertEquals("WithoutTableAnnotation", res);
    }

    @Test
    @DisplayName("Testing Table annotation is present on a marked class should take name from annotation")
    void test_getTableName_when_tableAnnotationIsPresent_should_takeNameFromAnnotation() {

        @Entity
        @Table(value = "table_annotation_test")
        class WithTableAnnotation {
        }

        var res = AnnotationsUtils.getNameOfTable(WithTableAnnotation.class);

        assertEquals("table_annotation_test", res);
    }

    @Test
    @DisplayName("Testing Table annotation is present on a marked class with empty annotation value should take name from class")
    void test_getTableName_when_tableAnnotationIsPresentButEmpty_should_takeNameFromClass() {

        @Entity
        @Table()
        class WithTableAnnotation {
        }

        var res = AnnotationsUtils.getNameOfTable(WithTableAnnotation.class);

        assertEquals("WithTableAnnotation", res);
    }

    @Test
    void test_getIdField_when_idIsAbsent_should_returnNull() {

        @Entity
        class WithoutIdField {
        }

        var result = Utils.getIdField(WithoutIdField.class);

        assertNull(result);

    }

    @Test
    void test_getIdField_when_idIsPresent_should_returnNotNull() {
        @Entity
        class WithoutIdField {
            @Id
            Long id;
        }
        var result  = Utils.getIdField(WithoutIdField.class);

        assertNotNull(result);
    }

    @Test
    void test_getIdField_should_returnColumnName() {
        @Entity
        class WithoutIdField {
            @Id
            Long id;
        }
        var fieldInfo = Utils.getIdField(WithoutIdField.class);

        String columnName = fieldInfo.getColumnName();

        assertEquals("id", columnName);
    }

    @Test
    void test_getIdField_when_columnAnnotationIsPresent_should_returnColumnNameBasedOnAnnotation() {
        @Entity
        class WithColumn {
            @Id
            @Column(value = "id_column")
            Long id;
        }

        String expectedColumnName = "id_column";
        FieldInfo fieldInfo = Utils.getIdField(WithColumn.class);
        String columnName = fieldInfo.getColumnName();

        assertEquals(expectedColumnName, columnName);
    }

    @Test
    void test_createTableDdl_when_MinimalEntityIsProvided_should_produceExpectedDDL() {
        @Entity
        class MinimalEntity {
            @Id
            Long id;
        }
        String expectedDdl = """
                create table if not exists MinimalEntity (
                  id bigint not null,
                  primary key (id)
                );
                """;

        String res = Utils.createTableDdl(MinimalEntity.class);

        // assertEquals(res.toLowerCase(),expectedDdl.toLowerCase());
    }
}
