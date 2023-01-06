package org.example.lib;

import lombok.extern.slf4j.Slf4j;
import org.example.lib.annotations.*;
import org.example.lib.exceptions.ObjectAlreadyExistException;
import org.example.lib.service.Mapper;
import org.example.lib.service.MapperImpl;
import org.example.lib.service.Repository;
import org.example.lib.utils.AnnotationsUtils;
import org.example.lib.utils.SQLUtils;
import org.example.lib.utils.Utils;

import javax.sql.DataSource;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.*;
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
        var classesToRegister = new HashSet<Class>();
        for (Class currClass : entityClasses) {
            if (!AnnotationsUtils.isAnnotationPresent(currClass, Entity.class)){
                //to do exception
                continue;
            }
            classesToRegister.add(currClass);
        }

        var registeredClasses = registerClassWithReferences(classesToRegister);

        for (Class currClass : classesToRegister) {
            if (registeredClasses.contains(currClass)){
                continue;
            }
            registerClassInDB(currClass);
        }
    }

    private HashSet<Class> registerClassWithReferences(HashSet<Class> classesToRegister) {
        var registeredClasses = new HashSet<Class>();

        for (Class currClass : classesToRegister) {
            var field = AnnotationsUtils.getFieldByAnnotation(currClass,ManyToOne.class);
            if (field == null){
                continue;
            }
            var typeClass = field.getType();
            if (!classesToRegister.contains(typeClass)){
                //to do exception
                continue;
            }
            var fieldId = AnnotationsUtils.getFieldByAnnotation(typeClass,Id.class);
            if (fieldId == null){
                //to do exception
                continue;
            }
            registerClassInDB(typeClass);
            registeredClasses.add(typeClass);
        }
        return registeredClasses;
    }

    private void registerClassInDB(Class currClass) {
        SQLQuery sql = new SQLQuery(currClass);
        if (sql.getParamsIsSet()) {
            repository.update(sql.getCreateTableSQL());
        } else {
            log.error("Error creating sql query for class " + currClass.getSimpleName() + " cannot set parameters by class!");
        }
    }

    @Override
    public <T> T save(T o) {
        if (Utils.checkIfObjectInDB(o)) {
            return merge(o);
        }
        persist(o);
        return o;
    }

    @Override
    public void persist(Object o) throws ObjectAlreadyExistException {
        SQLQuery sqlQuery = new SQLQuery(o);
        if (!sqlQuery.getParamsIsSet()) {
            log.error("Error reading params for sql from object " + o);
            return;
        }
        var objectHasAutoIncrementID = SQLUtils.objectHasAutoIncrementID(o);

        if (Utils.checkIfObjectInDB(o)
                && objectHasAutoIncrementID) {
            var message = "Try to persist an existing object. Object " + o + " already exist in database!";
            log.error(message);
            throw new ObjectAlreadyExistException(message);
        }

        var status = false;

        if (objectHasAutoIncrementID) {
            status = insertObjectWithAutoIncrementToDatabase(sqlQuery, o);
        } else {
            status = insertObjectToDatabase(sqlQuery, o);
        }
        if (!status) {
            log.error("Object " + o + " not saved to DB!");
        }
    }

    private boolean insertObjectToDatabase(SQLQuery sqlQuery, Object o) {
        var status = false;

        var sql = sqlQuery.getInsertSQLWithParams();
        Field idField = AnnotationsUtils.getFieldByAnnotation(o, Id.class);
        var arrayOfFields = sqlQuery.getArrayOfFields();
        if (idField != null) {
            arrayOfFields.add(SQLUtils.getValueFromJavaToSQLType(sqlQuery.getGeneratedID(),idField.getType()));
        }
        status = repository.update(sql, arrayOfFields);
        if (status && idField != null) {
            var generatedID = sqlQuery.getGeneratedID();
            Utils.setValueOfFieldForObject(o, idField, generatedID);
        }
        return status;
    }

    private <T> boolean insertObjectWithAutoIncrementToDatabase(SQLQuery sqlQuery, T o) {
        Field idField = AnnotationsUtils.getFieldByAnnotation(o, Id.class);
        if (idField == null) return false;

        var arrayOfFields = sqlQuery.getArrayOfFields();
        Mapper<T> mapper = new MapperImpl(o.getClass());
        var objectWithId = repository.updateAndGetObjectWithID(sqlQuery.getInsertSQLWithParams(), arrayOfFields, mapper);
        if (objectWithId == null) {
            return false;
        }
        Utils.copyValueOfFieldForObject(o, objectWithId, idField);
        return true;

    }

    @Override
    public <T> Optional<T> findById(Serializable id, Class<T> cls) {
        Field idField = AnnotationsUtils.getFieldByAnnotation(cls,Id.class);
        if (idField == null){
            log.error("Cannot find by id for class because class "+cls.getSimpleName()+" has no Id field");
            return Optional.empty();
        }

        SQLQuery sqlQuery = new SQLQuery(cls);
        if (!sqlQuery.getParamsIsSet()){
            log.error("Error reading params for sql from class "+cls.getSimpleName());
            return Optional.empty();
        }

        String sql = sqlQuery.getSelectSQLWithParams();
        ArrayList<Object> params = new ArrayList<>();
        params.add(id);
        var mapper = new MapperImpl<>(cls);

        T res = repository.queryForObject(sql, params, mapper);
        return Optional.ofNullable(res);
    }

    @Override
    public <T> List<T> findAll(Class<T> cls) {
        SQLQuery sqlQuery = new SQLQuery(cls);
        if (!sqlQuery.getParamsIsSet()) {
            log.error("Error reading params for sql from class " + cls.getSimpleName());
            return Collections.emptyList();
        }

        String sql = sqlQuery.getSelectAllSQLWithParams();
        var mapper = new MapperImpl<>(cls);

        return repository.queryForList(sql, Collections.emptyList(), mapper);
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
        var idField = AnnotationsUtils.getFieldByAnnotation(o, Id.class);
        if (idField == null) {
            log.error("Try to merge object without id field!");
            return o;
        }

        if (!Utils.checkIfObjectInDB(o)) {
            log.error("Try to merge new object (object " + o + " not found in database!))");
            return o;
        }

        SQLQuery sqlQuery = new SQLQuery(o);
        if (!sqlQuery.getParamsIsSet()) {
            log.error("Error reading params for sql from object " + o);
            return o;
        }
        var arrayOfFields = sqlQuery.getArrayOfFields();
        arrayOfFields.add(SQLUtils.getValueFieldFromObjectToSQLType(o, idField));
        var status = repository.update(sqlQuery.getUpdateSQLWithIdParam(), arrayOfFields);

        if (!status) {
            log.error("An error while merge object " + o + "!");
        }
        return o;
    }

    @Override
    public <T> T refresh(T o) {
        var field = AnnotationsUtils.getFieldByAnnotation(o, Id.class);
        if (field == null) {
            log.error("Try to refresh object without id field!");
            return o;
        }
        var currId = Utils.getValueOfFieldForObject(o, field);
        var objectFromDB = findById((Serializable) currId, o.getClass());

        objectFromDB.ifPresent(objFrom -> Utils.copyFieldsOfObject(o, objFrom));

        return o;
    }

    @Override
    public boolean delete(Object o) {
        Field idField = AnnotationsUtils.getFieldByAnnotation(o, Id.class);
        if (idField == null) {
            log.error("Cannot delete object because object " + o + " has no Id field");
            return false;
        }

        var currValue = SQLUtils.getValueFieldFromObjectToSQLType(o, idField);
        if (currValue == null) {
            log.error("Cannot delete object because id is null or not set " + o);
            return false;
        }
        SQLQuery sqlQuery = new SQLQuery(o);
        if (!sqlQuery.getParamsIsSet()) {
            log.error("Error reading params for sql from object " + o);
            return false;
        }

        String sql = sqlQuery.getDeleteSQLWithParams();
        ArrayList<Object> params = new ArrayList<>();
        params.add(currValue);

        var status = repository.update(sql, params);

        if (status && sqlQuery.getObjectHasAutoIncrementID()) {
            Utils.setNullToFieldForObject(o, idField);
        }

        return status;
    }

    @Override
    public long getCount(Object o) {
        SQLQuery sqlQuery = new SQLQuery(o);
        String sql = sqlQuery.getCountSQL();

        return repository.count(sql);
    }
}
