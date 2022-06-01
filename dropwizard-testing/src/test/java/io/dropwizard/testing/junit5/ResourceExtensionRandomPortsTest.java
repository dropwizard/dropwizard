package io.dropwizard.testing.junit5;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory;
import org.junit.jupiter.api.Test;

class ResourceExtensionRandomPortsTest {

    @Test
    void eachTestShouldUseANewPort() throws Throwable {
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
