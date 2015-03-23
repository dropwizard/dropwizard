package io.dropwizard.auth;

import java.lang.annotation.*;

/**
 * This annotation is used filter resource methods and provide a proper SecurityContext
 *
 * @see Authenticator
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface Auth {
    /**
     * If {@code true}, the request will not be processed in the absence of a valid principal. If
     * {@code false}, {@code null} will be passed in as a principal. Defaults to {@code true}.
     */
    boolean required() default true;
}
