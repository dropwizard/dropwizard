package io.dropwizard.views.freemarker;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import freemarker.core.HTMLOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import freemarker.template.Version;
import io.dropwizard.views.common.View;
import io.dropwizard.views.common.ViewRenderException;
import io.dropwizard.views.common.ViewRenderer;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A {@link ViewRenderer} which renders Freemarker ({@code .ftl, .ftlh or .ftlx}) templates.
 */
public class FreemarkerViewRenderer implements ViewRenderer {
    private static final Pattern FILE_PATTERN = Pattern.compile("\\.ftl[hx]?");
    private final TemplateLoader loader;

    private static class TemplateLoader implements CacheLoader<Class<?>, Configuration> {
        private final Version incompatibleImprovementsVersion;
        private Map<String, String> baseConfig = Collections.emptyMap();

        private TemplateLoader(Version incompatibleImprovementsVersion) {
            this.incompatibleImprovementsVersion = incompatibleImprovementsVersion;
        }

        @Override
        public Configuration load(@NonNull Class<?> key) throws Exception {
            final Configuration configuration = new Configuration(incompatibleImprovementsVersion);
            configuration.setObjectWrapper(new DefaultObjectWrapperBuilder(incompatibleImprovementsVersion).build());
            configuration.loadBuiltInEncodingMap();
            configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
            configuration.setClassForTemplateLoading(key, "/");
            // setting the outputformat implicitly enables auto escaping
            configuration.setOutputFormat(HTMLOutputFormat.INSTANCE);
            for (Map.Entry<String, String> entry : baseConfig.entrySet()) {
                configuration.setSetting(entry.getKey(), entry.getValue());
            }
            return configuration;
        }

        void setBaseConfig(Map<String, String> baseConfig) {
            this.baseConfig = baseConfig;
        }
    }

    private final LoadingCache<Class<?>, Configuration> configurationCache;

    /**
     * @deprecated Use {@link #FreemarkerViewRenderer(Version)} instead.
     */
    @Deprecated
    public FreemarkerViewRenderer() {
        this(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
    }

    /**
     * @param incompatibleImprovementsVersion FreeMarker version number for backward compatible bug fixes and improvements.
     *                                        See {@link Configuration#Configuration(Version)} for more information.
     */
    public FreemarkerViewRenderer(Version incompatibleImprovementsVersion) {
        this.loader = new TemplateLoader(incompatibleImprovementsVersion);
        this.configurationCache = Caffeine.newBuilder().build(loader);
    }

    @Override
    public boolean isRenderable(View view) {
        return FILE_PATTERN.matcher(view.getTemplateName()).find();
    }

    @Override
    public void render(View view,
                       Locale locale,
                       OutputStream output) throws IOException {
        final Configuration configuration = configurationCache.get(view.getClass());
        if (configuration == null) {
            throw new ViewRenderException("Couldn't find view class " + view.getClass());
        }

        try {
            final Charset charset = view.getCharset().orElseGet(() -> Charset.forName(configuration.getEncoding(locale)));
            final Template template = configuration.getTemplate(view.getTemplateName(), locale, charset.name());
            template.process(view, new OutputStreamWriter(output, template.getEncoding()));
        } catch (Exception e) {
            throw new ViewRenderException(e);
        }
    }

    @Override
    public void configure(Map<String, String> baseConfig) {
        this.loader.setBaseConfig(baseConfig);
    }

    @Override
    public String getConfigurationKey() {
        return "freemarker";
    }
}
