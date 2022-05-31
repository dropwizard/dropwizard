package io.dropwizard.jetty;

import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.util.DataSize;
import io.dropwizard.validation.BaseValidator;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GzipHandlerFactoryTest {
    private GzipHandlerFactory gzip;

    @BeforeEach
    void setUp() throws Exception {
        this.gzip = new YamlConfigurationFactory<>(GzipHandlerFactory.class,
                BaseValidator.newValidator(), Jackson.newObjectMapper(), "dw")
                .build(new ResourceConfigurationSourceProvider(),"yaml/gzip.yml");
    }

    @Test
    void canBeEnabled() {
        assertThat(gzip.isEnabled())
                .isFalse();
    }

    @Test
    void hasAMinimumEntitySize() {
        assertThat(gzip.getMinimumEntitySize())
                .isEqualTo(DataSize.kibibytes(12));
    }

    @Test
    void hasABufferSize() {
        assertThat(gzip.getBufferSize())
                .isEqualTo(DataSize.kibibytes(32));
    }

    @Test
    void hasCompressedMimeTypes() {
        assertThat(gzip.getCompressedMimeTypes())
                .isEqualTo(Collections.singleton("text/plain"));
    }

    @Test
    void testBuild() {
        final GzipHandler handler = gzip.build(null);

        assertThat(handler.getMinGzipSize()).isEqualTo((int) gzip.getMinimumEntitySize().toBytes());
        assertThat(handler.getIncludedMimeTypes()).containsOnly("text/plain");
        assertThat(handler.getIncludedMethods()).containsOnly("GET", "POST");
    }

    @Test
    void testBuildDefault() throws Exception {
        final GzipHandler handler = new YamlConfigurationFactory<>(GzipHandlerFactory.class,
                BaseValidator.newValidator(), Jackson.newObjectMapper(), "dw")
                .build(new ResourceConfigurationSourceProvider(), "yaml/default_gzip.yml")
                .build(null);

        assertThat(handler.getMinGzipSize()).isEqualTo(256);
        assertThat(handler.getIncludedMimeTypes()).isEmpty(); // All apart excluded
        assertThat(handler.getIncludedMethods()).containsExactlyInAnyOrder("GET", "POST");
    }

    @Test
    void testBuilderProperties() {
        GzipHandlerFactory gzip = new GzipHandlerFactory();
        gzip.setMinimumEntitySize(DataSize.bytes(4096));
        gzip.setIncludedMethods(Set.of("GET", "POST"));
        gzip.setCompressedMimeTypes(Set.of("text/html", "application/json"));
        gzip.setExcludedMimeTypes(Collections.singleton("application/thrift"));
        gzip.setIncludedPaths(Collections.singleton("/include/me"));
        gzip.setExcludedPaths(Collections.singleton("/exclude/me"));
        Handler handler = mock(Handler.class);
        GzipHandler biDiGzipHandler = gzip.build(handler);

        assertThat(biDiGzipHandler.getMinGzipSize()).isEqualTo(4096);
        assertThat(biDiGzipHandler.getIncludedMethods()).containsExactlyInAnyOrder("GET", "POST");
        assertThat(biDiGzipHandler.getIncludedMimeTypes()).containsExactlyInAnyOrder("text/html", "application/json");
        assertThat(biDiGzipHandler.getExcludedMimeTypes()).containsExactly("application/thrift");
        assertThat(biDiGzipHandler.getIncludedPaths()).containsExactly("/include/me");
        assertThat(biDiGzipHandler.getExcludedPaths()).containsExactly("/exclude/me");
    }
}
