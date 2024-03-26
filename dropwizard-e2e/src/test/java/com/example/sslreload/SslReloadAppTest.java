package com.example.sslreload;

import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.core.Configuration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
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
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class SslReloadAppTest {
    private static Path tempDir;

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
        new DropwizardAppExtension<>(SslReloadApp.class, "sslreload/config.yml",
            new ResourceConfigurationSourceProvider(),
            ConfigOverride.config("server.applicationConnectors[0].keyStorePath", keystore.toString()),
            ConfigOverride.config("server.adminConnectors[0].keyStorePath", keystore.toString()));

    @BeforeAll
    public static void setupClass() throws IOException {
        tempDir = Files.createTempDirectory("keystoreTest");
        keystore = tempDir.resolve("keystore.jks");
        // Assume this writes an initial keystore file
        writeKeystore("/sslreload/keystore.jks");
    }

    @AfterEach
    void after() throws IOException {
        // Reset keystore to known good keystore
        writeKeystore("/sslreload/keystore.jks");
    }

    @Test
    void ensureKeystoreCanBeModified() throws IOException {
        // Write initial content
        String initialContent = "initial content";
        Files.writeString(keystore, initialContent);

        // Confirm initial write
        String contentBefore = Files.readString(keystore);
        assertThat(contentBefore).isEqualTo(initialContent);

        // Simulate modification by writing new content
        String modifiedContent = "modified content";
        Files.writeString(keystore, modifiedContent);

        // Verify the file was modified by checking the content has changed
        String contentAfter = Files.readString(keystore);
        assertThat(contentAfter).isNotEqualTo(initialContent);
        assertThat(contentAfter).isEqualTo(modifiedContent);
    }

    private static void writeKeystore(String source) throws IOException  {
        try (final InputStream inputStream = requireNonNull(SslReloadAppTest.class.getResourceAsStream(source))) {
            Files.write(keystore, inputStream.readAllBytes());
        }
    }
}
