package io.dropwizard.client.ssl;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.util.Duration;
import org.glassfish.jersey.client.ClientResponse;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.io.File;
import java.net.SocketException;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class DropwizardSSLConnectionSocketFactoryTest {
    private TlsConfiguration tlsConfiguration;
    private JerseyClientConfiguration jerseyClientConfiguration;

    @Path("/")
    public static class TestResource {
        @GET
        public Response respondOk() {
            return Response.ok().build();
        }
    }

    public static class TlsTestApplication extends Application<Configuration> {
        public static void main(String[] args) throws Exception {
            new TlsTestApplication().run(args);
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
            environment.jersey().register(TestResource.class);
        }
    }

    @ClassRule
    public static DropwizardAppRule<Configuration> TLS_APP_RULE = new DropwizardAppRule<>(TlsTestApplication.class,
            ResourceHelpers.resourceFilePath("yaml/ssl_connection_socket_factory_test.yml"),
            Optional.of("tls"),
            ConfigOverride.config("tls", "server.applicationConnectors[0].keyStorePath", ResourceHelpers.resourceFilePath("stores/server/keycert.p12")),
            ConfigOverride.config("tls", "server.applicationConnectors[1].keyStorePath", ResourceHelpers.resourceFilePath("stores/server/self_sign_keycert.p12")),
            ConfigOverride.config("tls", "server.applicationConnectors[2].keyStorePath", ResourceHelpers.resourceFilePath("stores/server/keycert.p12")),
            ConfigOverride.config("tls", "server.applicationConnectors[2].trustStorePath", ResourceHelpers.resourceFilePath("stores/server/ca_truststore.ts")),
            ConfigOverride.config("tls", "server.applicationConnectors[2].wantClientAuth", "true"),
            ConfigOverride.config("tls", "server.applicationConnectors[2].needClientAuth", "true"),
            ConfigOverride.config("tls", "server.applicationConnectors[2].validatePeers", "false"),
            ConfigOverride.config("tls", "server.applicationConnectors[2].trustStorePassword", "password"),
            ConfigOverride.config("tls", "server.applicationConnectors[3].keyStorePath", ResourceHelpers.resourceFilePath("stores/server/bad_host_keycert.p12")),
            ConfigOverride.config("tls", "server.applicationConnectors[4].keyStorePath", ResourceHelpers.resourceFilePath("stores/server/keycert.p12")),
            ConfigOverride.config("tls", "server.applicationConnectors[4].supportedProtocols", "SSLv1,SSLv2,SSLv3"));

    @Before
    public void setUp() throws Exception {
        tlsConfiguration = new TlsConfiguration();
        tlsConfiguration.setTrustStorePath(new File(ResourceHelpers.resourceFilePath("stores/server/ca_truststore.ts")));
        tlsConfiguration.setTrustStorePassword("password");
        jerseyClientConfiguration = new JerseyClientConfiguration();
        jerseyClientConfiguration.setTlsConfiguration(tlsConfiguration);
        jerseyClientConfiguration.setConnectionTimeout(Duration.milliseconds(2000));
        jerseyClientConfiguration.setTimeout(Duration.milliseconds(5000));
    }

    @Test
    public void shouldReturn200IfServerCertInTruststore() throws Exception {
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("tls_working_client");
        final Response response = client.target(String.format("https://localhost:%d", TLS_APP_RULE.getLocalPort())).request().get();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void shouldErrorIfServerCertNotFoundInTruststore() throws Exception {
        tlsConfiguration.setTrustStorePath(new File(ResourceHelpers.resourceFilePath("stores/server/other_cert_truststore.ts")));
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("tls_broken_client");
        try {
            client.target(String.format("https://localhost:%d", TLS_APP_RULE.getLocalPort())).request().get();
            fail("expected ProcessingException");
        } catch(ProcessingException e) {
           assertThat(e.getCause()).isInstanceOf(SSLHandshakeException.class);
        }
    }

    @Test
    public void shouldNotErrorIfServerCertSelfSignedAndSelfSignedCertsAllowed() throws Exception {
        tlsConfiguration.setTrustSelfSignedCertificates(true);
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("self_sign_permitted");
        final Response response = client.target(String.format("https://localhost:%d", TLS_APP_RULE.getTestSupport().getPort(1))).request().get();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void shouldErrorIfServerCertSelfSignedAndSelfSignedCertsNotAllowed() throws Exception {
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("self_sign_failure");
        try {
            client.target(String.format("https://localhost:%d", TLS_APP_RULE.getPort(1))).request().get(ClientResponse.class);
            fail("expected ProcessingException");
        } catch(ProcessingException e) {
            assertThat(e.getCause()).isInstanceOf(SSLHandshakeException.class);
        }
    }

    @Test
    public void shouldReturn200IfAbleToClientAuth() throws Exception {
        tlsConfiguration.setKeyStorePath(new File(ResourceHelpers.resourceFilePath("stores/client/keycert.p12")));
        tlsConfiguration.setKeyStorePassword("password");
        tlsConfiguration.setKeyStoreType("PKCS12");
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("client_auth_working");
        final Response response = client.target(String.format("https://localhost:%d", TLS_APP_RULE.getPort(2))).request().get();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void shouldErrorIfClientAuthFails() throws Exception {
        tlsConfiguration.setKeyStorePath(new File(ResourceHelpers.resourceFilePath("stores/server/self_sign_keycert.p12")));
        tlsConfiguration.setKeyStorePassword("password");
        tlsConfiguration.setKeyStoreType("PKCS12");
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("client_auth_broken");
        try {
            client.target(String.format("https://localhost:%d", TLS_APP_RULE.getPort(2))).request().get();
            fail("expected ProcessingException");
        } catch(ProcessingException e) {
            assertThat(e.getCause()).isInstanceOfAny(SocketException.class, SSLHandshakeException.class);
        }
    }

    @Test
    public void shouldErrorIfHostnameVerificationOnAndServerHostnameDoesntMatch() throws Exception {
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("bad_host_broken");
        try {
            client.target(String.format("https://localhost:%d", TLS_APP_RULE.getPort(3))).request().get();
            fail("Expected ProcessingException");
        } catch (ProcessingException e) {
            assertThat(e.getCause()).isExactlyInstanceOf(SSLPeerUnverifiedException.class);
            assertThat(e.getCause().getMessage()).isEqualTo("Host name 'localhost' does not match the certificate subject provided by the peer (O=server, CN=badhost)");
        }
    }

    @Test
    public void shouldBeOkIfHostnameVerificationOffAndServerHostnameDoesntMatch() throws Exception {
        tlsConfiguration.setVerifyHostname(false);
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("bad_host_working");
        final Response response = client.target(String.format("https://localhost:%d", TLS_APP_RULE.getPort(3))).request().get();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void shouldRejectNonSupportedProtocols() throws Exception {
        tlsConfiguration.setSupportedProtocols(asList("TLSv1.2"));
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("reject_non_supported");
        try {
            client.target(String.format("https://localhost:%d", TLS_APP_RULE.getPort(4))).request().get();
            fail("expected ProcessingException");
        } catch (ProcessingException e) {
            assertThat(e.getCause()).isInstanceOfAny(SocketException.class, SSLHandshakeException.class);
        }
    }
}
