package io.dropwizard.views.trimou;

import io.dropwizard.views.View;
import io.dropwizard.views.ViewRenderer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Locale;

import javax.ws.rs.WebApplicationException;

import org.apache.commons.lang3.StringUtils;
import org.trimou.Mustache;
import org.trimou.engine.MustacheEngine;
import org.trimou.engine.MustacheEngineBuilder;
import org.trimou.engine.config.EngineConfigurationKey;
import org.trimou.exception.MustacheException;

/**
 * A {@link ViewRenderer} for Trimou, an extensible Mustache implementation with helper API (inspired by Handlebars).
 *
 * Localized template files are supported by default, however it could be disabled to improve performance.
 *
 * @author Martin Kouba
 * @see Builder
 */
public class TrimouViewRenderer implements ViewRenderer {

    public static final String DEFAULT_SUFFIX = ".trimou";

    private final MustacheEngine engine;

    private final String suffix;

    private final boolean hasLocalizedTemplates;

    /**
     *
     * @param engine
     * @param suffix
     * @param hasLocalizedTemplates
     */
    private TrimouViewRenderer(MustacheEngine engine, String suffix, boolean hasLocalizedTemplates) {
        this.engine = engine;
        this.suffix = suffix;
        this.hasLocalizedTemplates = hasLocalizedTemplates;
    }

    @Override
    public boolean isRenderable(View view) {
        return view.getTemplateName().endsWith(suffix);
    }

    @Override
    public void render(View view, Locale locale, OutputStream output) throws IOException, WebApplicationException {

        Mustache template = null;

        if (hasLocalizedTemplates && locale != null) {
            // First try the Locale
            template = engine.getMustache(getLocalizedTemplateName(view.getTemplateName(), locale.toString()));
            if (template == null) {
                // Then only the language
                template = engine.getMustache(getLocalizedTemplateName(view.getTemplateName(), locale.getLanguage()));
            }
        }

        if (template == null) {
            template = engine.getMustache(view.getTemplateName());
        }

        if (template == null) {
            throw new FileNotFoundException("Template not found: " + view.getTemplateName());
        }

        final Writer writer = new OutputStreamWriter(output, engine.getConfiguration().getStringPropertyValue(EngineConfigurationKey.DEFAULT_FILE_ENCODING));

        try {
            template.render(writer, view);
            writer.flush();
        } catch (MustacheException e) {
            throw new IOException(e);
        }
    }

    private String getLocalizedTemplateName(String templateName, String localePart) {
        return StringUtils.removeEnd(templateName, suffix) + "_" + localePart + suffix;
    }

    public static class Builder {

        private String suffix = DEFAULT_SUFFIX;

        private boolean hasLocalizedTemplates = true;

        public Builder setSuffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        public Builder setHasLocalizedTemplates(boolean hasLocalizedTemplates) {
            this.hasLocalizedTemplates = hasLocalizedTemplates;
            return this;
        }

        public TrimouViewRenderer build() {
            return build(MustacheEngineBuilder.newBuilder());
        }

        public TrimouViewRenderer build(MustacheEngineBuilder builder) {
            return build(builder.addTemplateLocator(new DropwizardViewTemplateLocator(10)).addTemplateLocator(new DropwizardFragmentTemplateLocator(9, suffix))
                    .build());
        }

        public TrimouViewRenderer build(MustacheEngine engine) {
            return new TrimouViewRenderer(engine, suffix, hasLocalizedTemplates);
        }

    }

}
