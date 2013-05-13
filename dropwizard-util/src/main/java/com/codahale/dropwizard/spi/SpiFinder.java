package com.codahale.dropwizard.spi;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;

public class SpiFinder {
    private SpiFinder() { /* singleton */ }

    public static ImmutableList<Class<?>> locateSpiClasses() {
        final ImmutableList.Builder<Class<?>> klasses = ImmutableList.builder();
        try {
            final Enumeration<URL> resources = ClassLoader.getSystemResources("META-INF/services/com.codahale.dropwizard.spi.DropwizardSpiClass");
            while (resources.hasMoreElements()) {
                final URL url = resources.nextElement();
                try (InputStream input = url.openStream();
                     InputStreamReader streamReader = new InputStreamReader(input, Charsets.UTF_8);
                     BufferedReader reader = new BufferedReader(streamReader)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        try {
                            final Class<?> klass = Class.forName(line);
                            klasses.add(klass);
                        } catch (ClassNotFoundException ignored) {

                        }
                    }
                }
            }
        } catch (IOException ignored) {

        }
        return klasses.build();
    }
}
