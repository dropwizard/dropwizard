package io.dropwizard.jersey.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * A JAX-RS filter that generates a Content Security Policy (CSP) nonce per request,
 * stores it in the request context properties, and injects it into configured
 * {@code Content-Security-Policy} and/or {@code Content-Security-Policy-Report-Only} headers.
 * <p>
 * The placeholder {@code $NONCE} in the configured policy templates is replaced
 * with the generated nonce value.
 * </p>
 */
@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class CspFilter implements ContainerRequestFilter, ContainerResponseFilter {

    /**
     * The {@link jakarta.ws.rs.container.ContainerRequestContext} property key under which the
     * per-request CSP nonce is stored. Use this constant in any code that reads or writes the
     * nonce via {@code requestContext.getProperty} / {@code requestContext.setProperty} to
     * guarantee a consistent key across all modules.
     */
    public static final String NONCE_PROPERTY_KEY = "csp-nonce";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Nullable
    private final String policyTemplate;

    @Nullable
    private final String reportOnlyPolicyTemplate;

    public CspFilter(@Nullable String policyTemplate, @Nullable String reportOnlyPolicyTemplate) {
        this.policyTemplate = policyTemplate;
        this.reportOnlyPolicyTemplate = reportOnlyPolicyTemplate;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        final byte[] nonceBytes = new byte[32];
        SECURE_RANDOM.nextBytes(nonceBytes);
        final String nonce = Base64.getUrlEncoder().withoutPadding().encodeToString(nonceBytes);
        requestContext.setProperty(NONCE_PROPERTY_KEY, nonce);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        final String nonce = (String) requestContext.getProperty(NONCE_PROPERTY_KEY);
        if (nonce != null) {
            if (policyTemplate != null) {
                final String policy = policyTemplate.replace("$NONCE", nonce);
                responseContext.getHeaders().putSingle("Content-Security-Policy", policy);
            }
            if (reportOnlyPolicyTemplate != null) {
                final String reportOnlyPolicy = reportOnlyPolicyTemplate.replace("$NONCE", nonce);
                responseContext.getHeaders().putSingle("Content-Security-Policy-Report-Only", reportOnlyPolicy);
            }
        }
    }
}
