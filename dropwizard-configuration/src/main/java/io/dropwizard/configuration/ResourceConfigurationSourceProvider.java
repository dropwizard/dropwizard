package io.dropwizard.configuration;

import java.io.IOException;
import java.io.InputStream;

/**
 * An implementation of {@link ConfigurationSourceProvider} which reads the configuration
 * from a resource file.
 * <p>
 * In order to abide by the calling conventions of
 * {ClassLoader#getResourceAsStream} [1], absolute path strings
 * (i.e. those with leading "/" characters) passed to {@link #open(String)}
 * are converted to relative paths by removing the leading "/".
 * <p>
 * See [1] for more information on resources in Java and how they are
 * loaded at runtime.
 * <p>
 * [1] https://docs.oracle.com/javase/8/docs/technotes/guides/lang/resources.html
 */
public class ResourceConfigurationSourceProvider implements ConfigurationSourceProvider {
    @Override
    public InputStream open(String path) throws IOException {
        InputStream result = getResourceAsStream(path);
        return result == null && path.startsWith("/") ? getResourceAsStream(path.substring(1)) : result;
    }

    private InputStream getResourceAsStream(String path) {
        return getClass().getClassLoader().getResourceAsStream(path);
    }
}
