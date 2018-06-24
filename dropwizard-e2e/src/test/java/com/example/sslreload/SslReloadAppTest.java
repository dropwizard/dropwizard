package com.example.sslreload;

import io.dropwizard.Configuration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.util.CharStreams;
import io.dropwizard.util.Resources;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class SslReloadAppTest {
    @ClassRule
    public static final TemporaryFolder FOLDER = new TemporaryFolder();

    private static final X509TrustManager TRUST_ALL = new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    };

    private static Path keystore;

    @Rule
    public final DropwizardAppRule<Configuration> rule =
        new DropwizardAppRule<>(SslReloadApp.class, ResourceHelpers.resourceFilePath("sslreload/config.yml"),
            ConfigOverride.config("server.applicationConnectors[0].keyStorePath", keystore.toString()),
            ConfigOverride.config("server.adminConnectors[0].keyStorePath", keystore.toString()));

    @BeforeClass
    public static void setupClass() throws IOException {
        keystore = FOLDER.newFile("keystore.jks").toPath();
        final byte[] keystoreBytes = Resources.toByteArray(Resources.getResource("sslreload/keystore.jks"));
        Files.write(keystore, keystoreBytes);
    }

    @After
    public void after() throws IOException {
        // Reset keystore to known good keystore
        final byte[] keystoreBytes = Resources.toByteArray(Resources.getResource("sslreload/keystore.jks"));
        Files.write(keystore, keystoreBytes);
    }

    @Test
    public void reloadCertificateChangesTheServerCertificate() throws Exception {
        // Copy over our new keystore that has our new certificate to the current
        // location of our keystore
        final byte[] keystore2Bytes = Resources.toByteArray(Resources.getResource("sslreload/keystore2.jks"));
        Files.write(keystore, keystore2Bytes);

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
    public void badReloadDoesNotChangeTheServerCertificate() throws Exception {
        // This keystore has a different password than what jetty has been configured with
        // the password is "password2"
        final byte[] badKeystore = Resources.toByteArray(Resources.getResource("sslreload/keystore-diff-pwd.jks"));
        Files.write(keystore, badKeystore);

        // Get the bytes for the first certificate. The reload should fail
        byte[] firstCertBytes = certBytes(500, "Keystore was tampered with, or password was incorrect");

        // Issue another request. The returned certificate should be the same as before
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
                assertThat(CharStreams.toString(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)))
                    .isEqualTo(content);
            } else {
                assertThat(CharStreams.toString(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8)))
                    .contains(content);
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

        conn.setHostnameVerifier(new NoopHostnameVerifier());
        conn.setSSLSocketFactory(sslCtx.getSocketFactory());

        // Make it a POST
        conn.setDoOutput(true);
        conn.getOutputStream().write(new byte[]{});
    }
}
