package org.example.lib;

import org.example.lib.annotations.Column;
import org.example.lib.annotations.Entity;
import org.example.lib.annotations.Enumerated;
import org.example.lib.annotations.Id;
import org.example.lib.utils.AnnotationsUtils;
import org.example.model.TestDBWithID;
import org.example.model.TestDBWithIDWithoutTable;
import org.example.model.TestDBWithoutID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnnotationsUtilsTest {

    @Test
    void Is_Annotation_present_for_Class() {
        var result = AnnotationsUtils.isAnnotationPresent(TestDBWithID.class,Entity.class);
        assertTrue(result);
    }

    @Test
    void Is_Annotation_Not_present_for_Class() {
        class WithoutEntity {
            @Column("test")
            Long id;
        }
        var result = AnnotationsUtils.isAnnotationPresent(WithoutEntity.class,Entity.class);
        assertFalse(result);
    }

    @Test
    void Is_Annotation_present_for_Field() {
        var field = AnnotationsUtils.getFieldByAnnotation(TestDBWithID.class, Id.class);
        assertNotNull(field);
    }


    @Test
    void Is_Annotation_Not_present_for_Field() {
        var field = AnnotationsUtils.getFieldByAnnotation(TestDBWithoutID.class, Id.class);
        assertNull(field);
    }

    @Test
    void Test_Name_Of_Column() {
        var field = AnnotationsUtils.getFieldByAnnotation(TestDBWithID.class, Id.class);
        assertNotNull(field);

        String columnName = AnnotationsUtils.getNameOfColumn(field);

        assertEquals("id", columnName);
    }

    @Test
    void Test_Name_Of_Column_With_Name_In_Annotation() {
        var field = AnnotationsUtils.getFieldByAnnotation(TestDBWithoutID.class, Column.class);
        assertNotNull(field);

        String columnName = AnnotationsUtils.getNameOfColumn(field);

        assertEquals("id", columnName);
    }

    @Test
    void Test_Name_Of_Column_With_Name_In_Field() {
        @Entity
        class WithoutIdWithColumnField {
            @Column
            Long id;
        }
        var field = AnnotationsUtils.getFieldByAnnotation(WithoutIdWithColumnField.class, Column.class);
        assertNotNull(field);

        String columnName = AnnotationsUtils.getNameOfColumn(field);

        assertEquals("id", columnName);
    }

    @Test
    void Get_ID_Type_Return_UUID() {
        class WithoutEntityWithIDField {
            @Id(Id.IDType.UUID)
            Long id;
        }
        var field = AnnotationsUtils.getFieldByAnnotation(WithoutEntityWithIDField.class, Id.class);

        assertNotNull(field);

        assertEquals(Id.IDType.UUID, AnnotationsUtils.getIdType(field));
    }

    @Test
    void Get_ID_Type_Return_SERIAL() {
        class WithoutEntityWithIDField {
            @Id
            Long id;
        }
        var field = AnnotationsUtils.getFieldByAnnotation(WithoutEntityWithIDField.class, Id.class);

        assertNotNull(field);

        assertEquals(Id.IDType.SERIAL, AnnotationsUtils.getIdType(field));
    }

    @Test
    void Get_Name_Of_Table_By_Class() {
        var res = AnnotationsUtils.getNameOfTable(TestDBWithIDWithoutTable.class);
        assertEquals("TestDBWithIDWithoutTable", res);
    }

    @Test
    void Get_Name_Of_Table_By_Annotation() {
        var res = AnnotationsUtils.getNameOfTable(TestDBWithID.class);
        assertEquals("testID", res);
    }

    @Test
    void Get_ID_Type_Return_STRING() {
        class WithoutEntityWithEntityField {
            @Enumerated(Enumerated.EnumType.STRING)
            Long id;
        }
        var field = AnnotationsUtils.getFieldByAnnotation(WithoutEntityWithEntityField.class, Enumerated.class);

        assertNotNull(field);

        assertEquals(Enumerated.EnumType.STRING, AnnotationsUtils.getEnumType(field));
    }

    @Test
    void Get_ID_Type_Return_Ordinal() {
        class WithoutEntityWithEntityField {
            @Enumerated
            Long id;
        }
        var field = AnnotationsUtils.getFieldByAnnotation(WithoutEntityWithEntityField.class, Enumerated.class);

        assertNotNull(field);

        assertEquals(Enumerated.EnumType.ORDINAL, AnnotationsUtils.getEnumType(field));

    }

    @Test
    void Get_field_By_Annotation_If_Present() {
        class WithoutEntityWithIDField {
            @Id
            Long id;
        }
        var field = AnnotationsUtils.getFieldByAnnotation(WithoutEntityWithIDField.class, Id.class);
        assertNotNull(field);
    }

    @Test
    void Get_field_By_Annotation_If_Not_Present() {
        class WithoutIDField {
            Long id;
        }
        var field = AnnotationsUtils.getFieldByAnnotation(WithoutIDField.class, Id.class);
        assertNull(field);
    }

}