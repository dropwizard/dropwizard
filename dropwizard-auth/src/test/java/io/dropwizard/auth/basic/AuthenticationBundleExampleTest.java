package io.dropwizard.auth.basic;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.logging.LoggingFactory;
import org.eclipse.jetty.server.UserIdentity;
import org.junit.Test;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.security.auth.Subject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.security.Principal;
import java.util.Arrays;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

public class AuthenticationBundleExampleTest extends JerseyTest {
    private static final String AUTHORIZATION_ADMIN = "Basic YWRtaW4tZ3V5OnNlY3JldA=="; //username=admin-guy
    private static final String AUTHORIZATION_USER = "Basic Z29vZC1ndXk6c2VjcmV0"; //username=good-guy
    private static final String AUTHORIZATION_UNKNOWN = "Basic dW5rbm93bi1ndXk6c2VjcmV0"; //username=unknown-guy
    private static final String AUTHORIZATION_MALFORMED = "Basic foobar";
    private static final String AUTHORIZATION_INTERNAL_ERROR = "Basic dXNlcm5hbWU6cGFzc3dvcmQ="; //username=username

    static {
        LoggingFactory.bootstrap();
    }

    @Path("/test/")
    @RolesAllowed("admin")
    @Produces(MediaType.TEXT_PLAIN)
    public static class ExampleResource {
        @GET
        @Path("/restrictedAccess/")
        public String restrictedAccess() {
            return "Restricted Access Granted";
        }

        @GET
        @PermitAll
        @Path("/publicAccess/")
        public String publicAccess() {
            return "Public Access Granted";
        }
    }

    private static UserIdentity create(final String principalName, final String... roles) {
        return new UserIdentity() {
            @Override
            public Subject getSubject() {
                return new Subject();
            }

            @Override
            public Principal getUserPrincipal() {
                return new Principal() {
                    @Override
                    public String getName() {
                        return principalName;
                    }
                };
            }

            @Override
            public boolean isUserInRole(String role, Scope scope) {
                return Arrays.asList(roles).contains(role);
            }
        };
    }

    @Override
    protected AppDescriptor configure() {
        final Authenticator<BasicCredentials, UserIdentity> authenticator = new Authenticator<BasicCredentials, UserIdentity>() {
            @Override
            public Optional<UserIdentity> authenticate(BasicCredentials credentials) throws AuthenticationException {
                if ("admin-guy".equals(credentials.getUsername()) &&
                        "secret".equals(credentials.getPassword())) {
                    return Optional.of(create("admin-guy", "admin"));
                }
                if ("good-guy".equals(credentials.getUsername()) &&
                        "secret".equals(credentials.getPassword())) {
                    return Optional.of(create("good-guy"));
                }
                if ("username".equals(credentials.getUsername())) {
                    throw new AuthenticationException("CRAP");
                }
                return Optional.absent();
            }
        };
        final DropwizardResourceConfig config = DropwizardResourceConfig.forTesting(new MetricRegistry());
        AuthenticationBundle authenticationBundle = new AuthenticationBundle(authenticator, true, "realm");
        authenticationBundle.setupResourceConfig(config);

        config.getSingletons().add(new ExampleResource());
        return new LowLevelAppDescriptor.Builder(config).build();
    }

    @Test
    public void authHeader_accessGranted_shouldReturnHttpStatus200() {
        final String response = client().resource("/test/publicAccess/").header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_USER).get(String.class);
        assertThat(response).isEqualTo("Public Access Granted");
    }

    @Test
    public void authHeader_restrictedAccessGranted_shouldReturnHttpStatus200() {
        final String response = client().resource("/test/restrictedAccess/").header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_ADMIN).get(String.class);
        assertThat(response).isEqualTo("Restricted Access Granted");
    }

    @Test
    public void authHeader_malformedBase64UsernameAndPassword_shouldReturnHttpStatus400() {
        try {
            client().resource("/test/publicAccess/").header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_MALFORMED).get(String.class);
            failBecauseExceptionWasNotThrown(UniformInterfaceException.class);
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(400);
        }
    }

    @Test
    public void noAuthHeader_shouldReturnHttpStatus401() {
        try {
            client().resource("/test/publicAccess/").get(String.class);
            failBecauseExceptionWasNotThrown(UniformInterfaceException.class);
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(401);

            assertThat(e.getResponse().getHeaders().get(HttpHeaders.WWW_AUTHENTICATE))
                    .containsOnly("Basic realm=\"realm\"");

        }
    }

    @Test
    public void authHeader_unknownUser_shouldReturnHttpStatus403() {
        try {
            client().resource("/test/publicAccess/").header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_UNKNOWN).get(String.class);
            failBecauseExceptionWasNotThrown(UniformInterfaceException.class);
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(403);
        }
    }

    @Test
    public void authHeader_accessDenied_authHeader_shouldReturnHttpStatus403() {
        try {
            client().resource("/test/restrictedAccess/").header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_USER).get(String.class);
            failBecauseExceptionWasNotThrown(UniformInterfaceException.class);
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(403);
        }
    }

    @Test
    public void authHeader_internalError_shouldReturnHttpStatus500() {
        try {
            client().resource("/test/publicAccess/").header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_INTERNAL_ERROR).get(String.class);
            failBecauseExceptionWasNotThrown(UniformInterfaceException.class);
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(500);
        }
    }
}
