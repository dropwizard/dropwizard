package io.dropwizard.metrics.graphite;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.graphite.GraphiteUDP;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DefaultObjectMapperFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.validation.BaseValidator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class GraphiteReporterFactoryTest {

    private final GraphiteReporter.Builder builderSpy = mock(GraphiteReporter.Builder.class);

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
             BaseValidator.newValidator(), new DefaultObjectMapperFactory().newObjectMapper(), "dw")
            .build();
        assertThat(factory.getFrequency()).isNotPresent();
    }

    @Test
    void testNoAddressResolutionForGraphite() {
        graphiteReporterFactory.build(new MetricRegistry());

        final ArgumentCaptor<Graphite> argument = ArgumentCaptor.forClass(Graphite.class);
        verify(builderSpy).build(argument.capture());

        final Graphite graphite = argument.getValue();
        assertThat(graphite)
            .satisfies(g -> assertThat(g).extracting("hostname").isEqualTo("localhost"))
            .satisfies(g -> assertThat(g).extracting("port").isEqualTo(2003))
            .satisfies(g -> assertThat(g).extracting("address").isNull());
    }

    @Test
    void testCorrectTransportForGraphiteUDP() {
        graphiteReporterFactory.setTransport("udp");
        graphiteReporterFactory.build(new MetricRegistry());

        final ArgumentCaptor<GraphiteUDP> argument = ArgumentCaptor.forClass(GraphiteUDP.class);
        verify(builderSpy).build(argument.capture());

        final GraphiteUDP graphite = argument.getValue();
        assertThat(graphite)
            .satisfies(g -> assertThat(g).extracting("hostname").isEqualTo("localhost"))
            .satisfies(g -> assertThat(g).extracting("port").isEqualTo(2003))
            .satisfies(g -> assertThat(g).extracting("address").isNull());
    }
}
