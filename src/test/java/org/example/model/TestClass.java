package org.example.model;

import org.example.lib.annotations.Id;
import org.example.lib.utils.AnnotationsUtils;
import org.example.lib.utils.Utils;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public interface TestClass {
    default Object getId(){
        Field idField = AnnotationsUtils.getFieldByAnnotation(this, Id.class);
        assertNotNull(idField);
        var currData = Utils.getValueOfFieldForObject(this, idField);
        assertNotNull(currData);
        return currData;
    }
    String getName();

    void setName(String name);
}
