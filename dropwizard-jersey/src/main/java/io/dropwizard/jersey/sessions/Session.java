package io.dropwizard.jersey.sessions;

import java.lang.annotation.*;

import javax.inject.Qualifier;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
public @interface Session {
    boolean doNotCreate() default false;
}
