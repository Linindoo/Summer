package ren.yale.java.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Service {
    String value() default "";
    String type();
}
