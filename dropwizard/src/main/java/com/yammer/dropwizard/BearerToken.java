package com.yammer.dropwizard;

import java.lang.annotation.*;

/**
 * A method parameter of type {@code Option[String]} will be populated with the
 * OAuth2 Bearer Token, if one is provided by the client.
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BearerToken {
    String value() default "Bearer";
}
