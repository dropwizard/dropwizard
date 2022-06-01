package io.dropwizard.http2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class Http2ConnectorFactoryTest {

    private final Http2ConnectorFactory http2ConnectorFactory = new Http2ConnectorFactory();

    @Test
    void testSetDefaultHttp2Cipher() {
        http2ConnectorFactory.checkSupportedCipherSuites();

        assertThat(http2ConnectorFactory.getSupportedCipherSuites())
                .containsExactly("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
    }

    @Test
    void testCustomCiphersAreSupported() {
        http2ConnectorFactory.setSupportedCipherSuites(
                Arrays.asList("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"));

        http2ConnectorFactory.checkSupportedCipherSuites();

        assertThat(http2ConnectorFactory.getSupportedCipherSuites())
                .containsExactly("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
    }

    @Test
    void testThrowExceptionIfDefaultCipherIsNotSet() {
        http2ConnectorFactory.setSupportedCipherSuites(
                Collections.singletonList("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"));

        assertThatIllegalArgumentException()
                .isThrownBy(http2ConnectorFactory::checkSupportedCipherSuites)
                .withMessage("HTTP/2 server configuration must include cipher: TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
    }
}
