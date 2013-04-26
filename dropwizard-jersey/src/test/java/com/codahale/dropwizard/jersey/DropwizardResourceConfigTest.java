package com.codahale.dropwizard.jersey;

import com.codahale.dropwizard.jersey.dummy.DummyResource;
import com.codahale.dropwizard.logging.LoggingFactory;
import com.codahale.metrics.MetricRegistry;
import com.sun.jersey.core.spi.scanning.PackageNamesScanner;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import static org.fest.assertions.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
public class DropwizardResourceConfigTest {
    static {
        LoggingFactory.bootstrap();
    }

    @Test
    public void findsResourceClassInPackage() {
        final DropwizardResourceConfig rc = DropwizardResourceConfig.forTesting(new MetricRegistry());
        rc.init(new PackageNamesScanner(new String[] { DummyResource.class.getPackage().getName() }));

        assertThat(rc.getRootResourceClasses())
                .containsOnly(DummyResource.class);
    }

    @Test
    public void findsResourceClassesInPackageAndSubpackage() {
        final DropwizardResourceConfig rc = DropwizardResourceConfig.forTesting(new MetricRegistry());
        rc.init(new PackageNamesScanner(new String[] { getClass().getPackage().getName() }));

        assertThat(rc.getRootResourceClasses())
                .contains
                        (DummyResource.class, TestResource.class);
    }

    @Path("/dummy")
    public static class TestResource {
        @GET
        public String foo() {
            return "bar";
        }
    }
}
