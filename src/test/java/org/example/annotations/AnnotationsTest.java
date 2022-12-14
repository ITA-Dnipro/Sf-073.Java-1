package org.example.annotations;

import org.example.lib.annotations.Column;
import org.example.lib.annotations.Entity;
import org.example.lib.annotations.Id;
import org.example.lib.annotations.Table;
import org.example.lib.utils.AnnotationsUtils;
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

        var result = AnnotationsUtils.isAnnotationPresent(MyEntity.class,Entity.class);
        assertTrue(result);

    }

    @Test
    @DisplayName("Testing Entity annotation is present on a custom class not marked")
    void test_entityAnnotationIsNotPresent_when_classIsNotMarked_should_returnFalse() {

        class NoEntity {
        }
        var result = AnnotationsUtils.isAnnotationPresent(NoEntity.class,Entity.class);
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
        var result = AnnotationsUtils.getFieldByAnnotation(WithoutIdField.class, Id.class);

        assertNull(result);

    }

    @Test
    void test_getIdField_when_idIsPresent_should_returnNotNull() {
        @Entity
        class WithoutIdField {
            @Id
            Long id;
        }
        var result = AnnotationsUtils.getFieldByAnnotation(WithoutIdField.class, Id.class);

        assertNotNull(result);
    }

    @Test
    void test_getIdField_should_returnColumnName() {
        @Entity
        class WithoutIdField {
            @Id
            Long id;
        }
        var field = AnnotationsUtils.getFieldByAnnotation(WithoutIdField.class, Id.class);

        String columnName = AnnotationsUtils.getNameOfColumn(field);

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
        var field = AnnotationsUtils.getFieldByAnnotation(WithColumn.class, Id.class);
        String columnName = AnnotationsUtils.getNameOfColumn(field);

        assertEquals(expectedColumnName, columnName);
    }

}