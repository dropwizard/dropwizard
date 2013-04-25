package com.yammer.dropwizard.views.handlebars;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.cache.HighConcurrencyTemplateCache;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.google.common.base.Charsets;
import com.yammer.dropwizard.views.View;
import com.yammer.dropwizard.views.ViewRenderer;

import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Locale;

/**
 * User: Santanu Sinha (santanu.sinha@flipkart.com)
 * Date: 25/04/13
 * Time: 10:55 AM
 */
public class HandlebarViewRenderer implements ViewRenderer{
    private TemplateLoader loader;
    private Handlebars handle;

    public HandlebarViewRenderer() {
        loader = new ClassPathTemplateLoader();
        handle = new Handlebars(loader, new HighConcurrencyTemplateCache());
    }

    @Override
    public boolean isRenderable(View view) {
        return view.getTemplateName().endsWith(".hbs");
    }

    @Override
    public void render(View view, Locale locale, OutputStream output) throws IOException, WebApplicationException {
        final OutputStreamWriter writer = new OutputStreamWriter(output, view.getCharset().or(Charsets.UTF_8));
        try {
            final Template template = handle.compile(view.getTemplateName().replaceAll("[.]hbs$",""));
            template.apply(view, writer);
        } finally {
            writer.close();
        }
    }
}
