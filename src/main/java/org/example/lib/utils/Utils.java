package org.example.lib.utils;
import lombok.extern.slf4j.Slf4j;
import org.example.lib.ORManager;
import org.example.lib.ORManagerImpl;
import org.example.lib.annotations.Id;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

@Slf4j
public class Utils {
    private Utils() {
    }

    public static ORManager getORMImplementation(DataSource dataSource){
           return new ORManagerImpl(dataSource);
    }

    private static String firstUpperCase(String word){
        if(word == null || word.isEmpty()) return "";//или return word;
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    public static Object getValueOfFieldForObject(Object o, Field field) {
        var methodName = Utils.firstUpperCase(field.getName());
        var className = o.getClass().getName();

        try {
            var methodObject = o.getClass().getDeclaredMethod("get"+Utils.firstUpperCase(field.getName()));
            return methodObject.invoke(o);
        } catch (NoSuchMethodException e) {
            log.error("Method "+methodName+" for "+className +" is not found! "+e);
            return null;
        } catch (InvocationTargetException | IllegalAccessException e) {
            log.error("Calling "+methodName+" for "+className +" led to an error:" + e);
            return null;
        }
    }

    public static boolean checkIfObjectInDB(Object o) {
        var field = AnnotationsUtils.getFieldByAnnotation(o,Id.class);
        if (field == null) return false;

        var primaryKey = getValueOfFieldForObject(o,field);
        return primaryKey != null;
    }

}
