package com.yammer.dropwizard.auth;

import java.lang.annotation.*;

@Target({ ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auth {
    boolean required() default true;
}
