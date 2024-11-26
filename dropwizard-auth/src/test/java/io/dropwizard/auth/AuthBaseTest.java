package io.dropwizard.auth;

import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.logging.common.BootstrapLogging;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.HttpHeaders;
import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public abstract class AuthBaseTest<T extends DropwizardResourceConfig> extends JerseyTest {
    protected static final String ADMIN_ROLE = "ADMIN";
    protected static final String ADMIN_USER = "good-guy";
    protected static final String ORDINARY_USER = "ordinary-guy";
    protected static final String BADGUY_USER = "bad-guy";
    protected static final String CUSTOM_PREFIX = "Custom";
    protected static final String BEARER_PREFIX = "Bearer";
    protected static final String BASIC_PREFIX = "Basic";
    protected static final String ORDINARY_USER_ENCODED_TOKEN = "b3JkaW5hcnktZ3V5OnNlY3JldA==";
    protected static final String GOOD_USER_ENCODED_TOKEN = "Z29vZC1ndXk6c2VjcmV0";
    protected static final String BAD_USER_ENCODED_TOKEN = "YmFkLWd1eTpzZWNyZXQ=";

    static {
        BootstrapLogging.bootstrap();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory()
        throws TestContainerException {
        return new GrizzlyWebTestContainerFactory();
    }

    protected abstract DropwizardResourceConfig getDropwizardResourceConfig();
    protected abstract Class<T> getDropwizardResourceConfigClass();
    protected abstract String getPrefix();
    protected abstract String getOrdinaryGuyValidToken();
    protected abstract String getGoodGuyValidToken();
    protected abstract String getBadGuyToken();

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected DeploymentContext configureDeployment() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        return ServletDeploymentContext.builder(getDropwizardResourceConfig())
            .initParam(ServletProperties.JAXRS_APPLICATION_CLASS, getDropwizardResourceConfigClass().getName())
            .build();
    }

    @Test
    void respondsToMissingCredentialsWith401() {
        Invocation.Builder request = target("/test/admin").request();
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> request.get(String.class))
            .extracting(WebApplicationException::getResponse)
            .satisfies(response -> assertThat(response.getStatus()).isEqualTo(401))
            .satisfies(response -> assertThat(response.getHeaders().get(HttpHeaders.WWW_AUTHENTICATE))
                .containsOnly(getPrefix() + " realm=\"realm\""));
    }

    @Test
    void resourceWithoutAuth200() {
        assertThat(target("/test/noauth").request()
            .get(String.class))
            .isEqualTo("hello");
    }

    @Test
    void resourceWithAuthenticationWithoutAuthorizationWithCorrectCredentials200() {
        assertThat(target("/test/profile").request()
            .header(HttpHeaders.AUTHORIZATION, getPrefix() + " " + getOrdinaryGuyValidToken())
            .get(String.class))
            .isEqualTo("'%s' has user privileges", ORDINARY_USER);
    }

    @Test
    void resourceWithAuthenticationWithoutAuthorizationNoCredentials401() {
        Invocation.Builder request = target("/test/profile").request();
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> request.get(String.class))
            .extracting(WebApplicationException::getResponse)
            .satisfies(response -> assertThat(response.getStatus()).isEqualTo(401))
            .satisfies(response -> assertThat(response.getHeaders().get(HttpHeaders.WWW_AUTHENTICATE))
                .containsOnly(getPrefix() + " realm=\"realm\""));
    }

    @Test
    void resourceWithValidOptionalAuthentication200() {
        assertThat(target("/test/optional").request()
            .header(HttpHeaders.AUTHORIZATION, getPrefix() + " " + getOrdinaryGuyValidToken())
            .get(String.class))
            .isEqualTo("principal was present");
    }

    @Test
    void resourceWithInvalidOptionalAuthentication200() {
        assertThat(target("/test/optional").request()
            .header(HttpHeaders.AUTHORIZATION, getPrefix() + " " + getBadGuyToken())
            .get(String.class))
            .isEqualTo("principal was not present");
    }

    @Test
    void resourceWithoutOptionalAuthentication200() {
        assertThat(target("/test/optional").request()
            .get(String.class))
            .isEqualTo("principal was not present");
    }

    @Test
    void resourceWithAuthorizationPrincipalIsNotAuthorized403() {
        Invocation.Builder request = target("/test/admin").request()
            .header(HttpHeaders.AUTHORIZATION, getPrefix() + " " + getOrdinaryGuyValidToken());
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> request.get(String.class))
            .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(403));
    }


    @Test
    void resourceWithDenyAllAndNoAuth401() {
        Invocation.Builder request = target("/test/denied").request();
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> request.get(String.class))
            .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(401));
    }

    @Test
    void resourceWithDenyAllAndAuth403() {
        Invocation.Builder request = target("/test/denied").request()
            .header(HttpHeaders.AUTHORIZATION, getPrefix() + " " + getGoodGuyValidToken());
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> request.get(String.class))
            .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(403));
    }

    @Test
    void transformsCredentialsToPrincipals() {
        assertThat(target("/test/admin").request()
            .header(HttpHeaders.AUTHORIZATION, getPrefix() + " " + getGoodGuyValidToken())
            .get(String.class))
            .isEqualTo("'%s' has admin privileges", ADMIN_USER);
    }

    @Test
    void transformsCredentialsToPrincipalsWhenAuthAnnotationExistsWithoutMethodAnnotation() {
        assertThat(target("/test/implicit-permitall").request()
            .header(HttpHeaders.AUTHORIZATION, getPrefix() + " " + getGoodGuyValidToken())
            .get(String.class))
            .isEqualTo("'%s' has user privileges", ADMIN_USER);
    }

    @Test
    void respondsToNonBasicCredentialsWith401() {
        Invocation.Builder request = target("/test/admin").request()
            .header(HttpHeaders.AUTHORIZATION, "Derp irrelevant");
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> request.get(String.class))
            .extracting(WebApplicationException::getResponse)
            .satisfies(response -> assertThat(response.getStatus()).isEqualTo(401))
            .satisfies(response -> assertThat(response.getHeaders().get(HttpHeaders.WWW_AUTHENTICATE))
                .containsOnly(getPrefix() + " realm=\"realm\""));
    }

    @Test
    void respondsToExceptionsWith500() {
        Invocation.Builder request = target("/test/admin").request()
            .header(HttpHeaders.AUTHORIZATION, getPrefix() + " " + getBadGuyToken());
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> request.get(String.class))
            .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(500));
    }
}
