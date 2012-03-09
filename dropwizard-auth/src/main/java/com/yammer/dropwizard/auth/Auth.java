package com.yammer.dropwizard.auth;

import java.lang.annotation.*;

/**
 * This annotation is used to inject authenticated principal objects into protected JAX-RS resource
 * methods.
 *
 * @see Authenticator
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD })
public @interface Auth {
    boolean required() default true;
}
