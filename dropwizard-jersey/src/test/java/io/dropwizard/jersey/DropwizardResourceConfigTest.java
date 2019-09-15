package io.dropwizard.jersey;

import io.dropwizard.jersey.dummy.DummyResource;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.model.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

public class DropwizardResourceConfigTest {
    private DropwizardResourceConfig rc = DropwizardResourceConfig.forTesting();
    private AbstractJerseyTest jerseyTest = new AbstractJerseyTest() {
        @Override
        protected Application configure() {
            return rc;
        }
    };

    @AfterEach
    void teardown() throws Exception {
        jerseyTest.tearDown();
    }

    // Start jersey test instance so that our resource config
    // successfully hooks into the Jersey start up application event
    private void runJersey() {
        try {
            jerseyTest.setUp();
        } catch (Exception e) {
            throw new RuntimeException("Could not start jersey", e);
        }
    }

    @Test
    void findsResourceClassInPackage() {
        rc.packages(DummyResource.class.getPackage().getName());

        assertThat(rc.getClasses()).contains(DummyResource.class);
    }

    @Test
    void findsResourceClassesInPackageAndSubpackage() {
        rc.packages(getClass().getPackage().getName());

        assertThat(rc.getClasses()).contains(
                DummyResource.class,
                TestResource.class,
                ResourceInterface.class);
    }

    @Test
    void combinesAlRegisteredClasses() {
        rc.register(new TestResource());
        rc.registerClasses(ResourceInterface.class, ImplementingResource.class);

        assertThat(rc.allClasses()).contains(
                TestResource.class,
                ResourceInterface.class,
                ImplementingResource.class
        );
    }

    @Test
    void combinesAlRegisteredClassesPathOnMethodLevel() {
        rc.register(new TestResource());
        rc.register(new ResourcePathOnMethodLevel());

        assertThat(rc.allClasses()).contains(
                TestResource.class,
                ResourcePathOnMethodLevel.class
        );

        runJersey();
        assertThat(rc.getEndpointsInfo())
                .contains("GET     /bar (io.dropwizard.jersey.DropwizardResourceConfigTest.ResourcePathOnMethodLevel)")
                .contains("GET     /dummy (io.dropwizard.jersey.DropwizardResourceConfigTest.TestResource)");
    }

    @Test
    void logsNoInterfaces() {
        rc.packages(getClass().getName());

        runJersey();
        assertThat(rc.getEndpointsInfo()).doesNotContain("io.dropwizard.jersey.DropwizardResourceConfigTest.ResourceInterface");
    }

    @Test
    void logsNoEndpointsWhenNoResourcesAreRegistered() {
        runJersey();
        assertThat(rc.getEndpointsInfo()).contains("    NONE");
    }

    @Test
    void logsEndpoints() {
        rc.register(TestResource.class);
        rc.register(ImplementingResource.class);

        runJersey();
        assertThat(rc.getEndpointsInfo())
                .contains("GET     /dummy (io.dropwizard.jersey.DropwizardResourceConfigTest.TestResource)")
                .contains("GET     /another (io.dropwizard.jersey.DropwizardResourceConfigTest.ImplementingResource)");
    }

