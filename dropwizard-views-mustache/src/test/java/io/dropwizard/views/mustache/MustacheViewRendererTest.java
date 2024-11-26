package io.dropwizard.views.mustache;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.logging.common.BootstrapLogging;
import io.dropwizard.views.common.ViewMessageBodyWriter;
import io.dropwizard.views.common.ViewRenderExceptionMapper;
import io.dropwizard.views.common.ViewRenderer;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

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
}
