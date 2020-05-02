package io.dropwizard.testing.junit5;

import org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.ConcurrentSkipListSet;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(DropwizardExtensionsSupport.class)
class ResourceExtensionRandomPortsTest {
    private final ResourceExtension resources = ResourceExtension.builder()
            .setTestContainerFactory(new GrizzlyTestContainerFactory())
            .build();

    ConcurrentSkipListSet<Integer> usedPorts = new ConcurrentSkipListSet<>();

    @RepeatedTest(10)
    public void eachTestShouldUseANewPort() {
        final int port = resources.target("/").getUri().getPort();
        assertThat(usedPorts).doesNotContain(port);

        usedPorts.add(port);
    }
}
