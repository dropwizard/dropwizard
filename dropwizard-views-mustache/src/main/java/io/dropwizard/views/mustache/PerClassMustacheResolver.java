package io.dropwizard.views.mustache;

import com.github.mustachejava.MustacheResolver;
import com.google.common.base.Charsets;
import io.dropwizard.views.View;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * @{link MustacheResolver} implementation that resolves mustache
 * files from the classpath relatively from a provided class.
 */
class PerClassMustacheResolver implements MustacheResolver {
    private final Class<? extends View> klass;

    PerClassMustacheResolver(Class<? extends View> klass) {
        this.klass = klass;
    }

    @Override
    public Reader getReader(String resourceName) {
        final InputStream is = klass.getResourceAsStream(resourceName);
        if (is == null) {
            return null;
        }
        return new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));
    }
}
