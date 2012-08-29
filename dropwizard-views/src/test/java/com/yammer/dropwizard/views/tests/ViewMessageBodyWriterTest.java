package com.yammer.dropwizard.views.tests;

import com.sun.jersey.core.util.StringKeyIgnoreCaseMultivaluedMap;
import com.yammer.dropwizard.views.MyOtherView;
import com.yammer.dropwizard.views.MyView;
import com.yammer.dropwizard.views.ViewMessageBodyWriter;
import com.yammer.dropwizard.views.ViewRenderException;
import com.yammer.dropwizard.views.example.BadView;
import com.yammer.dropwizard.views.example.MustacheView;
import com.yammer.dropwizard.views.example.UnknownView;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.lang.annotation.Annotation;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.mock;

public class ViewMessageBodyWriterTest {
    private static final Annotation[] NONE = { };

    private final HttpHeaders headers = mock(HttpHeaders.class);
    private final ViewMessageBodyWriter writer = new ViewMessageBodyWriter(headers);

    @Test
    public void canWriteViews() throws Exception {
        assertThat(writer.isWriteable(MyView.class, MyView.class, NONE, MediaType.TEXT_HTML_TYPE))
                .isTrue();
    }

    @Test
    public void cantWriteNonViews() throws Exception {
        assertThat(writer.isWriteable(String.class, String.class, NONE, MediaType.TEXT_HTML_TYPE))
                .isFalse();
    }

    @Test
    public void writesFreemarkerViews() throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        final MyView view = new MyView("HONK");

        writer.writeTo(view,
                       MyView.class,
                       null,
                       NONE,
                       MediaType.TEXT_HTML_TYPE,
                       new StringKeyIgnoreCaseMultivaluedMap<Object>(),
                       output);

        assertThat(output.toString())
                .isEqualTo(String.format("Woop woop. HONK\n"));
    }

    @Test
    public void writesMustacheViews() throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        final MustacheView view = new MustacheView("Stranger");

        writer.writeTo(view,
                       MustacheView.class,
                       null,
                       NONE,
                       MediaType.TEXT_HTML_TYPE,
                       new StringKeyIgnoreCaseMultivaluedMap<Object>(),
                       output);

        assertThat(output.toString())
                .isEqualTo("Hello Stranger!\nWoo!\n\n");
    }

    @Test
    public void handlesRelativeFreemarkerTemplatePaths() throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        final MyOtherView view = new MyOtherView();

        writer.writeTo(view,
                       MyOtherView.class,
                       null,
                       NONE,
                       MediaType.TEXT_HTML_TYPE,
                       new StringKeyIgnoreCaseMultivaluedMap<Object>(),
                       output);

        assertThat(output.toString())
                .isEqualTo(String.format("Ok.\n"));
    }

    @Test
    public void writesErrorMessagesForMissingTemplates() throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        final BadView view = new BadView();

        try {
            writer.writeTo(view,
                           MyView.class,
                           null,
                           NONE,
                           MediaType.TEXT_HTML_TYPE,
                           new StringKeyIgnoreCaseMultivaluedMap<Object>(),
                           output);
        } catch (WebApplicationException e) {
            final Response response = e.getResponse();

            assertThat(response.getStatus())
                    .isEqualTo(500);

            assertThat((String) response.getEntity())
                    .isEqualTo("<html><head><title>Missing Template</title></head><body><h1>Missing Template</h1><p>Template /woo-oo-ahh.txt.ftl not found.</p></body></html>");
        }
    }

    @Test
    public void throwsExceptionsForUnknownTemplateTypes() throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final UnknownView view = new UnknownView();
        try {
            writer.writeTo(view,
                           MyView.class,
                           null,
                           NONE,
                           MediaType.TEXT_HTML_TYPE,
                           new StringKeyIgnoreCaseMultivaluedMap<Object>(),
                           output);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (ViewRenderException e) {
            assertThat(e.getMessage())
                    .isEqualTo("Unable to find a renderer for /com/yammer/dropwizard/views/example/misterpoops.jjsjk");
        }
    }
}
