package io.dropwizard.client.ssl;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.client.DropwizardSSLConnectionSocketFactory;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.util.Duration;
import org.apache.http.conn.ssl.SSLInitializationException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.glassfish.jersey.client.ClientResponse;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.SocketException;
import java.security.Security;
import java.util.Collections;
import java.util.Optional;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME;

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

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @AfterClass
    public static void classTearDown() throws Exception {
        Security.removeProvider(PROVIDER_NAME);
    }

    @ClassRule
    public static final DropwizardAppRule<Configuration> TLS_APP_RULE = new DropwizardAppRule<>(TlsTestApplication.class,
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
        ConfigOverride.config("tls", "server.applicationConnectors[4].supportedProtocols", "SSLv1,SSLv2,SSLv3"),
        ConfigOverride.config("tls", "server.applicationConnectors[5].keyStorePath", ResourceHelpers.resourceFilePath("stores/server/acme-weak.keystore.p12")),
        ConfigOverride.config("tls", "server.applicationConnectors[5].trustStorePath", ResourceHelpers.resourceFilePath("stores/server/acme-weak.truststore.p12")),
        ConfigOverride.config("tls", "server.applicationConnectors[5].wantClientAuth", "true"),
        ConfigOverride.config("tls", "server.applicationConnectors[5].needClientAuth", "true"),
        ConfigOverride.config("tls", "server.applicationConnectors[5].validatePeers", "true"),
        ConfigOverride.config("tls", "server.applicationConnectors[5].trustStorePassword", "acme2"),
        ConfigOverride.config("tls", "server.applicationConnectors[5].keyStorePassword", "acme2"),
        ConfigOverride.config("tls", "server.applicationConnectors[5].trustStoreProvider", PROVIDER_NAME),
        ConfigOverride.config("tls", "server.applicationConnectors[5].keyStoreProvider", PROVIDER_NAME));

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
    public void configOnlyConstructorShouldSetNullCustomVerifier() throws Exception {
        final DropwizardSSLConnectionSocketFactory socketFactory;
        socketFactory = new DropwizardSSLConnectionSocketFactory(tlsConfiguration);

        final Field verifierField =
                FieldUtils.getField(DropwizardSSLConnectionSocketFactory.class, "verifier", true);
        assertThat(verifierField.get(socketFactory)).isNull();
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
        assertThatThrownBy(() -> client.target(String.format("https://localhost:%d", TLS_APP_RULE.getLocalPort())).request().get())
            .isInstanceOf(ProcessingException.class)
            .hasCauseInstanceOf(SSLHandshakeException.class);
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
        assertThatThrownBy(() -> client.target(String.format("https://localhost:%d", TLS_APP_RULE.getPort(1))).request().get(ClientResponse.class))
            .isInstanceOf(ProcessingException.class)
            .hasCauseInstanceOf(SSLHandshakeException.class);
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
        final Throwable exn = catchThrowable(() -> client.target(String.format("https://localhost:%d", TLS_APP_RULE.getPort(2))).request().get());
        assertThat(exn).isInstanceOf(ProcessingException.class);
        assertThat(exn.getCause()).isInstanceOfAny(SocketException.class, SSLHandshakeException.class, SSLException.class);
    }

    @Test
    public void shouldReturn200IfAbleToClientAuthSpecifyingCertAliasForGoodCert() throws Exception {
        tlsConfiguration.setKeyStorePath(new File(ResourceHelpers.resourceFilePath("stores/client/twokeys.p12")));
        tlsConfiguration.setKeyStorePassword("password");
        tlsConfiguration.setKeyStoreType("PKCS12");
        tlsConfiguration.setCertAlias("1");
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("client_auth_using_cert_alias_working");
        final Response response = client.target(String.format("https://localhost:%d", TLS_APP_RULE.getPort(2))).request().get();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void shouldErrorIfTryToClientAuthSpecifyingCertAliasForBadCert() throws Exception {
        tlsConfiguration.setKeyStorePath(new File(ResourceHelpers.resourceFilePath("stores/client/twokeys.p12")));
        tlsConfiguration.setKeyStorePassword("password");
        tlsConfiguration.setKeyStoreType("PKCS12");
        tlsConfiguration.setCertAlias("2");
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("client_auth_using_cert_alias_broken");
        final Throwable exn = catchThrowable(() -> client.target(String.format("https://localhost:%d", TLS_APP_RULE.getPort(2))).request().get());
        assertThat(exn).isInstanceOf(ProcessingException.class);
        assertThat(exn.getCause()).isInstanceOfAny(SocketException.class, SSLHandshakeException.class, SSLException.class);
    }

    @Test
    public void shouldErrorIfTryToClientAuthSpecifyingUnknownCertAlias() throws Exception {
        tlsConfiguration.setKeyStorePath(new File(ResourceHelpers.resourceFilePath("stores/client/twokeys.p12")));
        tlsConfiguration.setKeyStorePassword("password");
        tlsConfiguration.setKeyStoreType("PKCS12");
        tlsConfiguration.setCertAlias("unknown");
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("client_auth_using_unknown_cert_alias_broken");
        final Throwable exn = catchThrowable(() -> client.target(String.format("https://localhost:%d", TLS_APP_RULE.getPort(2))).request().get());
        assertThat(exn).isInstanceOf(ProcessingException.class);
        assertThat(exn.getCause()).isInstanceOfAny(SocketException.class, SSLHandshakeException.class, SSLException.class);
    }

    @Test
    public void shouldErrorIfHostnameVerificationOnAndServerHostnameDoesntMatch() throws Exception {
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("bad_host_broken");
        final Throwable exn = catchThrowable(() -> client.target(String.format("https://localhost:%d", TLS_APP_RULE.getPort(3))).request().get());
        assertThat(exn).hasCauseExactlyInstanceOf(SSLPeerUnverifiedException.class);
        assertThat(exn.getCause()).hasMessage("Certificate for <localhost> doesn't match any of the subject alternative names: []");
    }

    @Test
    public void shouldErrorIfHostnameVerificationOnAndServerHostnameMatchesAndFailVerifierSpecified() throws Exception {
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).using(new FailVerifier()).build("bad_host_broken_fail_verifier");
        final Throwable exn = catchThrowable(() -> client.target(String.format("https://localhost:%d", TLS_APP_RULE.getLocalPort())).request().get());
        assertThat(exn).hasCauseExactlyInstanceOf(SSLPeerUnverifiedException.class);
        assertThat(exn.getCause()).hasMessage("Certificate for <localhost> doesn't match any of the subject alternative names: []");
    }

    @Test
    public void shouldBeOkIfHostnameVerificationOnAndServerHostnameDoesntMatchAndNoopVerifierSpecified() throws Exception {
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).using(new NoopHostnameVerifier()).build("bad_host_noop_verifier_working");
        final Response response = client.target(String.format("https://localhost:%d", TLS_APP_RULE.getPort(3))).request().get();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void shouldBeOkIfHostnameVerificationOffAndServerHostnameDoesntMatch() throws Exception {
        tlsConfiguration.setVerifyHostname(false);
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("bad_host_working");
        final Response response = client.target(String.format("https://localhost:%d", TLS_APP_RULE.getPort(3))).request().get();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void shouldBeOkIfHostnameVerificationOffAndServerHostnameMatchesAndFailVerfierSpecified() throws Exception {
        tlsConfiguration.setVerifyHostname(false);
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).using(new FailVerifier()).build("bad_host_fail_verifier_working");
        final Response response = client.target(String.format("https://localhost:%d", TLS_APP_RULE.getLocalPort())).request().get();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void shouldRejectNonSupportedProtocols() throws Exception {
        tlsConfiguration.setSupportedProtocols(Collections.singletonList("TLSv1.2"));
        final Client client = new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("reject_non_supported");
        assertThatThrownBy(() -> client.target(String.format("https://localhost:%d", TLS_APP_RULE.getPort(4))).request().get())
            .isInstanceOf(ProcessingException.class)
            .hasRootCauseInstanceOf(IOException.class);
    }

    @Test(expected = SSLInitializationException.class)
    public void shouldFailDueDefaultProviderInsufficiency() throws Exception {
        tlsConfiguration.setKeyStorePath(new File(ResourceHelpers.resourceFilePath("stores/client/acme-weak.keystore.p12")));
        tlsConfiguration.setKeyStorePassword("acme2");
        tlsConfiguration.setKeyStoreType("PKCS12");
        tlsConfiguration.setCertAlias("acme-weak");
        tlsConfiguration.setTrustStorePath(new File(ResourceHelpers.resourceFilePath("stores/server/acme-weak.truststore.p12")));
        tlsConfiguration.setTrustStorePassword("acme2");
        tlsConfiguration.setTrustStoreType("PKCS12");

        new JerseyClientBuilder(TLS_APP_RULE.getEnvironment()).using(jerseyClientConfiguration).build("reject_provider_non_supported");
    }

    @Test
    public void shouldSucceedWithBcProvider() throws Exception {
        // switching host verifier off for simplicity
        tlsConfiguration.setVerifyHostname(false);

        tlsConfiguration.setKeyStorePath(new File(ResourceHelpers.resourceFilePath("stores/client/acme-weak.keystore.p12")));
        tlsConfiguration.setKeyStorePassword("acme2");
        tlsConfiguration.setKeyStoreType("PKCS12");
        tlsConfiguration.setKeyStoreProvider(PROVIDER_NAME);
        tlsConfiguration.setCertAlias("acme-weak");

        tlsConfiguration.setTrustStorePath(new File(ResourceHelpers.resourceFilePath("stores/server/acme-weak.truststore.p12")));
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
}
