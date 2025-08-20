package io.dropwizard.metrics.graphite;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.graphite.GraphiteUDP;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.validation.BaseValidator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class GraphiteReporterFactoryTest {
    private final GraphiteReporter.Builder builderSpy = mock();
    private final GraphiteReporterFactory graphiteReporterFactory = new GraphiteReporterFactory() {
        @Override
        protected GraphiteReporter.Builder builder(MetricRegistry registry) {
            return builderSpy;
        }
    };

    @Test
    void isDiscoverable() {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
            .contains(GraphiteReporterFactory.class);
    }

    @Test
    void createDefaultFactory() throws Exception {
        final GraphiteReporterFactory factory = new YamlConfigurationFactory<>(GraphiteReporterFactory.class,
            BaseValidator.newValidator(), Jackson.newObjectMapper(), "dw")
            .build();
        assertThat(factory.getFrequency()).isNotPresent();
    }

    @Test
    void testNoAddressResolutionForGraphite() {
        graphiteReporterFactory.build(new MetricRegistry());

        verify(builderSpy).build(ArgumentMatchers.assertArg(graphite -> assertThat(graphite)
            .isInstanceOf(Graphite.class)
            .extracting("hostname", "port", "address")
            .contains("localhost", 2003, null)));
    }

    @Test
    void testCorrectTransportForGraphiteUDP() {
        graphiteReporterFactory.setTransport("udp");
        graphiteReporterFactory.build(new MetricRegistry());

        verify(builderSpy).build(ArgumentMatchers.<GraphiteUDP>assertArg(graphite -> assertThat(graphite)
            .isInstanceOf(GraphiteUDP.class)
            .extracting("hostname", "port", "address")
            .contains("localhost", 2003, null)));
    }
}
