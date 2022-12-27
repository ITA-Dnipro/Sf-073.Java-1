package org.example.lib;

import lombok.extern.slf4j.Slf4j;
import org.example.lib.annotations.*;
import org.example.lib.exceptions.ObjectAlreadyExistException;
import org.example.lib.utils.AnnotationsUtils;
import org.example.lib.utils.Repository;
import org.example.lib.utils.SQLUtils;
import org.example.lib.utils.Utils;

import javax.sql.DataSource;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
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
    public boolean checkConnectionToDB() {
        return repository.checkConnection();
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
    public void persist(Object o) throws ObjectAlreadyExistException {
        if (Utils.checkIfObjectInDB(o)
        && !SQLUtils.getTypeOfIDField(o).contains("VARCHAR") ){
            throw new ObjectAlreadyExistException("Object already exist in database!");
            }
        var currClass = o.getClass();
        String nameOfTable = AnnotationsUtils.getNameOfTable(currClass);
        Field[] declaredFields = currClass.getDeclaredFields();
        StringJoiner joinerFields = new StringJoiner(",");
        StringJoiner joinerDataFields = new StringJoiner(",");
        var arrayOfFields = new ArrayList<>(declaredFields.length);
        for (Field field : declaredFields) {
            joinerFields.add(AnnotationsUtils.getNameOfColumn(field));
            joinerDataFields.add("?");
            arrayOfFields.add(SQLUtils.getDataObjectFieldInSQLType(o,field));
        }

        String sql = "INSERT INTO " + nameOfTable+" ("+joinerFields+")" +
                " VALUES (" + joinerDataFields + ")";

        repository.update(sql,arrayOfFields);

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
