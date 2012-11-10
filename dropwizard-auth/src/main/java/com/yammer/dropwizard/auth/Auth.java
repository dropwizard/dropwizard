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
@Target({ ElementType.PARAMETER, ElementType.FIELD })
public @interface Auth {
    /**
     * If {@code true}, the request will not be processed in the absence of a valid principal. If
     * {@code false}, {@code null} will be passed in as a principal. Defaults to {@code true}.
     */
    boolean required() default true;
}
