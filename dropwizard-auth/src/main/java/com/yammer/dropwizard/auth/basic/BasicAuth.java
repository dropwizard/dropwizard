package com.yammer.dropwizard.auth.basic;

import java.lang.annotation.*;

@Target({ ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BasicAuth {

}
