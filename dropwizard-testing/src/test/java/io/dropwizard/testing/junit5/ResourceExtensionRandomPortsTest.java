package io.dropwizard.testing.junit5;

import org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceExtensionRandomPortsTest {

    @Test
    public void eachTestShouldUseANewPort() throws Throwable {
        final ResourceExtension resources = ResourceExtension.builder()
                .setTestContainerFactory(new GrizzlyTestContainerFactory())
                .build();
        Set<Integer> usedPorts = new HashSet<>();

        for (int i = 0; i < 10; i++) {
            resources.before();
            final int port = resources.target("/").getUri().getPort();
            usedPorts.add(port);
        }
        assertThat(usedPorts).hasSizeGreaterThanOrEqualTo(2);
    }
}