    @Test
    void logsEndpointsSorted() {
        rc.register(DummyResource.class);
        rc.register(TestResource2.class);
        rc.register(TestResource.class);
        rc.register(ImplementingResource.class);

        runJersey();
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

    @Test
    void logsNestedEndpoints() {
        rc.register(WrapperResource.class);

        runJersey();
        assertThat(rc.getEndpointsInfo())
                .contains("    GET     /wrapper/bar (io.dropwizard.jersey.DropwizardResourceConfigTest.ResourcePathOnMethodLevel)")
                .contains("    GET     /locator/bar (io.dropwizard.jersey.DropwizardResourceConfigTest.ResourcePathOnMethodLevel)")
                .contains("    UNKNOWN /obj/{it} (io.dropwizard.jersey.DropwizardResourceConfigTest.WrapperResource)");
    }

    @Test
    void logsProgrammaticalEndpoints() {
        Resource.Builder resourceBuilder = Resource.builder("/prefix");
        resourceBuilder.addChildResource(Resource.from(DummyResource.class));
        resourceBuilder.addChildResource(Resource.from(TestResource.class));
        resourceBuilder.addChildResource(Resource.from(ImplementingResource.class));

        rc.registerResources(resourceBuilder.build());

        runJersey();
        final String expectedLog = String.format(
                "The following paths were found for the configured resources:%n"
                + "%n"
                + "    GET     /prefix/ (io.dropwizard.jersey.dummy.DummyResource)%n"
                + "    GET     /prefix/another (io.dropwizard.jersey.DropwizardResourceConfigTest.ImplementingResource)%n"
                + "    GET     /prefix/async (io.dropwizard.jersey.dummy.DummyResource)%n"
                + "    GET     /prefix/dummy (io.dropwizard.jersey.DropwizardResourceConfigTest.TestResource)%n"
        );

        assertThat(rc.getEndpointsInfo()).isEqualTo(expectedLog);
    }

    @Test
    void testEndpointLoggerPathCleaning() {
        String dirtyPath = " /this//is///a/dirty//path/     ";
        String anotherDirtyPath = "a/l//p/h/  /a/b /////  e/t";

        assertThat(DropwizardResourceConfig.cleanUpPath(dirtyPath)).isEqualTo("/this/is/a/dirty/path/");
        assertThat(DropwizardResourceConfig.cleanUpPath(anotherDirtyPath)).isEqualTo("a/l/p/h/a/b/e/t");
    }

    @Test
    void duplicatePathsTest() {
        rc.register(TestDuplicateResource.class);

        runJersey();
        final String expectedLog = String.format("The following paths were found for the configured resources:%n" + "%n"
                + "    GET     /anotherMe (io.dropwizard.jersey.DropwizardResourceConfigTest.TestDuplicateResource)%n"
                + "    GET     /callme (io.dropwizard.jersey.DropwizardResourceConfigTest.TestDuplicateResource)%n");

        assertThat(rc.getEndpointsInfo()).contains(expectedLog);
        assertThat(rc.getEndpointsInfo()).containsOnlyOnce("    GET     /callme (io.dropwizard.jersey.DropwizardResourceConfigTest.TestDuplicateResource)");
    }

    @Test
    void logEndpointWithRootSubresource() {
        rc.register(new ShoppingStore());

        runJersey();
        final String expectedLog = String.format("The following paths were found for the configured resources:%n" + "%n"
            + "    GET     /customers/{id} (io.dropwizard.jersey.DropwizardResourceConfigTest.Customer)%n"
            + "    UNKNOWN /customers/{id}/address (io.dropwizard.jersey.DropwizardResourceConfigTest.Customer)%n");

        assertThat(rc.getEndpointsInfo()).contains(expectedLog);
    }

    @Test
    void logEndpointBinder() {
        rc.register(ResourceInterface.class);
        rc.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(ImplementingResource.class).to(ResourceInterface.class);
            }
        });

        runJersey();
        final String expectedLog = String.format("The following paths were found for the configured resources:%n" + "%n"
            + "    GET     /another (io.dropwizard.jersey.DropwizardResourceConfigTest.ResourceInterface)");

        assertThat(rc.getEndpointsInfo()).contains(expectedLog);
    }

    @Test
    void logsEndpointsContextPathUrlPattern() {
        rc.setContextPath("/context");
        rc.setUrlPattern("/pattern");
        rc.register(TestResource.class);
        rc.register(ImplementingResource.class);

        runJersey();
        assertThat(rc.getEndpointsInfo())
            .contains("GET     /context/pattern/dummy (io.dropwizard.jersey.DropwizardResourceConfigTest.TestResource)")
            .contains("GET     /context/pattern/another (io.dropwizard.jersey.DropwizardResourceConfigTest.ImplementingResource)");
    }

    @Test
    void testMixedClassAndInstanceRegistration() {
        rc.setContextPath("/context");
        rc.setUrlPattern("/pattern");
        Object[] registrations = new Object[] {
                TestResource.class,
                new ImplementingResource()
        };
        for (Object registration : registrations) {
            rc.register(registration);
        }

        runJersey();
        assertThat(rc.getEndpointsInfo())
                .contains("GET     /context/pattern/dummy (io.dropwizard.jersey.DropwizardResourceConfigTest.TestResource)")
                .contains("GET     /context/pattern/another (io.dropwizard.jersey.DropwizardResourceConfigTest.ImplementingResource)");
    }

    @Test
    void logsEndpointsContextPath() {
        rc.setContextPath("/context");
        rc.register(TestResource.class);
        rc.register(ImplementingResource.class);

        runJersey();
        assertThat(rc.getEndpointsInfo())
            .contains("GET     /context/dummy (io.dropwizard.jersey.DropwizardResourceConfigTest.TestResource)")
            .contains("GET     /context/another (io.dropwizard.jersey.DropwizardResourceConfigTest.ImplementingResource)");
    }

    @Test
    void logsEndpointsNoSlashContextPath() {
        rc.setContextPath("context");
        rc.register(TestResource.class);
        rc.register(ImplementingResource.class);

        runJersey();
        assertThat(rc.getEndpointsInfo())
            .contains("GET     /context/dummy (io.dropwizard.jersey.DropwizardResourceConfigTest.TestResource)")
            .contains("GET     /context/another (io.dropwizard.jersey.DropwizardResourceConfigTest.ImplementingResource)");
    }

    @Test
    void logsEndpointsRelativePaths() {
        rc.register(new TestRootResource());
        rc.register(new TestRelativePathResource());

        assertThat(rc.allClasses()).contains(TestRootResource.class, TestRelativePathResource.class);

        runJersey();
        assertThat(rc.getEndpointsInfo())
                .contains("GET     / (io.dropwizard.jersey.DropwizardResourceConfigTest.TestRootResource)")
                .contains("GET     /relative/child1 (io.dropwizard.jersey.DropwizardResourceConfigTest.TestRelativePathResource)")
                .contains("GET     /relative/child2 (io.dropwizard.jersey.DropwizardResourceConfigTest.TestRelativePathResource)");
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

    @Path("relative")
    public static class TestRelativePathResource {
        @GET
        @Path("child1")
        public String child1() {
            return "foo";
        }

        @GET
        @Path("child2")
        public String child2() {
            return "bar";
        }
    }

    @Path("/")
    public static class TestRootResource {
        @GET
        public String helloWorld() {
            return "helloWorld";
        }
    }

    @Path("/")
    public static class TestDuplicateResource {

        @GET
        @Path("callme")
        @Produces(MediaType.APPLICATION_JSON)
        public String fooGet() {
            return "bar";
        }

        @GET
        @Path("callme")
        @Produces(MediaType.TEXT_HTML)
        public String fooGet2() {
            return "bar2";
        }

        @GET
        @Path("callme")
        @Produces(MediaType.APPLICATION_XML)
        public String fooGet3() {
            return "bar3";
        }

        @GET
        @Path("anotherMe")
        public String fooGet4() {
            return "bar4";
        }

    }

    @Path("/another")
    public interface ResourceInterface {
        @GET
        String bar();
    }

    @Path("/")
    public static class WrapperResource {
        @Path("wrapper")
        public ResourcePathOnMethodLevel getNested() {
            return new ResourcePathOnMethodLevel();
        }

        @Path("locator")
        public Class<ResourcePathOnMethodLevel> getNested2() {
            return ResourcePathOnMethodLevel.class;
        }

        @Path("obj/{it}")
        public Object getNested3(@PathParam("it") String path) {
            if (path.equals("implement")) {
                return new ImplementingResource();
            }
            return new ResourcePathOnMethodLevel();
        }
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

    @Path("/")
    public static class ShoppingStore {
        @Path("/customers/{id}")
        public Customer getCustomer(@PathParam("id") int id) {
            return new Customer();
        }
    }

    public static class Customer {
        @GET
        public String get() {return "A";}

        @Path("/address")
        public String getAddress() {return "B";}

    }
}
