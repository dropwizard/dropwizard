package io.dropwizard.views.freemarker;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.views.ViewMessageBodyWriter;
import io.dropwizard.views.ViewRenderExceptionMapper;
import io.dropwizard.views.ViewRenderer;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class FreemarkerViewRendererTest extends JerseyTest {
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
    protected Application configure() {
        ResourceConfig config = new ResourceConfig();
        final ViewRenderer renderer = new FreemarkerViewRenderer();
        config.register(new ViewMessageBodyWriter(new MetricRegistry(), Collections.singletonList(renderer)));
        config.register(new ExampleResource());
        config.register(new ViewRenderExceptionMapper());
        return config;
    }

    @Test
    public void rendersViewsWithAbsoluteTemplatePaths() throws Exception {
        final String response = target("/test/absolute")
                .request().get(String.class);
        assertThat(response).isEqualTo("Woop woop. yay\n");
    }

    @Test
    public void rendersViewsWithRelativeTemplatePaths() throws Exception {
        final String response = target("/test/relative")
                .request().get(String.class);
        assertThat(response).isEqualTo("Ok.\n");
    }

    @Test
    public void returnsA500ForViewsWithBadTemplatePaths() throws Exception {
        try {
            target("/test/bad")
                    .request().get(String.class);

            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus())
                    .isEqualTo(500);

            assertThat(e.getResponse().readEntity(String.class))
                .isEqualTo(ViewRenderExceptionMapper.TEMPLATE_ERROR_MSG);
        }
    }

    @Test
    public void returnsA500ForViewsThatCantCompile() throws Exception {
        try {
            target("/test/error").request().get(String.class);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus())
                    .isEqualTo(500);

            assertThat(e.getResponse().readEntity(String.class))
                    .isEqualTo(ViewRenderExceptionMapper.TEMPLATE_ERROR_MSG);
        }
    }

    @Test
    public void rendersViewsUsingUnsafeInputWithAutoEscapingEnabled() throws Exception {
        final String unsafe = "<script>alert(\"hello\")</script>";
        final Response response = target("/test/auto-escaping")
            .request().post(Entity.form(new Form("input", unsafe)));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getHeaderString("content-type")).isEqualToIgnoringCase(MediaType.TEXT_HTML);
        assertThat(response.readEntity(String.class)).doesNotContain(unsafe);
    }
}
