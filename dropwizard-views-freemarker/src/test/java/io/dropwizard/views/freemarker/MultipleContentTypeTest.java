package io.dropwizard.views.freemarker;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.views.View;
import io.dropwizard.views.ViewMessageBodyWriter;
import io.dropwizard.views.ViewRenderer;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
import java.util.Collections;
import java.util.Objects;
import java.util.StringJoiner;

import static org.assertj.core.api.Assertions.assertThat;

public class MultipleContentTypeTest extends JerseyTest {
    static {
        BootstrapLogging.bootstrap();
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
        final ViewRenderer renderer = new FreemarkerViewRenderer();
        return DropwizardResourceConfig.forTesting()
                .register(new ViewMessageBodyWriter(new MetricRegistry(), Collections.singletonList(renderer)))
                .register(new InfoMessageBodyWriter())
                .register(new ExampleResource());
    }

    @Test
    public void testJsonContentType() {
        final Response response = target("/").request().accept(MediaType.APPLICATION_JSON_TYPE).get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Info.class)).isEqualTo(new Info("Title#TEST", "Content#TEST"));
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
        assertThat(response.readEntity(Info.class)).isEqualTo(new Info("Title#TEST", "Content#TEST"));
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

        public Info(@JsonProperty("title") String title, @JsonProperty("content") String content) {
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Info that = (Info) o;

            return Objects.equals(this.title, that.title) && Objects.equals(this.content, that.content);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, content);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
                    .add("title = " + title)
                    .add("content = " + content)
                    .toString();
        }
    }

    @Provider
    @Produces(MediaType.APPLICATION_JSON)
    public static class InfoMessageBodyWriter implements MessageBodyWriter<Info> {
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
