package io.dropwizard.jersey;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.jersey.dummy.DummyResource;
import io.dropwizard.logging.LoggingFactory;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
public class DropwizardResourceConfigTest {
    static {
        LoggingFactory.bootstrap();
    }

    @Test
    public void findsResourceClassInPackage() {
        ResourceConfig rc = DropwizardResourceConfig.forTesting(new MetricRegistry());
        rc = rc.packages(DummyResource.class.getPackage().getName());

        assertThat(rc.getClasses()).contains(DummyResource.class);
    }

    @Test
    public void findsResourceClassesInPackageAndSubpackage() {
        ResourceConfig rc = DropwizardResourceConfig.forTesting(new MetricRegistry());
        rc = rc.packages(getClass().getPackage().getName());

        assertThat(rc.getClasses())
                .contains
                        (DummyResource.class, TestResource.class);
    }

    @Test
    public void testGetEndpointsInfo() {
        final DropwizardResourceConfig rc = DropwizardResourceConfig.forTesting(new MetricRegistry());
        rc.init(new PackageNamesScanner(new String[]{DummyResource.class.getPackage().getName()}));

        assertThat(rc.getEndpointsInfo()).isEqualTo("The following paths were found for the configured resources:\n" +
                "\n" +
                "    GET     / (io.dropwizard.jersey.dummy.DummyResource)" +
                "\n");
    }

    @Path("/dummy")
    public static class TestResource {
        @GET
        public String foo() {
            return "bar";
        }
    }
}
