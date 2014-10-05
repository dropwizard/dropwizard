package io.dropwizard.jersey;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.jersey.dummy.DummyResource;
import io.dropwizard.logging.LoggingFactory;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
public class DropwizardResourceConfigTest {
    static {
        LoggingFactory.bootstrap();
    }

    private final DropwizardResourceConfig resourceConfig = DropwizardResourceConfig.forTesting(new MetricRegistry());

    @Test
    public void findsResourceClassInPackage() {
        resourceConfig.packages(DummyResource.class.getPackage().getName());

        assertThat(resourceConfig.getClasses()).contains(DummyResource.class);
    }

    @Test
    public void findsResourceClassesInPackageAndSubpackage() {
        resourceConfig.packages(getClass().getPackage().getName());

        assertThat(resourceConfig.getClasses())
                .contains
                        (DummyResource.class, TestResource.class, AnotherTestResource.class);
    }

    @Test
    public void combinesAlRegisteredClasses() {
        resourceConfig.register(new TestResource());
        resourceConfig.register(AnotherTestResourceBean.class);
        resourceConfig.register("foo");

        // @formatter:off
        assertThat(resourceConfig.allClasses())
                .isNotEmpty()
                .contains(
                        TestResource.class,
                        AnotherTestResourceBean.class,
                        String.class
                );
        // @formatter:on
    }

    @Test
    public void logsNoEndpointsWhenNoResourcesAreRegistered() {
        final String log = resourceConfig.logEndpoints();

        assertThat(log).contains("\n    NONE\n");
    }

    @Test
    public void logsEndpoints() {
        resourceConfig.register(new TestResource());
        resourceConfig.register(AnotherTestResourceBean.class);
        final String log = resourceConfig.logEndpoints();

        assertThat(log).contains("\n    GET     /dummy (io.dropwizard.jersey.DropwizardResourceConfigTest.TestResource)");
        assertThat(log).contains("\n    GET     /another (io.dropwizard.jersey.DropwizardResourceConfigTest.AnotherTestResourceBean)");
    }

    @Path("/dummy")
    public static class TestResource {
        @GET
        public String foo() {
            return "bar";
        }
    }


    @Path("/another")
    public static interface AnotherTestResource {
        @GET
        public String bar();
    }

    public static class AnotherTestResourceBean implements AnotherTestResource {

        @Override
        public String bar() {
            return null;
        }
    }

}
