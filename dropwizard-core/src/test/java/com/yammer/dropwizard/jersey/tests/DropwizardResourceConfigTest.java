package com.yammer.dropwizard.jersey.tests;

import com.sun.jersey.core.spi.scanning.PackageNamesScanner;
import com.yammer.dropwizard.jersey.DropwizardResourceConfig;
import com.yammer.dropwizard.jersey.tests.dummy.DummyResource;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.fest.assertions.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
public class DropwizardResourceConfigTest {
    static {
        Logger.getLogger("com.sun.jersey").setLevel(Level.OFF);
    }

    @Test
    public void findsResourceClassInPackage() {
        final DropwizardResourceConfig rc = new DropwizardResourceConfig(true);
        rc.init(new PackageNamesScanner(new String[] { DummyResource.class.getPackage().getName() }));

        assertThat(rc.getRootResourceClasses())
                .containsOnly(DummyResource.class);
    }

    @Test
    public void findsResourceClassesInPackageAndSubpackage() {
        final DropwizardResourceConfig rc = new DropwizardResourceConfig(true);
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
