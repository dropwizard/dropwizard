package com.yammer.dropwizard.views.freemarker;

import com.google.common.base.Charsets;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sun.jersey.api.container.ContainerException;
import com.yammer.dropwizard.views.View;
import com.yammer.dropwizard.views.ViewRenderer;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Locale;

public class FreemarkerViewRenderer implements ViewRenderer {
    private static class TemplateLoader extends CacheLoader<Class<?>, Configuration> {
        @Override
        public Configuration load(Class<?> key) throws Exception {
            final Configuration configuration = new Configuration();
            configuration.setObjectWrapper(new DefaultObjectWrapper());
            configuration.loadBuiltInEncodingMap();
            configuration.setDefaultEncoding(Charsets.UTF_8.name());
            configuration.setClassForTemplateLoading(key, "/");
            return configuration;
        }
    }

    private final LoadingCache<Class<?>, Configuration> configurationCache;

    public FreemarkerViewRenderer() {
        this.configurationCache = CacheBuilder.newBuilder()
                                              .concurrencyLevel(128)
                                              .build(new TemplateLoader());
    }

    @Override
    public boolean isRenderable(View view) {
        return view.getTemplateName().endsWith(".ftl");
    }

    @Override
    public void render(View view,
                       Locale locale,
                       OutputStream output) throws IOException, WebApplicationException {
        try {
            final Configuration configuration = configurationCache.getUnchecked(view.getClass());
            final Template template = configuration.getTemplate(view.getTemplateName(), locale);
            template.process(view, new OutputStreamWriter(output, template.getEncoding()));
        } catch (TemplateException e) {
            throw new ContainerException(e);
        }
    }

}
