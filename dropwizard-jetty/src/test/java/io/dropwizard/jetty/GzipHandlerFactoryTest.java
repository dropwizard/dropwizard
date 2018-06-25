package io.dropwizard.jetty;

import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.util.Resources;
import io.dropwizard.util.Sets;
import io.dropwizard.util.Size;
import io.dropwizard.validation.BaseValidator;
import org.eclipse.jetty.server.Handler;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.zip.Deflater;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class GzipHandlerFactoryTest {
    private GzipHandlerFactory gzip;

    @Before
    public void setUp() throws Exception {
        this.gzip = new YamlConfigurationFactory<>(GzipHandlerFactory.class,
                BaseValidator.newValidator(), Jackson.newObjectMapper(), "dw")
                .build(new File(Resources.getResource("yaml/gzip.yml").toURI()));
    }

    @Test
    public void canBeEnabled() throws Exception {
        assertThat(gzip.isEnabled())
                .isFalse();
    }

    @Test
    public void hasAMinimumEntitySize() throws Exception {
        assertThat(gzip.getMinimumEntitySize())
                .isEqualTo(Size.kilobytes(12));
    }

    @Test
    public void hasABufferSize() throws Exception {
        assertThat(gzip.getBufferSize())
                .isEqualTo(Size.kilobytes(32));
    }

    @Test
    public void hasExcludedUserAgentPatterns() throws Exception {
        assertThat(gzip.getExcludedUserAgentPatterns())
                .isEqualTo(Collections.singleton("OLD-2.+"));
    }

    @Test
    public void hasCompressedMimeTypes() throws Exception {
        assertThat(gzip.getCompressedMimeTypes())
                .isEqualTo(Collections.singleton("text/plain"));
    }

    @Test
    public void testBuild() {
        final BiDiGzipHandler handler = gzip.build(null);

        assertThat(handler.getMinGzipSize()).isEqualTo((int) gzip.getMinimumEntitySize().toBytes());
        assertThat(handler.getExcludedAgentPatterns()).hasSize(1);
        assertThat(handler.getExcludedAgentPatterns()[0]).isEqualTo("OLD-2.+");
        assertThat(handler.getIncludedMimeTypes()).containsOnly("text/plain");
        assertThat(handler.getIncludedMethods()).containsOnly("GET", "POST");
        assertThat(handler.getCompressionLevel()).isEqualTo(Deflater.DEFAULT_COMPRESSION);
        assertThat(handler.isInflateNoWrap()).isTrue();
    }

    @Test
    public void testBuildDefault() throws Exception {
        final BiDiGzipHandler handler = new YamlConfigurationFactory<>(GzipHandlerFactory.class,
                BaseValidator.newValidator(), Jackson.newObjectMapper(), "dw")
                .build(new File(Resources.getResource("yaml/default_gzip.yml").toURI()))
                .build(null);

        assertThat(handler.getMinGzipSize()).isEqualTo(256);
        assertThat(handler.getExcludedAgentPatterns()).isEmpty();
        assertThat(handler.getIncludedMimeTypes()).isEmpty(); // All apart excluded
        assertThat(handler.getIncludedMethods()).containsOnly("GET");
        assertThat(handler.getCompressionLevel()).isEqualTo(Deflater.DEFAULT_COMPRESSION);
        assertThat(handler.isInflateNoWrap()).isTrue();
    }

    @Test
    public void testBuilderProperties() {
        GzipHandlerFactory gzip = new GzipHandlerFactory();
        gzip.setGzipCompatibleInflation(true); // Also known as "inflate no wrap"
        gzip.setMinimumEntitySize(Size.bytes(4096));
        gzip.setIncludedMethods(Sets.of("GET", "POST"));
        gzip.setExcludedUserAgentPatterns(Collections.singleton("MSIE 6.0"));
        gzip.setCompressedMimeTypes(Sets.of("text/html", "application/json"));
        gzip.setExcludedMimeTypes(Collections.singleton("application/thrift"));
        gzip.setIncludedPaths(Collections.singleton("/include/me"));
        gzip.setExcludedPaths(Collections.singleton("/exclude/me"));
        Handler handler = mock(Handler.class);
        BiDiGzipHandler biDiGzipHandler = gzip.build(handler);

        assertThat(biDiGzipHandler.isInflateNoWrap()).isTrue();
        assertThat(biDiGzipHandler.getMinGzipSize()).isEqualTo(4096);
        assertThat(biDiGzipHandler.getIncludedMethods()).containsExactlyInAnyOrder("GET", "POST");
        assertThat(biDiGzipHandler.getExcludedAgentPatterns()).containsExactly("MSIE 6.0");
        assertThat(biDiGzipHandler.getIncludedMimeTypes()).containsExactlyInAnyOrder("text/html", "application/json");
        assertThat(biDiGzipHandler.getExcludedMimeTypes()).containsExactly("application/thrift");
        assertThat(biDiGzipHandler.getIncludedPaths()).containsExactly("/include/me");
        assertThat(biDiGzipHandler.getExcludedPaths()).containsExactly("/exclude/me");
    }
}
