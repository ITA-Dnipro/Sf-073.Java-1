package org.example.lib.service;

import lombok.extern.slf4j.Slf4j;
import org.example.lib.annotations.Id;
import org.example.lib.annotations.OneToMany;
import org.example.lib.utils.AnnotationsUtils;
import org.example.lib.utils.SQLUtils;
import org.example.lib.utils.Utils;

import java.lang.reflect.Field;
import java.sql.ResultSet;

@Slf4j
public class MapperImpl<T> implements Mapper<T> {
    private final Class<T> cls;

    public MapperImpl(Class<T> cls) {
        this.cls = cls;
    }

    @Override
    public T mapID(ResultSet resultSet) {
        try {
            T obj = cls.getDeclaredConstructor().newInstance();
            var field = AnnotationsUtils.getFieldByAnnotation(obj,Id.class);
            if (field == null){
                return null;
            }
            field.setAccessible(true);
            var value = SQLUtils.getValueForFieldFromResultSet(resultSet,field);
            Utils.setValueOfFieldForObject(obj,field,value);
            return obj;
        } catch (Exception e) {
            log.error("Exception caught in mapRow: " + e);
            return null;
        }
    }

    @Override
    public T mapRow(ResultSet resultSet) {
        try {
            T obj = cls.getDeclaredConstructor().newInstance();
            for (Field field : cls.getDeclaredFields()) {
                field.setAccessible(true);
                if (AnnotationsUtils.isAnnotationPresent(field, OneToMany.class)) continue;
                var value = SQLUtils.getValueForFieldFromResultSet(resultSet,field);
                Utils.setValueOfFieldForObject(obj,field,value);
            }
            return obj;
        } catch (Exception e) {
            log.error("Exception caught in mapRow: " + e);
            return null;
        }
    }
}