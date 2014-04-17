package io.dropwizard.views.trimou;

import static org.fest.assertions.api.Assertions.assertThat;
import io.dropwizard.logging.LoggingFactory;
import io.dropwizard.views.ViewMessageBodyWriter;
import io.dropwizard.views.ViewRenderer;

import java.util.Locale;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.junit.Test;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;

/**
 *
 * @author Martin Kouba
 */
public class TrimouViewRendererTest extends JerseyTest {

    static {
        LoggingFactory.bootstrap();
    }

    @Path("/test/")
    @Produces(MediaType.TEXT_HTML)
    public static class TestResource {

        @GET
        @Path("/hello")
        public HelloView hello() {
            return new HelloView("Martin");
        }

        @GET
        @Path("/relative")
        public RelativeView relative() {
            return new RelativeView("Martin");
        }

        @GET
        @Path("/localized")
        public LocalizedView localized() {
            return new LocalizedView();
        }

        @GET
        @Path("/complex")
        public ComplexView complex() {
            return new ComplexView("Martin");
        }

    }

    @Override
    protected AppDescriptor configure() {
        final DefaultResourceConfig config = new DefaultResourceConfig();
        final ViewRenderer renderer = new TrimouViewRenderer.Builder().build();
        config.getSingletons().add(new ViewMessageBodyWriter(new MetricRegistry(), ImmutableList.of(renderer)));
        config.getSingletons().add(new TestResource());
        return new LowLevelAppDescriptor.Builder(config).build();
    }

    @Test
    public void testSimpleView() throws Exception {
        final String response = client().resource(getBaseURI() + "test/hello").get(String.class).trim();
        assertThat(response).isEqualTo("Hello Martin!");
    }

    @Test
    public void testRelativeView() throws Exception {
        final String response = client().resource(getBaseURI() + "test/relative").get(String.class).trim();
        assertThat(response).isEqualTo("Hello Martin!");
    }

    @Test
    public void testLocallizedView() throws Exception {
        assertThat(client().resource(getBaseURI() + "test/localized").acceptLanguage(new Locale("cs")).get(String.class).trim()).isEqualTo("Ahoj!");
        assertThat(client().resource(getBaseURI() + "test/localized").acceptLanguage(Locale.GERMANY).get(String.class).trim()).isEqualTo("Hallo!");
        assertThat(client().resource(getBaseURI() + "test/localized").acceptLanguage(Locale.FRANCE).get(String.class).trim()).isEqualTo("Salut!");
    }

    @Test
    public void testComplexView() throws Exception {
        final String response = client().resource(getBaseURI() + "test/complex").get(String.class).trim();
        assertThat(response).isEqualTo("Hello Martin again!");
    }

}
