package io.dropwizard.views.mustache;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.logging.common.BootstrapLogging;
import io.dropwizard.views.common.CspNonceLookup;
import io.dropwizard.views.common.ViewMessageBodyWriter;
import io.dropwizard.views.common.ViewRenderExceptionMapper;
import io.dropwizard.views.common.ViewRenderer;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Locale;

import static org.assertj.core.api.Assertions.*;

class MustacheViewRendererTest extends JerseyTest {
    static {
        BootstrapLogging.bootstrap();
    }

    @Path("/test/")
    @Produces(MediaType.TEXT_HTML)
    public static class ExampleResource {
        @GET
        @Path("/absolute")
        public AbsoluteView showAbsolute() {
            return new AbsoluteView("yay");
        }

        @GET
        @Path("/relative")
        public RelativeView showRelative() {
            return new RelativeView();
        }

        @GET
        @Path("/bad")
        public BadView showBad() {
            return new BadView();
        }

        @GET
        @Path("/error")
        public ErrorView showError() {
            return new ErrorView();
        }

        @GET
        @Path("/csp")
        public CspView showCsp() {
            return new CspView();
        }
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected Application configure() {
        ResourceConfig config = DropwizardResourceConfig.forTesting();
        final ViewRenderer renderer = new MustacheViewRenderer();
        config.register(new ViewMessageBodyWriter(new MetricRegistry(), Collections.singletonList(renderer)));
        config.register(new io.dropwizard.jersey.filter.CspFilter("default-src 'self'; script-src 'nonce-$NONCE';", null));
        config.register(new ViewRenderExceptionMapper());
        config.register(new ExampleResource());
        return config;
    }

    @Test
    void rendersViewsWithAbsoluteTemplatePaths() {
        assertThat(target("/test/absolute").request().get(String.class))
            .isEqualTo("Woop woop. yay\n");
    }

    @Test
    void rendersViewsWithRelativeTemplatePaths() {
        assertThat(target("/test/relative").request().get(String.class))
            .isEqualTo("Ok.\n");
    }

    @Test
    void returnsA500ForViewsWithBadTemplatePaths() {
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> target("/test/bad").request().get(String.class))
            .extracting(WebApplicationException::getResponse)
            .satisfies(response -> assertThat(response.getStatus())
                .isEqualTo(500))
            .satisfies(response -> assertThat(response.readEntity(String.class))
                .isEqualTo(ViewRenderExceptionMapper.TEMPLATE_ERROR_MSG));
    }

    @Test
    void returnsA500ForViewsThatCantCompile() {
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> target("/test/error").request().get(String.class))
            .extracting(WebApplicationException::getResponse)
            .satisfies(response -> assertThat(response.getStatus())
                .isEqualTo(500))
            .satisfies(response -> assertThat(response.readEntity(String.class))
                .isEqualTo(ViewRenderExceptionMapper.TEMPLATE_ERROR_MSG));
    }

    @Test
    void cacheByDefault() {
        MustacheViewRenderer mustacheViewRenderer = new MustacheViewRenderer();
        mustacheViewRenderer.configure(Collections.emptyMap());
        assertThat(mustacheViewRenderer.isUseCache()).isTrue();
    }

    @Test
    void canDisableCache() {
        MustacheViewRenderer mustacheViewRenderer = new MustacheViewRenderer();
        mustacheViewRenderer.configure(Collections.singletonMap("cache", "false"));
        assertThat(mustacheViewRenderer.isUseCache()).isFalse();
    }

    @Test
    void rendersViewsWithCspNonce() {
        try (final jakarta.ws.rs.core.Response response = target("/test/csp").request().get()) {
            assertThat(response.getStatus()).isEqualTo(jakarta.ws.rs.core.Response.Status.OK.getStatusCode());
            final String body = response.readEntity(String.class);
            final String cspHeader = response.getHeaderString("Content-Security-Policy");
            assertThat(cspHeader).startsWith("default-src 'self'; script-src 'nonce-");

            final String prefix = "default-src 'self'; script-src 'nonce-";
            final String suffix = "';";
            final String nonce = cspHeader.substring(prefix.length(), cspHeader.length() - suffix.length());

            assertThat(body).isEqualTo("<script nonce=\"" + nonce + "\">console.log(\"hello\");</script>\n");
        }
    }

    /**
     * Verifies that the CspNonceLookup ThreadLocal is cleared by ViewMessageBodyWriter even when
     * view rendering fails (ViewRenderException path). On thread-pooled servers (Jetty), a nonce
     * left in the ThreadLocal after an error would leak to the next request handled by that thread,
     * potentially allowing a stale nonce to appear in a subsequent template — breaking the
     * per-request nonce guarantee.
     *
     * <p>The guarantee is provided by the {@code finally { CspNonceLookup.remove(); }} block in
     * {@link io.dropwizard.views.common.ViewMessageBodyWriter#writeTo}. This test exercises
     * that path by calling writeTo() with a BadView that has no matching template.
     */
    @Test
    void cspNonceThreadLocalIsClearedAfterFailedRenderViaBodyWriter() throws Exception {
        final MustacheViewRenderer renderer = new MustacheViewRenderer();
        final ViewMessageBodyWriter writer = new ViewMessageBodyWriter(
                new MetricRegistry(), Collections.singletonList(renderer));

        // Manually prime the ThreadLocal to simulate a nonce being set by a previous request
        // on a thread-pool-reused thread (worst-case starting state)
        CspNonceLookup.set("test-nonce-sentinel");

        // BadView has no matching template — writeTo() will throw WebApplicationException,
        // but the finally block in writeTo() must still clear the ThreadLocal
        final BadView badView = new BadView();
        try {
            writer.writeTo(
                    badView,
                    BadView.class,
                    (Type) BadView.class,
                    new Annotation[0],
                    MediaType.TEXT_HTML_TYPE,
                    new MultivaluedHashMap<>(),
                    OutputStream.nullOutputStream());
        } catch (Exception ignored) {
            // Expected path: NullPointerException from null @Context HttpHeaders (headers not
            // injected outside Jersey), OR ViewRenderException → WebApplicationException.
            // Either way the finally-block in ViewMessageBodyWriter.writeTo() has already
            // called CspNonceLookup.remove() before this point.
        }

        // The ThreadLocal must be clean after the failed render
        assertThat(CspNonceLookup.get())
                .as("CspNonceLookup ThreadLocal must be empty after failed render to prevent " +
                    "nonce leaking to subsequent requests on reused threads")
                .isEmpty();
    }
}
