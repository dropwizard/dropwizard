package com.yammer.dropwizard.templates;

import com.google.common.base.Charsets;
import com.sun.jersey.api.container.ContainerException;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

@Provider
@Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_XHTML_XML})
public class ViewMessageBodyWriter implements MessageBodyWriter<View<?>> {
    private static final String MISSING_TEMPLATE_MSG =
            "<html>" +
                "<head><title>Missing Template</title></head>" +
                "<body><h1>Missing Template</h1><p>{0}</p></body>" +
            "</html>";

    private final Configuration configuration;

    @Context
    @SuppressWarnings("UnusedDeclaration")
    private HttpHeaders headers;

    public ViewMessageBodyWriter() {
        this.configuration = new Configuration();
        configuration.setObjectWrapper(new DefaultObjectWrapper());
        configuration.setDefaultEncoding(Charsets.UTF_8.name());
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return View.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(View<?> t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(View<?> t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        try {
            configuration.setClassForTemplateLoading(type, "/");
            final Template template = configuration.getTemplate(t.getTemplateName(),
                                                                detectLocale(headers));
            template.process(t.getModel(), new OutputStreamWriter(entityStream));
        } catch (TemplateException e) {
            throw new ContainerException(e);
        } catch (FileNotFoundException e) {
            final String msg = MessageFormat.format(MISSING_TEMPLATE_MSG, e.getMessage());
            throw new WebApplicationException(Response.serverError()
                                                      .type(MediaType.TEXT_HTML_TYPE)
                                                      .entity(msg)
                                                      .build());
        }
    }

    private Locale detectLocale(HttpHeaders headers) {
        final List<Locale> languages = headers.getAcceptableLanguages();
        for (Locale locale : languages) {
            if (!locale.toString().contains("*")) { // Freemarker doesn't do wildcards well
                return locale;
            }
        }
        return Locale.getDefault();
    }
}
