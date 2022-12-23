package org.example.lib;

import lombok.extern.slf4j.Slf4j;
import org.example.lib.annotations.*;
import org.example.lib.utils.AnnotationsUtils;
import org.example.lib.utils.Repository;
import org.example.lib.utils.SQLUtils;

import javax.sql.DataSource;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Stream;

@Slf4j
public class ORManagerImpl implements ORManager {
   private final Repository repository;

    public ORManagerImpl(DataSource dataSource) {
        this.repository = new Repository(dataSource);
    }

    @Override
    public void register(Class... entityClasses) {
        for (Class currClass : entityClasses) {
            if (!AnnotationsUtils.isAnnotationPresent(currClass, Entity.class)) continue;
            String nameOfTable = AnnotationsUtils.getNameOfTable(currClass);
            Field[] declaredFields = currClass.getDeclaredFields();
            StringJoiner joiner = new StringJoiner(",");
            for (Field field : declaredFields) {
                if (AnnotationsUtils.isAnnotationPresent(field, Id.class)) {
                    var currType = SQLUtils.getSQLStringForIdField(field);
                    if (currType.isEmpty()) continue;
                    joiner.add(currType);
                } else if (field.isAnnotationPresent(ManyToOne.class)) {
                    //to do
                } else if (field.isAnnotationPresent(OneToMany.class)) {
                    //to do
                } else {
                    var currType = SQLUtils.getSQLStringForField(field);
                    if (currType.isEmpty()) continue;
                    joiner.add(currType);
                }
            }

            String sql = "CREATE TABLE IF NOT EXISTS " + nameOfTable +
                    " (" + joiner + ")";

            repository.update(sql);
        }
    }

    @Override
    public <T> T save(T o) {
        return null;
    }

    @Override
    public void persist(Object o) {

    }

    @Override
    public <T> Optional<T> findById(Serializable id, Class<T> cls) {
        return Optional.empty();
    }

    @Override
    public <T> List<T> findAll(Class<T> cls) {
        return null;
    }

    @Override
    public <T> Iterable<T> findAllAsIterable(Class<T> cls) {
        return null;
    }

    @Override
    public <T> Stream<T> findAllAsStream(Class<T> cls) {
        return null;
    }

    @Override
    public <T> T merge(T o) {
        return null;
    }

    @Override
    public <T> T refresh(T o) {
        return null;
    }

    @Override
    public boolean delete(Object o) {
        return false;
    }
}
