package com.yammer.dropwizard;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for classes being parsed by
 * {@link com.yammer.dropwizard.jersey.JacksonMessageBodyProvider}. If present, the class will be
 * validated using {@link com.yammer.dropwizard.util.Validator}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Validated {
    // TODO: 11/10/11 <coda> -- add support for Validated for Scala
}
