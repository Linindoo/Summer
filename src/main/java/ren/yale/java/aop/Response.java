package ren.yale.java.aop;

import ren.yale.java.ResponseHandler;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Response {
    Class<? extends ResponseHandler> value();
}
