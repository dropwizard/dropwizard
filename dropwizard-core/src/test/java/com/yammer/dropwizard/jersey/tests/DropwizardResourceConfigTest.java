package com.yammer.dropwizard.jersey.tests;

import com.google.common.collect.ImmutableSet;
import com.sun.jersey.core.spi.scanning.PackageNamesScanner;
import com.yammer.dropwizard.jersey.DropwizardResourceConfig;
import com.yammer.dropwizard.jersey.tests.dummy.DummyResource;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DropwizardResourceConfigTest {

    @Test
    public void findsResourceClassInPackage() {
        final DropwizardResourceConfig rc = new DropwizardResourceConfig(true);
        rc.init(new PackageNamesScanner(new String[] { DummyResource.class.getPackage().getName() }));

        assertThat(rc.getRootResourceClasses(),
                   is((Set<Class<?>>) ImmutableSet.<Class<?>>of(
                           DummyResource.class
                   )));
    }

    @Test
    public void findsResourceClassesInPackageAndSubpackage() {
        final DropwizardResourceConfig rc = new DropwizardResourceConfig(true);
        rc.init(new PackageNamesScanner(new String[] { getClass().getPackage().getName() }));

        assertThat(rc.getRootResourceClasses(),
                   is((Set<Class<?>>) ImmutableSet.<Class<?>>of(
                           DummyResource.class, TestResource.class
                   )));
    }

    @Path("/dummy")
    public static class TestResource {
        @GET
        public String foo() {
            return "bar";
        }
    }
}
