package io.dropwizard.views.mustache;

import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheException;
import com.github.mustachejava.MustacheFactory;
import com.google.common.base.Charsets;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import io.dropwizard.views.View;
import io.dropwizard.views.ViewRenderer;

import javax.ws.rs.WebApplicationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * A {@link ViewRenderer} which renders Mustache ({@code .mustache}) templates.
 */
public class MustacheViewRenderer implements ViewRenderer {
    private final LoadingCache<Class<? extends View>, MustacheFactory> factories;

    public MustacheViewRenderer() {
        this.factories = CacheBuilder.newBuilder()
                                     .build(new CacheLoader<Class<? extends View>, MustacheFactory>() {
                                         @Override
                                         public MustacheFactory load(Class<? extends View> key) throws Exception {
                                             return new PerClassMustacheFactory(key);
                                         }
                                     });
    }

    @Override
    public boolean isRenderable(View view) {
        return view.getTemplateName().endsWith(getSuffix());
    }

    @Override
    public void render(View view, Locale locale, OutputStream output) throws IOException, WebApplicationException {
        try {
            final Mustache template = factories.get(view.getClass())
                                               .compile(view.getTemplateName());
            final Charset charset = view.getCharset().or(Charsets.UTF_8);
            try (OutputStreamWriter writer = new OutputStreamWriter(output, charset)) {
                template.execute(writer, view);
            }
        } catch (ExecutionException | UncheckedExecutionException | MustacheException ignored) {
            throw new FileNotFoundException("Template " + view.getTemplateName() + " not found.");
        }
    }

    @Override
    public void configure(Map<String, String> options) {}

    @Override
    public String getSuffix() {
        return ".mustache";
    }
}
