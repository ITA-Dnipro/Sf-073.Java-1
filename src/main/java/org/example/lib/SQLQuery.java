package org.example.lib;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class SQLQuery {
    private String sqlFields;
    private String sqlDataFields;
    private List<Object> arrayOfFields;
    private String sqlTable;
    private Boolean objectHasAutoIncrementID;
    private UUID generatedID;
    private Boolean paramsIsSet;
    private String idColumnName;
    private List<Field> arrayFieldsWithFK;

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
        this.arrayFieldsWithFK = new ArrayList<>(declaredFields.length);

        if (AnnotationsUtils.isAnnotationPresent(currClass,OneToMany.class)) {
            log.warn("Unsupported annotation OneToMany in class "+currClass.getSimpleName()+"- annotation has not been implemented! Fields will not be processed");
        }

        for (Field field : declaredFields) {
            if (Utils.IsServiceField(field)) continue;
            String currType;
            if (AnnotationsUtils.isAnnotationPresent(field, Id.class)) {
                this.idColumnName = AnnotationsUtils.getNameOfColumn(field);
                currType = SQLUtils.getSQLStringForIdField(field);
            } else if (AnnotationsUtils.isAnnotationPresent(field,ManyToOne.class)) {
                currType = SQLUtils.getSQLStringForFieldManyToOne(field);
                if (!currType.isEmpty()) arrayFieldsWithFK.add(field);
            } else if (AnnotationsUtils.isAnnotationPresent(field,OneToMany.class)) {
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
        this.generatedID = UUID.randomUUID();
    }

    private void setParamsByObject(Object o) {
        var currClass = o.getClass();

        this.objectHasAutoIncrementID = SQLUtils.objectHasAutoIncrementID(o);
        this.sqlTable = AnnotationsUtils.getNameOfTable(currClass);

        Field[] declaredFields = currClass.getDeclaredFields();
        StringJoiner joinerFields = new StringJoiner(",");
        StringJoiner joinerDataFields = new StringJoiner(",");
        this.arrayOfFields = new ArrayList<>(declaredFields.length);
        this.arrayFieldsWithFK = new ArrayList<>(declaredFields.length);

        if (AnnotationsUtils.isAnnotationPresent(currClass,OneToMany.class)) {
            log.warn("Unsupported annotation OneToMany in object "+o+"- annotation has not been implemented! Fields will not be processed");
        }

        for (Field field : declaredFields) {
            if (Utils.IsServiceField(field)) continue;
            if (AnnotationsUtils.isAnnotationPresent(field, ManyToOne.class)) {
                var currRef = Utils.getValueOfFieldForObject(o, field);
                if (currRef == null) continue;
                joinerFields.add(AnnotationsUtils.getNameOfColumn(field));
                var fieldId = AnnotationsUtils.getFieldByAnnotation(currRef,Id.class);
                arrayOfFields.add(SQLUtils.getValueFieldFromObjectToSQLType(currRef, fieldId));
                joinerDataFields.add("?");
                arrayFieldsWithFK.add(field);
                continue;
            }

            if (AnnotationsUtils.isAnnotationPresent(field, OneToMany.class)) {
                continue;//to do
            }

            var currData = Utils.getValueOfFieldForObject(o, field);

            if (AnnotationsUtils.isAnnotationPresent(field, Id.class)) {
                this.idColumnName = AnnotationsUtils.getNameOfColumn(field);
                continue;
            }
            if (currData == null) continue;
            joinerFields.add(AnnotationsUtils.getNameOfColumn(field));
            arrayOfFields.add(SQLUtils.getValueFieldFromObjectToSQLType(o, field));
            joinerDataFields.add("?");
        }

        this.sqlDataFields = joinerDataFields.toString();
        this.sqlFields = joinerFields.toString();
        this.paramsIsSet = true;
    }

    public String getInsertSQLWithParams() {
        if (sqlFields.isEmpty()) return "";

        StringJoiner joinerFields = new StringJoiner(",");
        StringJoiner joinerDataFields = new StringJoiner(",");
        joinerFields.add(sqlFields);
        joinerDataFields.add(sqlDataFields);

        if (!objectHasAutoIncrementID && idColumnName != null) {
            generateUUID();
            joinerFields.add(idColumnName);
            joinerDataFields.add("?");
        }

        return "INSERT INTO " + sqlTable + " (" + joinerFields+ ")" +
                " VALUES (" + joinerDataFields + ")";
    }

    public String getUpdateSQLWithIdParam() {
        if (sqlFields.isEmpty()) return "";
        return "UPDATE " + sqlTable + " SET " + sqlFields.replaceAll(",","=?, ") +"=?"+
                " where "+idColumnName+" = ?";
    }

    public String getCreateTableSQL() {
        if (sqlFields.isEmpty()) return "";
        return "CREATE TABLE IF NOT EXISTS " + sqlTable +
                " (" + sqlFields + ")";
    }

    public String getCreateFKSQL(Field field) {
        for (Field currFieldFk:arrayFieldsWithFK) {
            if (!currFieldFk.equals(field)) continue;

            String currFk = SQLUtils.getSQLStringForForeignKey(currFieldFk);
            if (currFk.isEmpty()) break;

            return "ALTER TABLE  " + sqlTable +
                    " ADD " + currFk + "";
        }
        return "";
    }

    public String getDeleteFKSQL(Field field) {
        for (Field currFieldFk:arrayFieldsWithFK) {
            if (!currFieldFk.equals(field)) continue;

            String currFk = AnnotationsUtils.getNameOfColumn(field);
            if (currFk.isEmpty()) break;

            return "ALTER TABLE  " + sqlTable +
                    " DROP CONSTRAINT " + currFk + "";
        }
        return "";
    }

    public String getCountSQL() {
        return "SELECT COUNT(*) FROM " + sqlTable;
    }

    public  String getSelectSQLWithParams() {
        return "SELECT * FROM " + sqlTable + " where "+idColumnName+" = ?";
    }

    public String getDeleteSQLWithParams() {
        return "DELETE FROM " + sqlTable + " where "+idColumnName+" = ?";
    }

    public String getSelectAllSQLWithParams() {
        return "SELECT * FROM " + sqlTable ;
    }
}
