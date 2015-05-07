package io.dropwizard.views.freemarker;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableList;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.views.View;
import io.dropwizard.views.ViewMessageBodyWriter;
import io.dropwizard.views.ViewRenderer;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static org.assertj.core.api.Assertions.assertThat;

public class MultipleContentTypeTest extends JerseyTest {
    static {
        BootstrapLogging.bootstrap();
    }

    @Override
    protected Application configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        final ViewRenderer renderer = new FreemarkerViewRenderer();
        return DropwizardResourceConfig.forTesting(new MetricRegistry())
                .register(new ViewMessageBodyWriter(new MetricRegistry(), ImmutableList.of(renderer)))
                .register(new InfoMessageBodyWriter())
                .register(new ExampleResource());
    }

    @Test
    public void testJsonContentType() {
        final Response response = target("/").request().accept(MediaType.APPLICATION_JSON_TYPE).get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isEqualTo("{\"title\":\"Title#TEST\",\"content\":\"Content#TEST\"}");
    }

    @Test
    public void testHtmlContentType() {
        final Response response = target("/").request().accept(MediaType.TEXT_HTML_TYPE).get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class))
                .contains("Breaking news")
                .contains("<h1>Title#TEST</h1>")
                .contains("<p>Content#TEST</p>");
    }

    @Test
    public void testOnlyJsonContentType() {
        final Response response = target("/json").request().accept(MediaType.APPLICATION_JSON_TYPE).get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isEqualTo("{\"title\":\"Title#TEST\",\"content\":\"Content#TEST\"}");
    }

    @Test
    public void testOnlyHtmlContentType() {
        final Response response = target("/html").request().accept(MediaType.TEXT_HTML_TYPE).get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class))
                .contains("Breaking news")
                .contains("<h1>Title#TEST</h1>")
                .contains("<p>Content#TEST</p>");
    }

    @Path("/")
    public static class ExampleResource {
        @GET
        @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_HTML})
        public Response getInfoCombined() {
            final Info info = new Info("Title#TEST", "Content#TEST");
            return Response.ok(info).build();
        }

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @Path("json")
        public Response getInfoJson() {
            final Info info = new Info("Title#TEST", "Content#TEST");
            return Response.ok(info).build();
        }

        @GET
        @Produces(MediaType.TEXT_HTML)
        @Path("html")
        public Response getInfoHtml() {
            final Info info = new Info("Title#TEST", "Content#TEST");
            return Response.ok(info).build();
        }
    }

    public static class Info extends View {
        private final String title;
        private final String content;

        public Info(String title, String content) {
            super("/issue627.ftl");
            this.title = title;
            this.content = content;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }
    }

    @Provider
    @Produces(MediaType.APPLICATION_JSON)
    public class InfoMessageBodyWriter implements MessageBodyWriter<Info> {
        @Override
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return Info.class.isAssignableFrom(type);
        }

        @Override
        public long getSize(Info info, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        @Override
        public void writeTo(Info info, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
                throws IOException, WebApplicationException {
            Jackson.newObjectMapper().writeValue(entityStream, info);
        }
    }
}
