package com.yammer.dropwizard.jersey.caching;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheControl {
    boolean isPrivate() default false;

    boolean noCache() default false;

    boolean noStore() default false;

    boolean noTransform() default true;

    boolean mustRevalidate() default false;

    boolean proxyRevalidate() default false;
    
    int maxAge() default -1;

    TimeUnit maxAgeUnit() default TimeUnit.SECONDS;
    
    int sharedMaxAge() default -1;

    TimeUnit sharedMaxAgeUnit() default TimeUnit.SECONDS;
}
