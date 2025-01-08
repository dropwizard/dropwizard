package io.dropwizard.testing.junit5;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ConfigurationMetadata;
import io.dropwizard.core.Configuration;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Verify that Logback can be excluded from the classpath without generating any noise.
 */
class LogbackExcludedTest {

    @Test
    void testLogbackExcludedClassNotFound() throws Exception {
        testBuildConfigurationMetadata(className -> {
            if (className.startsWith("ch.qos.logback.")) {
                throw new ClassNotFoundException();
            }
        });
    }

    @Test
    void testLogbackExcludedNoClassDef() throws Exception {
        testBuildConfigurationMetadata(className -> {
            if (className.startsWith("ch.qos.logback.")) {
                throw new NoClassDefFoundError();
            }
        });
    }

    @Test
    void testPropagatedException() throws Exception {
        AtomicReference<RuntimeException> thrown = new AtomicReference<>();
        try {
            testBuildConfigurationMetadata(className -> {
                if (className.startsWith("ch.qos.logback.")) {
                    thrown.set(new RuntimeException("Some unexpected error"));
                    throw thrown.get();
                }
            });
            fail("Expected exception to propagate out of ConfigurationMetadata");
        } catch (InvocationTargetException e) {
            assertThat(e.getCause()).isSameAs(thrown.get());
        }
    }

    public void testBuildConfigurationMetadata(CheckedConsumer<String> classFilter) throws Exception {
        try (ByteArrayOutputStream byteStream = captureStderr();
                CustomClassLoader loader = new CustomClassLoader(classFilter)) {
            // create class objects from custom loader
            Class<ConfigurationMetadata> cmType = loader.reloadClass(ConfigurationMetadata.class);
            Class<ObjectMapper> omType = loader.reloadClass(ObjectMapper.class);
            Class<Configuration> confType = loader.reloadClass(Configuration.class);
            // construct ConfigurationMetadata object using class object associated with custom loader so that we can
            // simulate Logback not being in the classpath
            cmType.getConstructor(omType, Class.class).newInstance(omType.newInstance(), confType);

            // make sure nothing is emitted to stderr; previously the absence of Logback in the classpath would cause
            // "class io.dropwizard.configuration.ConfigurationMetadata$1: Type ch.qos.logback.access.common.spi.IAccessEvent
            // not present" to be emitted to stderr
            String err = byteStream.toString();
            assertThat(err).isEmpty();
        }
    }

    /**
     * Replace stderr with a byte-backed stream until the returned stream is closed.
     */
    private static ByteArrayOutputStream captureStderr() {
        PrintStream err = System.err;
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream() {
            @Override
            public void close() {
                System.setErr(err);
            }
        };
        System.setErr(new PrintStream(byteStream));
        return byteStream;
    }

    private interface CheckedConsumer<T> {

        void accept(T t) throws ClassNotFoundException;
    }

    /**
     * Custom class loader to simulate classes not being in the classpath.
     */
    private static class CustomClassLoader extends URLClassLoader {

        public final CheckedConsumer<String> classFilter;

        public CustomClassLoader(CheckedConsumer<String> classFilter) {
            super(new URL[0], null);
            this.classFilter = classFilter;
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            classFilter.accept(name);
            Class<?> clazz = getClassSystemLoader(name);
            Optional<URL> url = getUrl(clazz);
            if (url.isEmpty()) {
                // no URL associated with class; must be standard Java
                return clazz;
            }
            addURL(url.get());
            return super.loadClass(name);
        }

        /**
         * Get the supplied class object via this class loader so that any references made to it are resolved through
         * this class loader.
         */
        public <T> Class<T> reloadClass(Class<T> clazz) throws ClassNotFoundException {
            @SuppressWarnings("unchecked")
            Class<T> ret = (Class<T>) loadClass(clazz.getCanonicalName());
            return ret;
        }

        private static Class<?> getClassSystemLoader(String name) throws ClassNotFoundException {
            return ClassLoader.getSystemClassLoader().loadClass(name);
        }

        private static Optional<URL> getUrl(Class<?> clazz) {
            return Optional.ofNullable(clazz.getProtectionDomain().getCodeSource()).map(CodeSource::getLocation);
        }
    }
}
