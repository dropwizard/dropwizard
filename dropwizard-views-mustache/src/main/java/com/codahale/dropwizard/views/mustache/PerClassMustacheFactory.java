package com.codahale.dropwizard.views.mustache;

import com.codahale.dropwizard.views.View;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheException;
import com.google.common.base.Charsets;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * A class-specific Mustache factory which caches the parsed/compiled templates.
 */
class PerClassMustacheFactory extends DefaultMustacheFactory {
    private final Class<? extends View> klass;

    PerClassMustacheFactory(Class<? extends View> klass) {
        this.klass = klass;
    }

    @Override
    public Reader getReader(String resourceName) {
        final InputStream is = klass.getResourceAsStream(resourceName);
        if (is == null) {
            throw new MustacheException("Template " + resourceName + " not found");
        }
        return new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));
    }
}
