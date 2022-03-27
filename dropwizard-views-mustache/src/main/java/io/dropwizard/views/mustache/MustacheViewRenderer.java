package io.dropwizard.views.mustache;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.resolver.FileSystemResolver;
import io.dropwizard.views.common.View;
import io.dropwizard.views.common.ViewRenderException;
import io.dropwizard.views.common.ViewRenderer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * A {@link ViewRenderer} which renders Mustache ({@code .mustache}) templates.
 */
public class MustacheViewRenderer implements ViewRenderer {
    private static final Pattern FILE_PATTERN = Pattern.compile("\\.mustache");
    private final LoadingCache<Class<? extends View>, MustacheFactory> factories;
    private boolean useCache = true;
    private Optional<File> fileRoot = Optional.empty();

    public MustacheViewRenderer() {
        this.factories = Caffeine.newBuilder().build(new CacheLoader<Class<? extends View>, MustacheFactory>() {
            @Override
            public MustacheFactory load(Class<? extends View> key) throws Exception {
                return createNewMustacheFactory(key);
            }
        });
    }

    @Override
    public boolean isRenderable(View view) {
        return FILE_PATTERN.matcher(view.getTemplateName()).find();
    }

    @Override
    public void render(View view, Locale locale, OutputStream output) throws IOException {
        try {
            final MustacheFactory mustacheFactory = useCache ? factories.get(view.getClass())
                    : createNewMustacheFactory(view.getClass());
            final Mustache template = mustacheFactory.compile(view.getTemplateName());
            final Charset charset = view.getCharset().orElse(StandardCharsets.UTF_8);
            try (OutputStreamWriter writer = new OutputStreamWriter(output, charset)) {
                template.execute(writer, view);
            }
        } catch (Throwable e) {
            throw new ViewRenderException("Mustache template error: " + view.getTemplateName(), e);
        }
    }

    @Override
    public void configure(Map<String, String> options) {
        useCache = Optional.ofNullable(options.get("cache")).map(Boolean::parseBoolean).orElse(true);
        fileRoot = Optional.ofNullable(options.get("fileRoot")).map(File::new);
    }

    boolean isUseCache() {
        return useCache;
    }

    @Override
    public String getConfigurationKey() {
        return "mustache";
    }

    private MustacheFactory createNewMustacheFactory(Class<? extends View> key) {
        return new DefaultMustacheFactory(
                fileRoot.isPresent() ? new FileSystemResolver(fileRoot.get()) : new PerClassMustacheResolver(key));
    }

}
