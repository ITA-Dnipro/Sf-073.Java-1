package org.example.lib.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Id {
    IDType value() default IDType.SERIAL;

    enum IDType {
        SERIAL,
        UUID
    }

} //                              (HIGH)
