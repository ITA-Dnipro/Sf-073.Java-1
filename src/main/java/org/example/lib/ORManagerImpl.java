package org.example.lib;

import lombok.extern.slf4j.Slf4j;
import org.example.lib.annotations.*;
import org.example.lib.exceptions.ObjectAlreadyExistException;
import org.example.lib.service.Repository;
import org.example.lib.utils.AnnotationsUtils;
import org.example.lib.utils.SQLUtils;
import org.example.lib.utils.Utils;

import javax.sql.DataSource;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
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
            SQLQuery sql = new SQLQuery(currClass);
            if (sql.getParamsIsSet()) {
                repository.update(sql.getCreateTableSQL());
            }else{
                log.error("Error creating sql query for class "+currClass.getSimpleName());
            }
        }
    }

    @Override
    public <T> T save(T o) {
        if (Utils.checkIfObjectInDB(o)){
            return merge(o);
        }
        persist(o);
        return o;
    }

    @Override
    public void persist(Object o) throws ObjectAlreadyExistException {
        var objectHasAutoIncrementID = SQLUtils.objectHasAutoIncrementID(o);

        if (Utils.checkIfObjectInDB(o)
        && objectHasAutoIncrementID){
            throw new ObjectAlreadyExistException("Try to persist an existing object. Object "+o+" already exist in database!");
            }

        if (!saveObjectToDB(o)){
            log.error("Object "+o+" not saved in DB!");
        }
    }

    private boolean saveObjectToDB(Object o){
        SQLQuery sqlQuery = new SQLQuery(o);
        var objectHasAutoIncrementID = sqlQuery.getObjectHasAutoIncrementID();
        var generatedID = sqlQuery.getGeneratedID();
        if(objectHasAutoIncrementID) {
            return updateObjectWithAutoIncrementInDatabase(sqlQuery, o);
        } else if(repository.update(sqlQuery.getInsertSQLWithParams(), sqlQuery.getArrayOfFields()) && generatedID != null ) {
            var fieldID = AnnotationsUtils.getFieldByAnnotation(o,Id.class);
            if (fieldID != null) {
                Utils.setValueOfFieldForObject(o,fieldID,generatedID);
                return true;
            }
        }
        return false;
    }

    private boolean updateObjectWithAutoIncrementInDatabase(SQLQuery sqlQuery, Object o){
        Field idField = AnnotationsUtils.getFieldByAnnotation(o,Id.class);
        if(idField != null && SQLUtils.objectHasAutoIncrementID(o)) {
            if (Utils.checkIfObjectInDB(o)) {
                var arrayOfFields = sqlQuery.getArrayOfFields();
                arrayOfFields.add(SQLUtils.getDataObjectFieldInSQLType(o, idField));
                return repository.update(sqlQuery.getUpdateSQLWithIdParam(),arrayOfFields);
            }
            var mapper = Utils.getMapperForClass(o.getClass());
            if (mapper == null) {
                return false;
            }
            var objectWithId = repository.updateAndGetObjectWithID(sqlQuery.getInsertSQLWithParams(), sqlQuery.getArrayOfFields(),mapper);
            if (objectWithId == null) {
                return false;
            }
            Utils.copyValueOfFieldForObject(o,objectWithId,idField);
            return true;
        }else {
            return repository.update(sqlQuery.getInsertSQLWithParams(), sqlQuery.getArrayOfFields());
        }
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
        var field = AnnotationsUtils.getFieldByAnnotation(o,Id.class);
        if (field == null){
            log.error("Try to merge object without id field!");
            return o;
        }
        saveObjectToDB(o);
        return o;
    }

    @Override
    public <T> T refresh(T o) {
        var field = AnnotationsUtils.getFieldByAnnotation(o,Id.class);
        if (field == null){
            log.error("Try to refresh object without id field!");
            return o;
        }
        var currId = Utils.getValueOfFieldForObject(o,field);
        var objectFromDB = findById((Serializable) currId,o.getClass());

        objectFromDB.ifPresent(objFrom -> Utils.copyFieldsOfObject(o,objFrom));

        return o;
    }

    @Override
    public boolean delete(Object o) {
        Field idField = AnnotationsUtils.getFieldByAnnotation(o,Id.class);
        if (idField == null){
            log.error("Cannot delete object because object "+o+" has no Id field");
            return false;
        }

        var currValue = SQLUtils.getDataObjectFieldInSQLType(o, idField);
        if (currValue == null){
            log.error("Cannot delete object because id is null or not set "+o);
            return false;
        }
        SQLQuery sqlQuery = new SQLQuery(o);
        if (!sqlQuery.getParamsIsSet()){
            log.error("Error reading params for sql from object "+o);
            return false;
        }

        String sql = sqlQuery.getDeleteSQLWithParams();
        ArrayList<Object> params = new ArrayList<>();
        params.add(currValue);

        var status = repository.update(sql, params);

        if(status && sqlQuery.getObjectHasAutoIncrementID()){
            Utils.setNullToFieldForObject(o, idField);
        }

        return status;
    }
}
