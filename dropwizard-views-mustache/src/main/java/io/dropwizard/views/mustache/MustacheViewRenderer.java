package io.dropwizard.views.mustache;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.dropwizard.views.View;
import io.dropwizard.views.ViewRenderer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

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
                                             return new DefaultMustacheFactory(new PerClassMustacheResolver(key));
                                         }
                                     });
    }

    @Override
    public boolean isRenderable(View view) {
        return view.getTemplateName().endsWith(getSuffix());
    }

    @Override
    public void render(View view, Locale locale, OutputStream output) throws IOException {
        try {
            final Mustache template = factories.get(view.getClass())
                                               .compile(view.getTemplateName());
            final Charset charset = view.getCharset().orElse(StandardCharsets.UTF_8);
            try (OutputStreamWriter writer = new OutputStreamWriter(output, charset)) {
                template.execute(writer, view);
            }
        } catch (Throwable e) {
            throw new RuntimeException("Mustache template error: " + view.getTemplateName(), e);
        }
    }

    @Override
    public void configure(Map<String, String> options) {

    }

    @Override
    public String getSuffix() {
        return ".mustache";
    }
}
