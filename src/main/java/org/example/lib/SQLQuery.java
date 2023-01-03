package org.example.lib;

import lombok.Data;
import org.example.lib.annotations.Entity;
import org.example.lib.annotations.Id;
import org.example.lib.annotations.ManyToOne;
import org.example.lib.annotations.OneToMany;
import org.example.lib.utils.AnnotationsUtils;
import org.example.lib.utils.SQLUtils;
import org.example.lib.utils.Utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

@Data
public class SQLQuery {
    private String sqlFields;
    private String sqlDataFields;
    private List<Object> arrayOfFields;
    private String sqlTable;
    private Boolean objectHasAutoIncrementID;
    private String generatedID;
    private Boolean paramsIsSet;
    private String idColumnName;

    public SQLQuery(Object o) {
        setParamsByObject(o);
    }

    public SQLQuery(Class<?> currClass) {
        setParamsByClass(currClass);
    }

    private void setParamsByClass(Class<?> currClass) {
        if (!AnnotationsUtils.isAnnotationPresent(currClass, Entity.class)) return;
        this.sqlTable = AnnotationsUtils.getNameOfTable(currClass);
        Field[] declaredFields = currClass.getDeclaredFields();
        StringJoiner joiner = new StringJoiner(",");
        for (Field field : declaredFields) {
            String currType;
            if (AnnotationsUtils.isAnnotationPresent(field, Id.class)) {
                this.idColumnName = AnnotationsUtils.getNameOfColumn(field);
                currType = SQLUtils.getSQLStringForIdField(field);
            } else if (field.isAnnotationPresent(ManyToOne.class)) {
                //to do
                continue;
            } else if (field.isAnnotationPresent(OneToMany.class)) {
                //to do
                continue;
            } else {
                currType = SQLUtils.getSQLStringForField(field);
            }
            if (currType.isEmpty()) continue;
            joiner.add(currType);
        }
        this.sqlFields = joiner.toString();
        this.paramsIsSet = true;
    }

    public void generateUUID() {
        this.generatedID = UUID.randomUUID().toString();
    }

    private void setParamsByObject(Object o) {
        var currClass = o.getClass();

        this.objectHasAutoIncrementID = SQLUtils.objectHasAutoIncrementID(o);
        this.sqlTable = AnnotationsUtils.getNameOfTable(currClass);

        Field[] declaredFields = currClass.getDeclaredFields();
        StringJoiner joinerFields = new StringJoiner(",");
        StringJoiner joinerDataFields = new StringJoiner(",");
        this.arrayOfFields = new ArrayList<>(declaredFields.length);

        for (Field field : declaredFields) {
            if (AnnotationsUtils.isAnnotationPresent(field, ManyToOne.class)
                    || AnnotationsUtils.isAnnotationPresent(field, OneToMany.class)) {
                continue;//to do
            }
            var currData = Utils.getValueOfFieldForObject(o, field);

            if (AnnotationsUtils.isAnnotationPresent(field, Id.class)) {
                this.idColumnName = AnnotationsUtils.getNameOfColumn(field);
                continue;
            }
            if (currData == null) continue;
            joinerFields.add(AnnotationsUtils.getNameOfColumn(field));
            arrayOfFields.add(SQLUtils.getValueFieldFromJavaToSQLType(o, field));
            joinerDataFields.add("?");
        }

        this.sqlDataFields = joinerDataFields.toString();
        this.sqlFields = joinerFields.toString();
        this.paramsIsSet = true;
    }

    public String getInsertSQLWithParams() {
        StringJoiner joinerFields = new StringJoiner(",");
        StringJoiner joinerDataFields = new StringJoiner(",");
        joinerFields.add(sqlFields);
        joinerDataFields.add(sqlDataFields);

        if (!objectHasAutoIncrementID && idColumnName != null) {
            generateUUID();
            joinerFields.add(idColumnName);
            joinerDataFields.add("?");
        }

        return "INSERT INTO " + sqlTable + " (" + joinerFields.toString() + ")" +
                " VALUES (" + joinerDataFields.toString() + ")";
    }

    public String getUpdateSQLWithIdParam() {
        return "UPDATE " + sqlTable + " SET " + sqlFields.replaceAll(",","=?, ") +"=?"+
                " where "+idColumnName+" = ?";
    }

    public String getCreateTableSQL() {
        return "CREATE TABLE IF NOT EXISTS " + sqlTable +
                " (" + sqlFields + ")";
    }

    public String getDeleteSQLWithParams() {
        return "DELETE FROM " + sqlTable + " where "+idColumnName+" = ?";
    }
}
