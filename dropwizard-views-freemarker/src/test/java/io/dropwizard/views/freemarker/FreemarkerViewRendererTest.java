package io.dropwizard.views.freemarker;

import com.codahale.metrics.MetricRegistry;
import freemarker.template.Configuration;
import io.dropwizard.logging.common.BootstrapLogging;
import io.dropwizard.views.common.ViewMessageBodyWriter;
import io.dropwizard.views.common.ViewRenderExceptionMapper;
import io.dropwizard.views.common.ViewRenderer;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class FreemarkerViewRendererTest extends JerseyTest {
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

        @POST
        @Path("/auto-escaping")
        public AutoEscapingView showUserInputSafely(@FormParam("input") String userInput) {
            return new AutoEscapingView(userInput);
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
        ResourceConfig config = new ResourceConfig();
        final ViewRenderer renderer = new FreemarkerViewRenderer(Configuration.VERSION_2_3_30);
        config.register(new ViewMessageBodyWriter(new MetricRegistry(), Collections.singletonList(renderer)));
        config.register(new ExampleResource());
        config.register(new ViewRenderExceptionMapper());
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
        Invocation.Builder request = target("/test/bad").request();
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> request.get(String.class))
            .extracting(WebApplicationException::getResponse)
            .satisfies(response -> assertThat(response.getStatus())
                .isEqualTo(500))
            .satisfies(response -> assertThat(response.readEntity(String.class))
                .isEqualTo(ViewRenderExceptionMapper.TEMPLATE_ERROR_MSG));
    }

    @Test
    @Disabled("Flaky on JUnit5")
    void returnsA500ForViewsThatCantCompile() {
        Invocation.Builder request = target("/test/error").request();
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> request.get(String.class))
            .extracting(WebApplicationException::getResponse)
            .satisfies(response -> assertThat(response.getStatus())
                .isEqualTo(500))
            .satisfies(response -> assertThat(response.readEntity(String.class))
                .isEqualTo(ViewRenderExceptionMapper.TEMPLATE_ERROR_MSG));
    }

    @Test
    void rendersViewsUsingUnsafeInputWithAutoEscapingEnabled() {
        final String unsafe = "<script>alert(\"hello\")</script>";
        Entity<Form> entity = Entity.form(new Form("input", unsafe));
        try (final Response response = target("/test/auto-escaping").request().post(entity)) {
            assertThat(response)
                .satisfies(r -> assertThat(r.getStatus()).isEqualTo(Response.Status.OK.getStatusCode()))
                .satisfies(r -> assertThat(r.getHeaderString("content-type")).isEqualToIgnoringCase(MediaType.TEXT_HTML))
                .satisfies(r -> assertThat(r.readEntity(String.class)).doesNotContain(unsafe));
        }
    }
}
