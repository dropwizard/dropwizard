package io.dropwizard.http2;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class Http2ConnectorFactoryTest {

    private Http2ConnectorFactory http2ConnectorFactory = new Http2ConnectorFactory();

    @Test
    public void testSetDefaultHttp2Cipher() {
        http2ConnectorFactory.checkSupportedCipherSuites();

        assertThat(http2ConnectorFactory.getSupportedCipherSuites()).containsExactly(
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
    }

    @Test
    public void testCustomCiphersAreSupported() {
        http2ConnectorFactory.setSupportedCipherSuites(ImmutableList.of("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
            "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"));

        http2ConnectorFactory.checkSupportedCipherSuites();

        assertThat(http2ConnectorFactory.getSupportedCipherSuites()).containsExactly(
            "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
    }

    @Test
    public void testThrowExceptionIfDefaultCipherIsNotSet() {
        http2ConnectorFactory.setSupportedCipherSuites(ImmutableList.of("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384"));

        assertThatIllegalArgumentException().isThrownBy(() -> http2ConnectorFactory.checkSupportedCipherSuites())
            .withMessage("HTTP/2 server configuration must include cipher: TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");
    }
}
