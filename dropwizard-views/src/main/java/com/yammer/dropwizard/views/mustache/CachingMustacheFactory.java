package com.yammer.dropwizard.views.mustache;

import com.github.mustachejava.*;
import com.google.common.base.Charsets;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.yammer.dropwizard.views.View;

import java.io.*;

/**
 * A class-specific Mustache factory which caches the parsed/compiled templates.
 */
class CachingMustacheFactory extends DefaultMustacheFactory {
    private final Class<? extends View> klass;
    private final LoadingCache<String, Mustache> mustaches;

    CachingMustacheFactory(Class<? extends View> klass) {
        this.klass = klass;
        this.mustaches = CacheBuilder.newBuilder().build(new CacheLoader<String, Mustache>() {
            @Override
            public Mustache load(String key) throws Exception {
                return originalCompile(key);
            }
        });
    }

    @Override
    public Reader getReader(String resourceName) {
        final InputStream is = klass.getResourceAsStream(resourceName);
        if (is == null) {
            throw new MustacheException("Template " + resourceName + " not found");
        }
        return new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));
    }

    @Override
    public Mustache compile(String name) {
        return mustaches.getUnchecked(name);
    }

    private Mustache originalCompile(String name) {
        return super.compile(name);
    }
}
