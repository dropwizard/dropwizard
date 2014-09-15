package io.dropwizard.jersey.caching;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * An annotation which adds a constant {@code Cache-Control} header to the response produced by
 * the annotated method.
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheControl {
    /**
     * If set, adds a {@code Cache-Control} header to the response which indicates the response is
     * immutable and should be kept in cache for as long as possible. (Technically, this corresponds
     * to a {@code max-age} of one year.
     *
     * @see #maxAge()
     * @return {@code true} if the response should be considered immutable and cached indefinitely
     */
    boolean immutable() default false;

    /**
     * Controls the {@code private} setting of the {@code Cache-Control} header.
     *
     * <p>From the HTTPbis spec:</p>
     * <blockquote>
     *     The private response directive indicates that the response message is intended for a
     *     single user and MUST NOT be stored by a shared cache.  A private cache MAY store the
     *     response.
     *
     *     If the private response directive specifies one or more field-names, this requirement is
     *     limited to the field-values associated with the listed response header fields.  That is,
     *     a shared cache MUST NOT store the specified field-names(s), whereas it MAY store the
     *     remainder of the response message.
     *
     *     Note: This usage of the word "private" only controls where the response can be stored; it
     *     cannot ensure the privacy of the message content.  Also, private response directives with
     *     field-names are often handled by implementations as if an unqualified private directive
     *     was received; i.e., the special handling for the qualified form is not widely
     *     implemented.
     * </blockquote>
     *
     * @return {@code true} if the response must not be stored by a shared cache
     */
    boolean isPrivate() default false;

    /**
     * Controls the {@code no-cache} setting of the {@code Cache-Control} header.
     *
     * <p>From the HTTPbis spec:</p>
     * <blockquote>
     *     The no-cache response directive indicates that the response MUST NOT be used to satisfy a
     *     subsequent request without successful validation on the origin server.  This allows an
     *     origin server to prevent a cache from using it to satisfy a request without contacting
     *     it, even by caches that have been configured to return stale responses.
     *
     *     If the no-cache response directive specifies one or more field-names, then a cache MAY
     *     use the response to satisfy a subsequent request, subject to any other restrictions on
     *     caching.  However, any header fields in the response that have the field-name(s) listed
     *     MUST NOT be sent in the response to a subsequent request without successful revalidation
     *     with the origin server.  This allows an origin server to prevent the re-use of certain
     *     header fields in a response, while still allowing caching of the rest of the response.
     *
     *     Note: Most HTTP/1.0 caches will not recognize or obey this directive.  Also, no-cache
     *     response directives with field-names are often handled by implementations as if an
     *     unqualified no-cache directive was received; i.e., the special handling for the qualified
     *     form is not widely implemented.
     * </blockquote>
     *
     * @return {@code true} if the response must not be cached
     */
    boolean noCache() default false;

    /**
     * Controls the {@code no-store} setting of the {@code Cache-Control} header.
     *
     * <p>From the HTTPbis spec:</p>
     * <blockquote>
     *     The no-store response directive indicates that a cache MUST NOT store any part of either
     *     the immediate request or response.  This directive applies to both private and shared
     *     caches.  "MUST NOT store" in this context means that the cache MUST NOT intentionally
     *     store the information in non-volatile storage, and MUST make a best-effort attempt to
     *     remove the information from volatile storage as promptly as possible after forwarding it.
     *
     *     This directive is NOT a reliable or sufficient mechanism for ensuring privacy.  In
     *     particular, malicious or compromised caches might not recognize or obey this directive,
     *     and communications networks might be vulnerable to eavesdropping.
     * </blockquote>
     *
     * @return {@code true} if the response must not be stored
     */
    boolean noStore() default false;

    /**
     * Controls the {@code no-transform} setting of the {@code Cache-Control} header.
     *
     * <p>From the HTTPbis spec:</p>
     * <blockquote>
     *     The no-transform response directive indicates that an intermediary (regardless of whether
     *     it implements a cache) MUST NOT change the Content-Encoding, Content-Range or
     *     Content-Type response header fields, nor the response representation.
     * </blockquote>
     *
     * @return {@code true} if the response must not be transformed by intermediaries
     */
    boolean noTransform() default true;

    /**
     * Controls the {@code must-revalidate} setting of the {@code Cache-Control} header.
     *
     * <p>From the HTTPbis spec:</p>
     * <blockquote>
     *     The must-revalidate response directive indicates that once it has become stale, a cache
     *     MUST NOT use the response to satisfy subsequent requests without successful validation on
     *     the origin server.
     *
     *     The must-revalidate directive is necessary to support reliable operation for certain
     *     protocol features.  In all circumstances a cache MUST obey the must-revalidate directive;
     *     in particular, if a cache cannot reach the origin server for any reason, it MUST generate
     *     a 504 (Gateway Timeout) response.
     *
     *     The must-revalidate directive ought to be used by servers if and only if failure to
     *     validate a request on the representation could result in incorrect operation, such as a
     *     silently unexecuted financial transaction.
     * </blockquote>
     *
     * @return {@code true} if caches must revalidate the content when it becomes stale
     */
    boolean mustRevalidate() default false;

    /**
     * Controls the {@code proxy-revalidate} setting of the {@code Cache-Control} header.
     *
     * <p>From the HTTPbis spec:</p>
     * <blockquote>
     *     The proxy-revalidate response directive has the same meaning as the must-revalidate
     *     response directive, except that it does not apply to private caches.
     * </blockquote>
     *
     * @return {@code true} if only proxies must revalidate the content when it becomes stale
     */
    boolean proxyRevalidate() default false;

    /**
     * Controls the {@code max-age} setting of the {@code Cache-Control} header. The unit of this
     * amount is determined by {@link #maxAgeUnit()}.
     *
     * <p>From the HTTPbis spec:</p>
     * <blockquote>
     *     The max-age response directive indicates that the response is to be considered stale
     *     after its age is greater than the specified number of seconds.
     * </blockquote>
     *
     * @see #maxAgeUnit()
     * @return the number of {@link #maxAgeUnit()}s for which the response should be considered
     *         fresh
     */
    int maxAge() default -1;

    /**
     * The time unit of {@link #maxAge()}.
     *
     * @return the time unit of {@link #maxAge()}
     */
    TimeUnit maxAgeUnit() default TimeUnit.SECONDS;

    /**
     * Controls the {@code s-max-age} setting of the {@code Cache-Control} header. The unit of this
     * amount is controlled by {@link #sharedMaxAgeUnit()}.
     *
     * <p>From the HTTPbis spec:</p>
     * <blockquote>
     *     The s-maxage response directive indicates that, in shared caches, the maximum age
     *     specified by this directive overrides the maximum age specified by either the max-age
     *     directive or the Expires header field.  The s-maxage directive also implies the semantics
     *     of the proxy-revalidate response directive.
     * </blockquote>
     *
     * @return the number of {@link #sharedMaxAgeUnit()}s for which the response should be
     *         considered fresh
     */
    int sharedMaxAge() default -1;

    /**
     * The time unit of {@link #sharedMaxAge()}.
     *
     * @return the time unit of {@link #sharedMaxAge()}
     */
    TimeUnit sharedMaxAgeUnit() default TimeUnit.SECONDS;
}
