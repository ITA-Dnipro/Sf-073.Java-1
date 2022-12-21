package org.example.lib.annotations;

import org.example.lib.EnumType;
import org.example.lib.IDType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Id {
    IDType value() default IDType.SERIAL;
} //                              (HIGH)
