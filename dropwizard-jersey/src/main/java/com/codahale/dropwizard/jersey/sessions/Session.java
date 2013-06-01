package com.codahale.dropwizard.jersey.sessions;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
public @interface Session {
    boolean doNotCreate() default false;
}
