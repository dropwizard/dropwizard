package com.codahale.dropwizard.views.freemarker;

import com.codahale.dropwizard.views.ModelContainer;
import com.codahale.dropwizard.views.View;
import com.codahale.dropwizard.views.ViewRenderer;

import com.google.common.base.Charsets;

import com.sun.jersey.api.container.MappableContainerException;

import freemarker.cache.TemplateLoader;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.ws.rs.WebApplicationException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import java.nio.charset.Charset;
import java.util.Locale;

/**
 * A {@link ViewRenderer} which renders Freemarker ({@code .ftl}) templates.
 */
public class FreemarkerViewRenderer implements ViewRenderer {
    private final Configuration configuration;

    /**
     * Returns default Freemarker configuration, which can then be modified to suit user's needs
     * and passed as an argument to the constructor.
     *
     * @return default Freemarker configuration
     */
    public static Configuration getDefaultConfiguration() {
        final Configuration configuration = new Configuration();
        configuration.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);
        configuration.setObjectWrapper(new DefaultObjectWrapper());
        configuration.loadBuiltInEncodingMap();
        configuration.setDefaultEncoding(Charsets.UTF_8.name());
        configuration.setTemplateLoader(new DefaultTemplateLoader());
        return configuration;
    }

    /**
     * Creates FreemarkerViewRenderer instance with default configuration.
     */
    public FreemarkerViewRenderer() {
        this(getDefaultConfiguration());
    }

    /**
     * Creates FreemarkerViewRenderer instance with custom configuration.
     */
    public FreemarkerViewRenderer(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRenderable(View view) {
        return view.getTemplateName().endsWith(".ftl");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(View view,
                       Locale locale,
                       OutputStream output) throws IOException, WebApplicationException {
        try {
            final Charset charset = view.getCharset().or(Charset.forName(configuration.getEncoding(locale)));
            final Object model = view instanceof ModelContainer
                                 ? ((ModelContainer) view).getModel()
                                 : view;
            final Template template = configuration.getTemplate(view.getTemplateName(), locale, charset.name());
            final Environment env = template.createProcessingEnvironment(
                                model, new OutputStreamWriter(output, template.getEncoding()));
            extendEnvironment(env);
            env.process();
        }
        catch (TemplateException e) {
            throw new MappableContainerException(e);
        }
    }

    /**
     * Allows end users to extend Freemarker processing environment with custom variables and
     * functions for each request.
     *
     * @param env Freemarker environment to extend
     */
    protected void extendEnvironment(Environment env) {}

    /**
     * Default template loader. Loads templates as classpath resources using the classloader
     * that loaded this class.
     */
    private static class DefaultTemplateLoader
            implements TemplateLoader {

        @Override
        public Object findTemplateSource(String name) throws IOException {
            return getClass().getClassLoader().getResourceAsStream(name);
        }

        @Override
        public long getLastModified(Object templateSource) {
            return -1;
        }

        public Reader getReader(Object templateSource, String encoding) throws IOException {
            return new InputStreamReader((InputStream) templateSource, encoding);
        }

        @Override
        public void closeTemplateSource(Object templateSource) throws IOException {
            ((InputStream) templateSource).close();
        }
    }
}
