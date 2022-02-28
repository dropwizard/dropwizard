package com.example.sslreload;

import io.dropwizard.Configuration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.util.ByteStreams;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.X509Certificate;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class SslReloadAppTest {

    private static final X509TrustManager TRUST_ALL = new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    };

    private static Path keystore;

    public final DropwizardAppExtension<Configuration> rule =
        new DropwizardAppExtension<>(SslReloadApp.class, ResourceHelpers.resourceFilePath("sslreload/config.yml"),
            ConfigOverride.config("server.applicationConnectors[0].keyStorePath", keystore.toString()),
            ConfigOverride.config("server.adminConnectors[0].keyStorePath", keystore.toString()));

    @BeforeAll
    public static void setupClass(@TempDir Path tempDir) throws IOException {
        keystore = tempDir.resolve("keystore.jks");
        try (InputStream inputStream = SslReloadAppTest.class.getResourceAsStream("/sslreload/keystore.jks")) {
            Files.write(keystore, ByteStreams.toByteArray(inputStream));
        }
    }

    @AfterEach
    void after() throws IOException {
        // Reset keystore to known good keystore
        writeKeystore("/sslreload/keystore.jks");
    }

    @Test
    void reloadCertificateChangesTheServerCertificate() throws Exception {
        // Copy over our new keystore that has our new certificate to the current
        // location of our keystore
        writeKeystore("/sslreload/keystore2.jks");

        // Get the bytes for the first certificate, and trigger a reload
        byte[] firstCertBytes = certBytes(200, "Reloaded certificate configuration\n");

        // Get the bytes from our newly reloaded certificate
        byte[] secondCertBytes = certBytes(200, "Reloaded certificate configuration\n");

        // Get the bytes from the reloaded certificate, but it should be the same
        // as the second cert because we didn't change anything!
        byte[] thirdCertBytes = certBytes(200, "Reloaded certificate configuration\n");

        assertThat(firstCertBytes).isNotEqualTo(secondCertBytes);
        assertThat(secondCertBytes).isEqualTo(thirdCertBytes);
    }

    @Test
    void badReloadDoesNotChangeTheServerCertificate() throws Exception {
        // This keystore has a different password than what jetty has been configured with
        // the password is "password2"
        writeKeystore("/sslreload/keystore-diff-pwd.jks");

        // Get the bytes for the first certificate. The reload should fail
        byte[] firstCertBytes = certBytes(500, "Keystore was tampered with, or password was incorrect");

        // Issue another request. The returned certificate should be the same as setUp
        byte[] secondCertBytes = certBytes(500, "Keystore was tampered with, or password was incorrect");

        // And just to triple check, a third request will continue with
        // the same original certificate
        byte[] thirdCertBytes = certBytes(500, "Keystore was tampered with, or password was incorrect");

        assertThat(firstCertBytes)
            .isEqualTo(secondCertBytes)
            .isEqualTo(thirdCertBytes);
    }

    /** Issues a POST against the reload ssl admin task, asserts that the code and content
     *  are as expected, and finally returns the server certificate */
    private byte[] certBytes(int code, String content) throws Exception {
        final URL url = new URL("https://localhost:" + rule.getAdminPort() + "/tasks/reload-ssl");
        final HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        try {
            postIt(conn);

            assertThat(conn.getResponseCode()).isEqualTo(code);
            if (code == 200) {
                assertThat(conn.getInputStream()).asString(UTF_8).isEqualTo(content);
            } else {
                assertThat(conn.getErrorStream()).asString(UTF_8).contains(content);
            }

            // The certificates are self signed, so are the only cert in the chain.
            // Thus, we return the one and only certificate.
            return conn.getServerCertificates()[0].getEncoded();
        } finally {
            conn.disconnect();
        }
    }

    /** Configure SSL and POST request parameters */
    private void postIt(HttpsURLConnection conn) throws Exception {
        final SSLContext sslCtx = SSLContext.getInstance("TLS");
        sslCtx.init(null, new TrustManager[]{TRUST_ALL}, null);

        conn.setHostnameVerifier((String s, SSLSession sslSession) -> true);
        conn.setSSLSocketFactory(sslCtx.getSocketFactory());

        // Make it a POST
        conn.setDoOutput(true);
        conn.getOutputStream().write(new byte[]{});
    }

    private void writeKeystore(String source) throws IOException {
        try (final InputStream inputStream = getClass().getResourceAsStream(source)) {
            Files.write(keystore, ByteStreams.toByteArray(inputStream));
        }
    }
}
