package org.example.lib.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(value={METHOD,FIELD})
@Retention(value=RUNTIME)
// 2nd part
public @interface OneToMany {
    String mappedBy() default "";
}  // (MEDIUM)
