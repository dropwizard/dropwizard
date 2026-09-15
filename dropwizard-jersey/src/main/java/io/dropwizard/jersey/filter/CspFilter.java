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
        final byte[] nonceBytes = new byte[16];
        SECURE_RANDOM.nextBytes(nonceBytes);
        final String nonce = Base64.getEncoder().encodeToString(nonceBytes);
        requestContext.setProperty("csp-nonce", nonce);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        final String nonce = (String) requestContext.getProperty("csp-nonce");
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
