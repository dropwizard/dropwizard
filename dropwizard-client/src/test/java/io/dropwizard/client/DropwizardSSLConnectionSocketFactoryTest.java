package io.dropwizard.client;

import io.dropwizard.client.ssl.TlsConfiguration;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.util.Duration;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.Response;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.glassfish.jersey.client.ClientResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.security.Security;
import java.util.Collections;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static io.dropwizard.util.Resources.getResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME;

@ExtendWith(DropwizardExtensionsSupport.class)
class DropwizardSSLConnectionSocketFactoryTest {
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
        public void run(Configuration configuration, Environment environment) {
            environment.jersey().register(TestResource.class);
        }
    }

    static {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    @AfterAll
    static void classTearDown() {
        Security.removeProvider(PROVIDER_NAME);
    }

    private static final DropwizardAppExtension<Configuration> TLS_APP_RULE = new DropwizardAppExtension<>(TlsTestApplication.class,
        "yaml/ssl_connection_socket_factory_test.yml",
        new ResourceConfigurationSourceProvider(),
        "tls",
        config("tls", "server.applicationConnectors[0].keyStorePath", resourceFilePath("stores/server/keycert.p12")),
        config("tls", "server.applicationConnectors[1].keyStorePath", resourceFilePath("stores/server/self_sign_keycert.p12")),
        config("tls", "server.applicationConnectors[2].keyStorePath", resourceFilePath("stores/server/keycert.p12")),
        config("tls", "server.applicationConnectors[2].trustStorePath", resourceFilePath("stores/server/ca_truststore.ts")),
        config("tls", "server.applicationConnectors[2].wantClientAuth", "true"),
        config("tls", "server.applicationConnectors[2].needClientAuth", "true"),
        config("tls", "server.applicationConnectors[2].validatePeers", "false"),
        config("tls", "server.applicationConnectors[2].trustStorePassword", "password"),
        config("tls", "server.applicationConnectors[3].keyStorePath", resourceFilePath("stores/server/bad_host_keycert.p12")),
        config("tls", "server.applicationConnectors[4].keyStorePath", resourceFilePath("stores/server/keycert.p12")),
        config("tls", "server.applicationConnectors[4].supportedProtocols", "SSLv1,SSLv2,SSLv3"),
        config("tls", "server.applicationConnectors[5].keyStorePath", resourceFilePath("stores/server/acme-weak.keystore.p12")),
        config("tls", "server.applicationConnectors[5].trustStorePath", resourceFilePath("stores/server/acme-weak.truststore.p12")),
        config("tls", "server.applicationConnectors[5].wantClientAuth", "true"),
        config("tls", "server.applicationConnectors[5].needClientAuth", "true"),
        config("tls", "server.applicationConnectors[5].validatePeers", "true"),
        config("tls", "server.applicationConnectors[5].trustStorePassword", "acme2"),
        config("tls", "server.applicationConnectors[5].keyStorePassword", "acme2"),
        config("tls", "server.applicationConnectors[5].trustStoreProvider", PROVIDER_NAME),
        config("tls", "server.applicationConnectors[5].keyStoreProvider", PROVIDER_NAME));

    @BeforeEach
    void setUp() throws Exception {
        tlsConfiguration = new TlsConfiguration();
        tlsConfiguration.setTrustStorePath(toFile("stores/server/ca_truststore.ts"));
        tlsConfiguration.setTrustStorePassword("password");
        jerseyClientConfiguration = new JerseyClientConfiguration();
        jerseyClientConfiguration.setTlsConfiguration(tlsConfiguration);
        jerseyClientConfiguration.setConnectionTimeout(Duration.milliseconds(2000));
        jerseyClientConfiguration.setTimeout(Duration.milliseconds(5000));
    }

    @Test
    void configOnlyConstructorShouldSetNullCustomVerifier() {
        assertThat(new DropwizardSSLConnectionSocketFactory(tlsConfiguration).verifier).isNull();
    }

    @Test
    void shouldReturn200IfServerCertInTruststore() {
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("tls_working_client");
        final Response response = client.target(String.format("https://localhost:%d", TLS_APP_RULE.getLocalPort())).request().get();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void shouldErrorIfServerCertNotFoundInTruststore() throws Exception {
        tlsConfiguration.setTrustStorePath(toFile("stores/server/other_cert_truststore.ts"));
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("tls_broken_client");
        Invocation.Builder request = client.target(String.format("https://localhost:%d", TLS_APP_RULE.getLocalPort())).request();
        assertThatExceptionOfType(ProcessingException.class)
            .isThrownBy(request::get)
            .withCauseInstanceOf(SSLHandshakeException.class);
    }

    @Test
    void shouldNotErrorIfServerCertSelfSignedAndSelfSignedCertsAllowed() {
        tlsConfiguration.setTrustSelfSignedCertificates(true);
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("self_sign_permitted");
        final Response response = client.target(String.format("https://localhost:%d", TLS_APP_RULE.getTestSupport().getPort(1))).request().get();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void shouldErrorIfServerCertSelfSignedAndSelfSignedCertsNotAllowed() {
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("self_sign_failure");
        Invocation.Builder request = client.target(String.format("https://localhost:%d", TLS_APP_RULE.getPort(1))).request();
        assertThatExceptionOfType(ProcessingException.class)
            .isThrownBy(() -> request.get(ClientResponse.class))
            .withCauseInstanceOf(SSLHandshakeException.class);
    }

    @Test
    void shouldReturn200IfAbleToClientAuth() throws Exception {
        tlsConfiguration.setKeyStorePath(toFile("stores/client/keycert.p12"));
        tlsConfiguration.setKeyStorePassword("password");
        tlsConfiguration.setKeyStoreType("PKCS12");
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("client_auth_working");
        final Response response = client.target(String.format("https://localhost:%d", TLS_APP_RULE.getPort(2))).request().get();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void shouldErrorIfClientAuthFails() throws Exception {
        tlsConfiguration.setKeyStorePath(toFile("stores/server/self_sign_keycert.p12"));
        tlsConfiguration.setKeyStorePassword("password");
        tlsConfiguration.setKeyStoreType("PKCS12");
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("client_auth_broken");
        Invocation.Builder request = client.target(String.format("https://localhost:%d", TLS_APP_RULE.getPort(2))).request();
        assertThatExceptionOfType(ProcessingException.class)
            .isThrownBy(request::get)
            .satisfies(e -> assertThat(e.getCause()).isInstanceOfAny(SocketException.class, SSLHandshakeException.class, SSLException.class));
    }

    @Test
    void shouldReturn200IfAbleToClientAuthSpecifyingCertAliasForGoodCert() throws Exception {
        tlsConfiguration.setKeyStorePath(toFile("stores/client/twokeys.p12"));
        tlsConfiguration.setKeyStorePassword("password");
        tlsConfiguration.setKeyStoreType("PKCS12");
        tlsConfiguration.setCertAlias("1");
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("client_auth_using_cert_alias_working");
        final Response response = client.target(String.format("https://localhost:%d", TLS_APP_RULE.getPort(2))).request().get();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void shouldErrorIfTryToClientAuthSpecifyingCertAliasForBadCert() throws Exception {
        tlsConfiguration.setKeyStorePath(toFile("stores/client/twokeys.p12"));
        tlsConfiguration.setKeyStorePassword("password");
        tlsConfiguration.setKeyStoreType("PKCS12");
        tlsConfiguration.setCertAlias("2");
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("client_auth_using_cert_alias_broken");
        Invocation.Builder request = client.target(String.format("https://localhost:%d", TLS_APP_RULE.getPort(2))).request();
        assertThatExceptionOfType(ProcessingException.class)
            .isThrownBy(request::get)
            .satisfies(e -> assertThat(e.getCause()).isInstanceOfAny(SocketException.class, SSLHandshakeException.class, SSLException.class));
    }

    @Test
    void shouldErrorIfTryToClientAuthSpecifyingUnknownCertAlias() throws Exception {
        tlsConfiguration.setKeyStorePath(toFile("stores/client/twokeys.p12"));
        tlsConfiguration.setKeyStorePassword("password");
        tlsConfiguration.setKeyStoreType("PKCS12");
        tlsConfiguration.setCertAlias("unknown");
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("client_auth_using_unknown_cert_alias_broken");
        Invocation.Builder request = client.target(String.format("https://localhost:%d", TLS_APP_RULE.getPort(2))).request();
        assertThatExceptionOfType(ProcessingException.class)
            .isThrownBy(request::get)
            .satisfies(e -> assertThat(e.getCause()).isInstanceOfAny(SocketException.class, SSLHandshakeException.class, SSLException.class));
    }

    @Test
    void shouldErrorIfHostnameVerificationOnAndServerHostnameDoesntMatch() {
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("bad_host_broken");
        assertThatThrownBy(() -> client.target(String.format("https://localhost:%d", TLS_APP_RULE.getPort(3))).request().get())
            .hasCauseExactlyInstanceOf(SSLPeerUnverifiedException.class)
            .hasRootCauseMessage("Certificate for <localhost> doesn't match common name of the certificate subject: badhost");
    }

    @Test
    void shouldErrorIfHostnameVerificationOnAndServerHostnameMatchesAndFailVerifierSpecified() {
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).using(new FailVerifier()).build("bad_host_broken_fail_verifier");
        assertThatThrownBy(() -> client.target(String.format("https://localhost:%d", TLS_APP_RULE.getLocalPort())).request().get())
            .hasCauseExactlyInstanceOf(SSLPeerUnverifiedException.class)
            .hasRootCauseMessage("Certificate for <localhost> doesn't match any of the subject alternative names: []");
    }

    @Test
    void shouldBeOkIfHostnameVerificationOnAndServerHostnameDoesntMatchAndNoopVerifierSpecified() {
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).using(new NoopHostnameVerifier()).build("bad_host_noop_verifier_working");
        final Response response = client.target(String.format("https://localhost:%d", TLS_APP_RULE.getPort(3))).request().get();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void shouldBeOkIfHostnameVerificationOffAndServerHostnameDoesntMatch() {
        tlsConfiguration.setVerifyHostname(false);
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("bad_host_working");
        final Response response = client.target(String.format("https://localhost:%d", TLS_APP_RULE.getPort(3))).request().get();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void shouldBeOkIfHostnameVerificationOffAndServerHostnameMatchesAndFailVerifierSpecified() {
        tlsConfiguration.setVerifyHostname(false);
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).using(new FailVerifier()).build("bad_host_fail_verifier_working");
        final Response response = client.target(String.format("https://localhost:%d", TLS_APP_RULE.getLocalPort())).request().get();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void shouldRejectNonSupportedProtocols() {
        tlsConfiguration.setSupportedProtocols(Collections.singletonList("TLSv1.2"));
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("reject_non_supported");
        Invocation.Builder request = client.target(String.format("https://localhost:%d", TLS_APP_RULE.getPort(4))).request();
        assertThatExceptionOfType(ProcessingException.class)
            .isThrownBy(request::get)
            .withRootCauseInstanceOf(IOException.class);
    }

    @Test
    void shouldSucceedWithBcProvider() throws Exception {
        // switching host verifier off for simplicity
        tlsConfiguration.setVerifyHostname(false);

        tlsConfiguration.setKeyStorePath(toFile("stores/client/acme-weak.keystore.p12"));
        tlsConfiguration.setKeyStorePassword("acme2");
        tlsConfiguration.setKeyStoreType("PKCS12");
        tlsConfiguration.setKeyStoreProvider(PROVIDER_NAME);
        tlsConfiguration.setCertAlias("acme-weak");

        tlsConfiguration.setTrustStorePath(toFile("stores/server/acme-weak.truststore.p12"));
        tlsConfiguration.setTrustStorePassword("acme2");
        tlsConfiguration.setTrustStoreType("PKCS12");
        tlsConfiguration.setTrustStoreProvider(PROVIDER_NAME);

        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("custom_jce_supported");
        final Response response = client.target(String.format("https://localhost:%d", TLS_APP_RULE.getPort(5))).request().get();

        assertThat(response.getStatus()).isEqualTo(200);
    }

    private static class FailVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String arg0, SSLSession arg1) {
            return false;
        }
    }

    private static File toFile(final String resourceName) throws URISyntaxException {
        return new File(getResource(resourceName).toURI());
    }
}
