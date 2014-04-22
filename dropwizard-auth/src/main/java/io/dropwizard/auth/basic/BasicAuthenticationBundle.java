package io.dropwizard.auth.basic;

import com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory;
import com.sun.jersey.api.core.ResourceConfig;
import io.dropwizard.Bundle;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.UserIdentity;

/**
 * BasicAuthenticationBundle let you use {@link javax.annotation.security.RolesAllowed},
 * {@link javax.annotation.security.PermitAll} and {@link javax.annotation.security.DenyAll} in your resources
 * by enabling Jersey's {@link com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory} and register
 * {@link BasicAuthenticationFilter} to Jersey's container request filter chain.
 * <p/>
 * In order to use this bundle you must provide an implementation of {@link Authenticator}<{@link BasicCredentials}, {@link UserIdentity}>}.
 * <p/>
 * Example of a simple implementation:
 * <pre>
 * {@code
 * import com.google.common.base.Optional;
 * import io.dropwizard.auth.basic.BasicCredentials;
 * import org.eclipse.jetty.server.UserIdentity;
 *
 * import javax.security.auth.Subject;
 * import java.security.Principal;
 *
 * public class SimpleAuthenticator implements Authenticator<BasicCredentials, UserIdentity>
 *     {
 *     @Override
 *     public Optional<UserIdentity> authenticate(BasicCredentials credentials) throws AuthenticationException
 *     {
 *         UserIdentity userIdentity = null;
 *         if ("admin".equals(credentials.getUsername()) && "secret".equals(credentials.getPassword())) {
 *             userIdentity = new UserIdentity() {
 *                 @Override
 *                 public Subject getSubject() {
 *                     return new Subject();
 *                 }
 *
 *                 @Override
 *                 public Principal getUserPrincipal() {
 *                     return new Principal() {
 *                         @Override
 *                         public String getName() {
 *                             return "admin";
 *                         }
 *                     };
 *                 }
 *
 *                 @Override
 *                 public boolean isUserInRole(String role, Scope scope) {
 *                     if ("admin-role".equals(role)) {
 *                         return true;
 *                     }
 *                     return false;
 *                 }
 *             };
 *         }
 *         return Optional.of(userIdentity);
 *     }
 * }
 * }
 * </pre>
 */
public class BasicAuthenticationBundle implements Bundle {
    private final boolean requireAuthorization;
    private final String realm;
    private final Authenticator<BasicCredentials, UserIdentity> authenticator;

    /**
     * @param authenticator        - Implementation that will authenticate user and list roles that user are authorized to use.
     * @param requireAuthorization - if set to true, all request without HTTP header 'Authorization' will return HTTP Status 401/UNAUTHORIZED.
     * @param realm                - Name to be displayed then browser prompts for username and password.
     */
    public BasicAuthenticationBundle(final Authenticator<BasicCredentials, UserIdentity> authenticator, final boolean requireAuthorization, final String realm) {
        this.authenticator = authenticator;
        this.requireAuthorization = requireAuthorization;
        this.realm = realm;
    }

    @Override
    public void initialize(final Bootstrap<?> bootstrap) {
    }

    @Override
    public void run(final Environment environment) {
        final JerseyEnvironment jersey = environment.jersey();
        final ResourceConfig resourceConfig = jersey.getResourceConfig();
        setupResourceConfig(resourceConfig);
    }

    public void setupResourceConfig(ResourceConfig resourceConfig) {
        resourceConfig.getResourceFilterFactories().add(RolesAllowedResourceFilterFactory.class.getName());
        resourceConfig.getContainerRequestFilters().add(new BasicAuthenticationFilter(authenticator, requireAuthorization, realm));
    }
}
