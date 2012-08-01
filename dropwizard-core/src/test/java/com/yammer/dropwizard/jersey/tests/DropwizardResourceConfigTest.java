package com.yammer.dropwizard.jersey.tests;

import static org.junit.Assert.assertEquals;

import java.util.Iterator;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.junit.Test;

import com.sun.jersey.core.spi.scanning.PackageNamesScanner;
import com.yammer.dropwizard.jersey.DropwizardResourceConfig;
import com.yammer.dropwizard.jersey.tests.dummy.DummyResource;

public class DropwizardResourceConfigTest {

    @Test
    public void findsResourceClassInPackage() {
        final DropwizardResourceConfig rc = new DropwizardResourceConfig(true);
        rc.init(new PackageNamesScanner(new String[] { DummyResource.class.getPackage().getName() }));
        assertEquals("Resource classes found", 1, rc.getRootResourceClasses().size());
        assertEquals(
                "Unexpected resource class found",
                DummyResource.class,
                rc.getRootResourceClasses().iterator().next());
    }

    @Test
    public void findsResourceClassesInPackageAndSubpackage() {
        final DropwizardResourceConfig rc = new DropwizardResourceConfig(true);
        rc.init(new PackageNamesScanner(new String[] { getClass().getPackage().getName() }));
        assertEquals("Resource classes found", 2, rc.getRootResourceClasses().size());
        final Iterator<Class<?>> resources = rc.getRootResourceClasses().iterator();
        assertEquals(
                "Unexpected resource class found",
                TestResource.class,
                resources.next());
        assertEquals(
                "Unexpected resource class found",
                DummyResource.class,
                resources.next());
    }

    @Path("/dummy")
    public static class TestResource {
        @GET
        public String foo() {
            return "bar";
        }
    }
}
