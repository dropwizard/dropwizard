package com.codahale.dropwizard.views;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Optional;

import java.nio.charset.Charset;

/**
 * A Dropwizard view class.
 */
public abstract class View {
    private final String templateName;
    private final Charset charset;

    /**
     * Creates a new view.
     *
     * @param templateName the name of the template resource
     */
    protected View(String templateName) {
        this(templateName, null);
    }

    /**
     * Creates a new view.
     *
     * @param templateName the name of the template resource
     * @param charset      the character set for {@code templateName}
     */
    protected View(String templateName, Charset charset) {
        this.templateName = resolveName(templateName);
        this.charset = charset;
    }

    /**
     * Returns the template name.
     *
     * @return the template name
     */
    @JsonIgnore
    public String getTemplateName() {
        return templateName;
    }

    /**
     * Returns the character set of the template.
     *
     * @return the character set of the template
     */
    @JsonIgnore
    public Optional<Charset> getCharset() {
        return Optional.fromNullable(charset);
    }

    private String resolveName(String templateName) {
        if (templateName.startsWith("/")) {
            return templateName;
        }
        final String packagePath = getClass().getPackage().getName().replace('.', '/');
        return String.format("/%s/%s", packagePath, templateName);
    }
}
