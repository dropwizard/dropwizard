package com.yammer.dropwizard.views.tests;

import com.sun.jersey.core.util.StringKeyIgnoreCaseMultivaluedMap;
import com.yammer.dropwizard.views.MyOtherView;
import com.yammer.dropwizard.views.MyView;
import com.yammer.dropwizard.views.ViewMessageBodyWriter;
import com.yammer.dropwizard.views.example.BadView;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.lang.annotation.Annotation;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class ViewMessageBodyWriterTest {
    private static final Annotation[] NONE = {};
    
    private final HttpHeaders headers = mock(HttpHeaders.class);
    private final ViewMessageBodyWriter writer = new ViewMessageBodyWriter(headers);

    @Test
    public void canWriteViews() throws Exception {
        assertThat(writer.isWriteable(MyView.class, MyView.class, NONE, MediaType.TEXT_HTML_TYPE),
                   is(true));
    }

    @Test
    public void cantWriteNonViews() throws Exception {
        assertThat(writer.isWriteable(String.class, String.class, NONE, MediaType.TEXT_HTML_TYPE),
                   is(false));
    }

    @Test
    public void writesViews() throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        final MyView view = new MyView("HONK");

        writer.writeTo(view,
                       MyView.class,
                       null,
                       NONE,
                       MediaType.TEXT_HTML_TYPE,
                       new StringKeyIgnoreCaseMultivaluedMap<Object>(),
                       output);
        
        assertThat(output.toString(),
                   is("Woop woop. HONK\n"));
    }

    @Test
    public void handlesRelativeTemplatePaths() throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        final MyOtherView view = new MyOtherView();

        writer.writeTo(view,
                       MyOtherView.class,
                       null,
                       NONE,
                       MediaType.TEXT_HTML_TYPE,
                       new StringKeyIgnoreCaseMultivaluedMap<Object>(),
                       output);

        assertThat(output.toString(),
                   is("Ok.\n"));
    }

    @Test
    public void writesErrorMessagesForBadTemplates() throws Exception {
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

            assertThat(response.getStatus(),
                       is(500));
            
            assertThat((String) response.getEntity(),
                       is("<html><head><title>Missing Template</title></head><body><h1>Missing Template</h1><p>Template /woo-oo-ahh.txt not found.</p></body></html>"));
        }
    }
}
