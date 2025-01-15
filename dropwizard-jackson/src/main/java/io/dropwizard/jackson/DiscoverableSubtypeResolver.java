package io.dropwizard.jackson;

import com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    /**
     * The list of discovered subtypes.
     */
    private final List<Class<?>> discoveredSubtypes;

    /**
     * Constructs a subtype resolver which scans for subtypes of {@link Discoverable}.
     */
    public DiscoverableSubtypeResolver() {
        this(Discoverable.class);
    }

    /**
     * Constructs a subtype resolver which scans for subtypes of the provided class.
     *
     * @param rootKlass the class to choose the correct {@code META-INF/services} file from
     */
    public DiscoverableSubtypeResolver(Class<?> rootKlass) {
        final List<Class<?>> subtypes = new ArrayList<>();
        for (Class<?> klass : discoverServices(rootKlass)) {
            for (Class<?> subtype : discoverServices(klass)) {
                subtypes.add(subtype);
                registerSubtypes(subtype);
            }
        }
        this.discoveredSubtypes = subtypes;
    }

    /**
     * Returns the subtypes discovered from the {@code META-INF} configuration file.
     *
     * @return a list of {@link Class} objects representing the subtypes
     */
    public List<Class<?>> getDiscoveredSubtypes() {
        return discoveredSubtypes;
    }

    /**
     * Returns a {@link ClassLoader} from the current class.
     *
     * @return the current {@link ClassLoader}
     */
    protected ClassLoader getClassLoader() {
        return this.getClass().getClassLoader();
    }

    /**
     * Discovers the services in the {@code META-INF/services} folder for the provided class.
     *
     * @param klass the class to lookup services
     * @return the discovered services
     */
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
                        if (!line.startsWith("#")) {
                            final Class<?> loadedClass = loadClass(line);
                            if (loadedClass != null) {
                                serviceClasses.add(loadedClass);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Unable to load META-INF/services/{}", klass.getName(), e);
        }
        return serviceClasses;
    }

    /**
     * Loads a class discovered from a service file without throwing a {@link ClassNotFoundException},
     * if the class' implementation isn't found.
     *
     * @param line the string line with the class name
     * @return the {@link Class} instance or {@code null}, if the class was not found
     */
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
