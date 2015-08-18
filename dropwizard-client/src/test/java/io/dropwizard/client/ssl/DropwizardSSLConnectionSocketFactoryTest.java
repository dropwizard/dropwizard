package io.dropwizard.client.ssl;

import com.google.common.base.Optional;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.glassfish.jersey.client.ClientResponse;
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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class DropwizardSSLConnectionSocketFactoryTest {

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
    public static DropwizardAppRule<Configuration> TLS_APP_RULE = new DropwizardAppRule<Configuration>(TlsTestApplication.class,
            ResourceHelpers.resourceFilePath("yaml/ssl_connection_socket_factory_test.yml"),
            Optional.of("tls"),
            ConfigOverride.config("tls", "server.applicationConnectors[0].keyStorePath", ResourceHelpers.resourceFilePath("stores/server/keycert.p12")));

    @ClassRule
    public static DropwizardAppRule<Configuration> SELF_SIGNED_APP_RULE = new DropwizardAppRule<Configuration>(TlsTestApplication.class,
            ResourceHelpers.resourceFilePath("yaml/ssl_connection_socket_factory_test.yml"),
            Optional.of("selfSigned"),
            ConfigOverride.config("selfSigned", "server.applicationConnectors[0].keyStorePath", ResourceHelpers.resourceFilePath("stores/server/self_sign_keycert.p12")));

    @ClassRule
    public static DropwizardAppRule<Configuration> CLIENT_AUTH_APP_RULE = new DropwizardAppRule<Configuration>(TlsTestApplication.class,
            ResourceHelpers.resourceFilePath("yaml/ssl_connection_socket_factory_test.yml"),
            Optional.of("clientAuth"),
            ConfigOverride.config("clientAuth", "server.applicationConnectors[0].keyStorePath", ResourceHelpers.resourceFilePath("stores/server/keycert.p12")),
            ConfigOverride.config("clientAuth", "server.applicationConnectors[0].trustStorePath", ResourceHelpers.resourceFilePath("stores/server/ca_truststore.ts")),
            ConfigOverride.config("clientAuth", "server.applicationConnectors[0].wantClientAuth", "true"),
            ConfigOverride.config("clientAuth", "server.applicationConnectors[0].needClientAuth", "true"),
            ConfigOverride.config("clientAuth", "server.applicationConnectors[0].trustStorePassword", "password")
            );

    @ClassRule
    public static DropwizardAppRule<Configuration> BAD_HOST_APP_RULE = new DropwizardAppRule<Configuration>(TlsTestApplication.class,
            ResourceHelpers.resourceFilePath("yaml/ssl_connection_socket_factory_test.yml"),
            Optional.of("badHost"),
            ConfigOverride.config("badHost", "server.applicationConnectors[0].keyStorePath", ResourceHelpers.resourceFilePath("stores/server/bad_host_keycert.p12"))
    );

    @ClassRule
    public static DropwizardAppRule<Configuration> SSL3_APP_RULE = new DropwizardAppRule<Configuration>(TlsTestApplication.class,
            ResourceHelpers.resourceFilePath("yaml/ssl_connection_socket_factory_test.yml"),
            Optional.of("ssl3_app"),
            ConfigOverride.config("ssl3_app", "server.applicationConnectors[0].keyStorePath", ResourceHelpers.resourceFilePath("stores/server/keycert.p12")),
            ConfigOverride.config("ssl3_app", "server.applicationConnectors[0].supportedProtocols", "SSLv1,SSLv2,SSLv3")
    );
    @Test
    public void shouldReturn200IfServerCertInTruststore() throws Exception {
        final TlsConfiguration tlsConfiguration = new TlsConfiguration();
        tlsConfiguration.setTrustStorePath(new File(ResourceHelpers.resourceFilePath("stores/server/ca_truststore.ts")));
        tlsConfiguration.setTrustStorePassword("password");
        final JerseyClientConfiguration jerseyClientConfiguration = new JerseyClientConfiguration();
        jerseyClientConfiguration.setTlsConfiguration(tlsConfiguration);
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("tls_working_client");
        final Response response = client.target(String.format("https://localhost:%d", TLS_APP_RULE.getLocalPort())).request().get();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void shouldErrorIfServerCertNotFoundInTruststore() throws Exception {
        final TlsConfiguration tlsConfiguration = new TlsConfiguration();
        tlsConfiguration.setTrustStorePath(new File(ResourceHelpers.resourceFilePath("stores/server/other_cert_truststore.ts")));
        tlsConfiguration.setTrustStorePassword("password");
        final JerseyClientConfiguration jerseyClientConfiguration = new JerseyClientConfiguration();
        jerseyClientConfiguration.setTlsConfiguration(tlsConfiguration);
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("tls_broken_client");
        try {
            final Response response = client.target(String.format("https://localhost:%d", TLS_APP_RULE.getLocalPort())).request().get();
            fail("expected ProcessingException");
        } catch(ProcessingException e) {
           assertThat(e.getCause()).isInstanceOf(SSLHandshakeException.class);
        }
    }

    @Test
    public void shouldNotErrorIfServerCertSelfSignedAndSelfSignedCertsAllowed() throws Exception {
        final TlsConfiguration tlsConfiguration = new TlsConfiguration();
        tlsConfiguration.setTrustStorePath(new File(ResourceHelpers.resourceFilePath("stores/server/ca_truststore.ts")));
        tlsConfiguration.setTrustStorePassword("password");
        tlsConfiguration.setTrustSelfSignedCertificates(true);
        final JerseyClientConfiguration jerseyClientConfiguration = new JerseyClientConfiguration();
        jerseyClientConfiguration.setTlsConfiguration(tlsConfiguration);
        final Client client = new JerseyClientBuilder(SELF_SIGNED_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("self_sign_permitted");
        final Response response = client.target(String.format("https://localhost:%d", SELF_SIGNED_APP_RULE.getLocalPort())).request().get();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void shouldErrorIfServerCertSelfSignedAndSelfSignedCertsNotAllowed() throws Exception {
        final TlsConfiguration tlsConfiguration = new TlsConfiguration();
        tlsConfiguration.setTrustStorePath(new File(ResourceHelpers.resourceFilePath("stores/server/ca_truststore.ts")));
        tlsConfiguration.setTrustStorePassword("password");
        final JerseyClientConfiguration jerseyClientConfiguration = new JerseyClientConfiguration();
        jerseyClientConfiguration.setTlsConfiguration(tlsConfiguration);
        final Client client = new JerseyClientBuilder(SELF_SIGNED_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("self_sign_failure");
        try {
            final ClientResponse response = client.target(String.format("https://localhost:%d", SELF_SIGNED_APP_RULE.getLocalPort())).request().get(ClientResponse.class);
            fail("expected ProcessingException");
        } catch(ProcessingException e) {
            assertThat(e.getCause()).isInstanceOf(SSLHandshakeException.class);
        }
    }

    @Test
    public void shouldReturn200IfAbleToClientAuth() throws Exception {
        final TlsConfiguration tlsConfiguration = new TlsConfiguration();
        tlsConfiguration.setTrustStorePath(new File(ResourceHelpers.resourceFilePath("stores/server/ca_truststore.ts")));
        tlsConfiguration.setTrustStorePassword("password");
        tlsConfiguration.setKeyStorePath(new File(ResourceHelpers.resourceFilePath("stores/client/keycert.p12")));
        tlsConfiguration.setKeyStorePassword("password");
        tlsConfiguration.setKeyStoreType("PKCS12");
        final JerseyClientConfiguration jerseyClientConfiguration = new JerseyClientConfiguration();
        jerseyClientConfiguration.setTlsConfiguration(tlsConfiguration);
        final Client client = new JerseyClientBuilder(CLIENT_AUTH_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("client_auth_working");
        final Response response = client.target(String.format("https://localhost:%d", CLIENT_AUTH_APP_RULE.getLocalPort())).request().get();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void shouldErrorIfClientAuthFails() throws Exception {
        final TlsConfiguration tlsConfiguration = new TlsConfiguration();
        tlsConfiguration.setTrustStorePath(new File(ResourceHelpers.resourceFilePath("stores/server/ca_truststore.ts")));
        tlsConfiguration.setTrustStorePassword("password");
        tlsConfiguration.setKeyStorePath(new File(ResourceHelpers.resourceFilePath("stores/server/self_sign_keycert.p12")));
        tlsConfiguration.setKeyStorePassword("password");
        tlsConfiguration.setKeyStoreType("PKCS12");
        final JerseyClientConfiguration jerseyClientConfiguration = new JerseyClientConfiguration();
        jerseyClientConfiguration.setTlsConfiguration(tlsConfiguration);
        final Client client = new JerseyClientBuilder(CLIENT_AUTH_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("client_auth_broken");
        try {
            final Response response = client.target(String.format("https://localhost:%d", CLIENT_AUTH_APP_RULE.getLocalPort())).request().get();
            fail("expected ProcessingException");
        } catch(ProcessingException e) {
            assertThat(e.getCause()).isInstanceOf(SocketException.class);
        }
    }

    @Test
    public void shouldErrorIfHostnameVerificationOnAndServerHostnameDoesntMatch() throws Exception {
        final TlsConfiguration tlsConfiguration = new TlsConfiguration();
        tlsConfiguration.setTrustStorePath(new File(ResourceHelpers.resourceFilePath("stores/server/ca_truststore.ts")));
        tlsConfiguration.setTrustStorePassword("password");
        final JerseyClientConfiguration jerseyClientConfiguration = new JerseyClientConfiguration();
        jerseyClientConfiguration.setTlsConfiguration(tlsConfiguration);
        final Client client = new JerseyClientBuilder(BAD_HOST_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("bad_host_broken");
        try {
            final Response response = client.target(String.format("https://localhost:%d", BAD_HOST_APP_RULE.getLocalPort())).request().get();
            fail("Expected ProcessingException");
        } catch (ProcessingException e) {
            assertThat(e.getCause()).isExactlyInstanceOf(SSLPeerUnverifiedException.class);
            assertThat(e.getCause().getMessage()).isEqualTo("Host name 'localhost' does not match the certificate subject provided by the peer (O=server, CN=badhost)");
        }
    }

    @Test
    public void shouldBeOkIfHostnameVerificationOffAndServerHostnameDoesntMatch() throws Exception {
        final TlsConfiguration tlsConfiguration = new TlsConfiguration();
        tlsConfiguration.setTrustStorePath(new File(ResourceHelpers.resourceFilePath("stores/server/ca_truststore.ts")));
        tlsConfiguration.setVerifyHostname(false);
        tlsConfiguration.setTrustStorePassword("password");
        final JerseyClientConfiguration jerseyClientConfiguration = new JerseyClientConfiguration();
        jerseyClientConfiguration.setTlsConfiguration(tlsConfiguration);
        final Client client = new JerseyClientBuilder(BAD_HOST_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("bad_host_working");
        final Response response = client.target(String.format("https://localhost:%d", BAD_HOST_APP_RULE.getLocalPort())).request().get();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void shouldRejectNonSupportedProtocols() throws Exception {
        final TlsConfiguration tlsConfiguration = new TlsConfiguration();
        tlsConfiguration.setTrustStorePath(new File(ResourceHelpers.resourceFilePath("stores/server/ca_truststore.ts")));
        tlsConfiguration.setTrustStorePassword("password");
        tlsConfiguration.setSupportedProtocols(asList("TLSv1.2"));
        final JerseyClientConfiguration jerseyClientConfiguration = new JerseyClientConfiguration();
        jerseyClientConfiguration.setTlsConfiguration(tlsConfiguration);
        final Client client = new JerseyClientBuilder(SSL3_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("reject_non_supported");
        try {
            final Response response = client.target(String.format("https://localhost:%d", SSL3_APP_RULE.getLocalPort())).request().get();
            fail("expected ProcessingException");
        } catch (ProcessingException e) {
            assertThat(e.getCause()).isInstanceOf(SSLHandshakeException.class);
            assertThat(e.getCause().getMessage()).isEqualTo("Server chose SSLv3, but that protocol version is not enabled or not supported by the client.");
        }
    }
}