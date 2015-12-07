package io.dropwizard.jackson;

import com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * A subtype resolver which discovers subtypes via
 * {@code META-INF/services/io.dropwizard.jackson.Discoverable}.
 */
public class DiscoverableSubtypeResolver extends StdSubtypeResolver {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DiscoverableSubtypeResolver.class);

    private final ImmutableList<Class<?>> discoveredSubtypes;

    public DiscoverableSubtypeResolver() {
        this(Discoverable.class);
    }

    public DiscoverableSubtypeResolver(Class<?> rootKlass) {
        final ImmutableList.Builder<Class<?>> subtypes = ImmutableList.builder();
        for (Class<?> klass : discoverServices(rootKlass)) {
            for (Class<?> subtype : discoverServices(klass)) {
                subtypes.add(subtype);
                registerSubtypes(subtype);
            }
        }
        this.discoveredSubtypes = subtypes.build();
    }

    public ImmutableList<Class<?>> getDiscoveredSubtypes() {
        return discoveredSubtypes;
    }

    protected ClassLoader getClassLoader() {
        return this.getClass().getClassLoader();
    }

    protected List<Class<?>> discoverServices(Class<?> klass) {
        final List<Class<?>> serviceClasses = new ArrayList<>();
        try {
            // use classloader that loaded this class to find the service descriptors on the classpath
            // better than ClassLoader.getSystemResources() which may not be the same classloader if ths app
            // is running in a container (e.g. via maven exec:java)
            final Enumeration<URL> resources = getClassLoader().getResources("META-INF/services/" + klass.getName());
            while (resources.hasMoreElements()) {
                final URL url = resources.nextElement();
                try (InputStream input = url.openStream();
                     InputStreamReader streamReader = new InputStreamReader(input, StandardCharsets.UTF_8);
                     BufferedReader reader = new BufferedReader(streamReader)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        final Class<?> loadedClass = loadClass(line);
                        if (loadedClass != null) {
                            serviceClasses.add(loadedClass);
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Unable to load META-INF/services/{}", klass.getName(), e);
        }
        return serviceClasses;
    }

    @Nullable
    private Class<?> loadClass(String line) {
        try {
            return getClassLoader().loadClass(line.trim());
        } catch (ClassNotFoundException e) {
            LOGGER.info("Unable to load {}", line);
            return null;
        }
    }
}
