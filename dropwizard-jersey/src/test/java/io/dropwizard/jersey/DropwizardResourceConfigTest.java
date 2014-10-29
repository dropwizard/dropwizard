package io.dropwizard.jersey;

import com.codahale.metrics.MetricRegistry;
import com.sun.jersey.core.spi.scanning.PackageNamesScanner;
import io.dropwizard.jersey.dummy.DummyResource;
import io.dropwizard.logging.LoggingFactory;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.LinkedList;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;

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

    @Test
    public void safeguardAgainstRecursiveEndpointLogging() throws Exception {
        final DropwizardResourceConfig rc = DropwizardResourceConfig.forTesting(new MetricRegistry());
        rc.init(new PackageNamesScanner(new String[] { getClass().getPackage().getName() }));

        LinkedList<String> endpoints = new LinkedList<>();
        try {
            rc.populateEndpoints(endpoints, "/", TestResource.class, false);
        } catch (StackOverflowError e) {
            fail("Did not expect a StackOverflowError here", e);
        }

        assertThat(endpoints).contains(
                "    GET     /dummy (io.dropwizard.jersey.DropwizardResourceConfigTest.TestResource)",
                "    GET     /dummy/sub (io.dropwizard.jersey.DropwizardResourceConfigTest.SubResource)",
                "    *       /dummy/sub/{subPath} (io.dropwizard.jersey.DropwizardResourceConfigTest.SubResource)",
                "    *       /dummy/sub/subResource2 (io.dropwizard.jersey.DropwizardResourceConfigTest.SubResource)"
        );

    }


    @Path("/dummy")
    public static class TestResource {
        @GET
        public String foo() {
            return "bar";
        }

        @Path("/sub")
        public SubResource subFooLocator() {
            return new SubResource();
        }
    }

    public static class SubResource {

        @GET
        public String subFoo() {
            return "subFoo";
        }

        @Path("{subPath}")
        public SubResource subSubFooLocator() {
            return new SubResource();
        }

        @Path("subResource2")
        public SubResource2 subSubFooLocator2() {
            return new SubResource2();
        }

    }
    public static class SubResource2 {

        @POST
        public String subFoo2() {
            return "subFoo2";
        }


    }



}
