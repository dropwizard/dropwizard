package io.dropwizard.jersey;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.jersey.dummy.DummyResource;
import io.dropwizard.logging.BootstrapLogging;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;

import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class DropwizardResourceConfigTest {
    static {
        BootstrapLogging.bootstrap();
    }

    private DropwizardResourceConfig rc;

    @Before
    public void setUp() {
        rc = DropwizardResourceConfig.forTesting(new MetricRegistry());
    }

    @Test
    public void findsResourceClassInPackage() {
        rc.packages(DummyResource.class.getPackage().getName());

        assertThat(rc.getClasses()).contains(DummyResource.class);
    }

    @Test
    public void findsResourceClassesInPackageAndSubpackage() {
        rc.packages(getClass().getPackage().getName());

        assertThat(rc.getClasses()).contains(
                DummyResource.class,
                TestResource.class,
                ResourceInterface.class);
    }

    @Test
    public void combinesAlRegisteredClasses() {
        rc.register(new TestResource());
        rc.registerClasses(ResourceInterface.class, ImplementingResource.class);

        assertThat(rc.allClasses()).contains(
                TestResource.class,
                ResourceInterface.class,
                ImplementingResource.class
        );
    }

    @Test
    public void combinesAlRegisteredClassesPathOnMethodLevel() {
        rc.register(new TestResource());
        rc.register(new ResourcePathOnMethodLevel());

        assertThat(rc.allClasses()).contains(
                TestResource.class,
                ResourcePathOnMethodLevel.class
        );

        assertThat(rc.getEndpointsInfo())
                .contains("GET     /bar (io.dropwizard.jersey.DropwizardResourceConfigTest.ResourcePathOnMethodLevel)")
                .contains("GET     /dummy (io.dropwizard.jersey.DropwizardResourceConfigTest.TestResource)");
    }

    @Test
    public void logsNoInterfaces() {
        rc.packages(getClass().getPackage().getName());

        assertThat(rc.getEndpointsInfo()).doesNotContain("io.dropwizard.jersey.DropwizardResourceConfigTest.ResourceInterface");
    }

    @Test
    public void logsNoEndpointsWhenNoResourcesAreRegistered() {
        assertThat(rc.getEndpointsInfo()).contains("    NONE");
    }

    @Test
    public void logsEndpoints() {
        rc.register(TestResource.class);
        rc.register(ImplementingResource.class);

        assertThat(rc.getEndpointsInfo())
                .contains("GET     /dummy (io.dropwizard.jersey.DropwizardResourceConfigTest.TestResource)")
                .contains("GET     /another (io.dropwizard.jersey.DropwizardResourceConfigTest.ImplementingResource)");
    }

    @Test
    public void logsEndpointsSorted() {
        rc.register(DummyResource.class);
        rc.register(TestResource2.class);
        rc.register(TestResource.class);
        rc.register(ImplementingResource.class);

        final String expectedLog = String.format(
                "The following paths were found for the configured resources:%n"
                + "%n"
                + "    GET     / (io.dropwizard.jersey.dummy.DummyResource)%n"
                + "    GET     /another (io.dropwizard.jersey.DropwizardResourceConfigTest.ImplementingResource)%n"
                + "    GET     /async (io.dropwizard.jersey.dummy.DummyResource)%n"
                + "    DELETE  /dummy (io.dropwizard.jersey.DropwizardResourceConfigTest.TestResource2)%n"
                + "    GET     /dummy (io.dropwizard.jersey.DropwizardResourceConfigTest.TestResource)%n"
                + "    POST    /dummy (io.dropwizard.jersey.DropwizardResourceConfigTest.TestResource2)%n");
        assertThat(rc.getEndpointsInfo()).isEqualTo(expectedLog);
    }


    @Path("/dummy")
    public static class TestResource {
        @GET
        public String foo() {
            return "bar";
        }
    }

    @Path("/dummy")
    public static class TestResource2 {
        @POST
        public String fooPost() {
            return "bar";
        }

        @DELETE
        public String fooDelete() {
            return "bar";
        }
    }

    @Path("/another")
    public static interface ResourceInterface {
        @GET
        public String bar();
    }

    public static class ResourcePathOnMethodLevel {
        @GET @Path("/bar")
        public String bar() {
            return "";
        }
    }

    public static class ImplementingResource implements ResourceInterface {
        @Override
        public String bar() {
            return "";
        }
    }
}
