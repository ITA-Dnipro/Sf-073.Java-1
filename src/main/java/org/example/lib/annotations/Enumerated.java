package org.example.lib.annotations;

import org.example.lib.EnumType;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(value=FIELD)
@Retention(value=RUNTIME)
public @interface Enumerated{
    EnumType value() default EnumType.ORDINAL;
}