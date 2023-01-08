package org.example.lib.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(value={METHOD,FIELD})
@Retention(value=RUNTIME)
public @interface ManyToOne {
    String columnName() default "";
} // (MEDIUM)
