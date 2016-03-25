package io.dropwizard.views.freemarker;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
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
 * A {@link ViewRenderer} which renders Freemarker ({@code .ftl}) templates.
 */
public class FreemarkerViewRenderer implements ViewRenderer {

    private static final Version FREEMARKER_VERSION = Configuration.getVersion();
    private final TemplateLoader loader;

    private static class TemplateLoader extends CacheLoader<Class<?>, Configuration> {
        private Map<String, String> baseConfig = ImmutableMap.of();
        @Override
        public Configuration load(Class<?> key) throws Exception {
            final Configuration configuration = new Configuration(FREEMARKER_VERSION);
            configuration.setObjectWrapper(new DefaultObjectWrapperBuilder(FREEMARKER_VERSION).build());
            configuration.loadBuiltInEncodingMap();
            configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
            configuration.setClassForTemplateLoading(key, "/");
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

    public FreemarkerViewRenderer() {
        this.loader = new TemplateLoader();
        this.configurationCache = CacheBuilder.newBuilder()
                                              .concurrencyLevel(128)
                                              .build(loader);
    }

    @Override
    public boolean isRenderable(View view) {
        return view.getTemplateName().endsWith(getSuffix());
    }

    @Override
    public void render(View view,
                       Locale locale,
                       OutputStream output) throws IOException {
        try {
            final Configuration configuration = configurationCache.getUnchecked(view.getClass());
            final Charset charset = view.getCharset().orElseGet(() -> Charset.forName(configuration.getEncoding(locale)));
            final Template template = configuration.getTemplate(view.getTemplateName(), locale, charset.name());
            template.process(view, new OutputStreamWriter(output, template.getEncoding()));
        } catch (TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    public void configure(Map<String, String> baseConfig) {
        this.loader.setBaseConfig(baseConfig);
    }

    @Override
    public String getSuffix() {
        return ".ftl";
    }
}
