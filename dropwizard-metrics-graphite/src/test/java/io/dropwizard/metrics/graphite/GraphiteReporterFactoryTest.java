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
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class GraphiteReporterFactoryTest {

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
             BaseValidator.newValidator(), Jackson.newObjectMapper(), "dw")
            .build();
        assertThat(factory.getFrequency()).isNotPresent();
    }

    @Test
    void testNoAddressResolutionForGraphite() throws Exception {
        graphiteReporterFactory.build(new MetricRegistry());

        final ArgumentCaptor<Graphite> argument = ArgumentCaptor.forClass(Graphite.class);
        verify(builderSpy).build(argument.capture());

        final Graphite graphite = argument.getValue();
        assertThat(getField(graphite, "hostname")).isEqualTo("localhost");
        assertThat(getField(graphite, "port")).isEqualTo(2003);
        assertThat(getField(graphite, "address")).isNull();
    }

    @Test
    void testCorrectTransportForGraphiteUDP() throws Exception {
        graphiteReporterFactory.setTransport("udp");
        graphiteReporterFactory.build(new MetricRegistry());

        final ArgumentCaptor<GraphiteUDP> argument = ArgumentCaptor.forClass(GraphiteUDP.class);
        verify(builderSpy).build(argument.capture());

        final GraphiteUDP graphite = argument.getValue();
        assertThat(getField(graphite, "hostname")).isEqualTo("localhost");
        assertThat(getField(graphite, "port")).isEqualTo(2003);
        assertThat(getField(graphite, "address")).isNull();
    }

    private static Object getField(GraphiteUDP graphite, String name) throws NoSuchFieldException {
        try {
            return getInaccessibleField(GraphiteUDP.class, name).get(graphite);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Object getField(Graphite graphite, String name) throws NoSuchFieldException {
        try {
            return getInaccessibleField(Graphite.class, name).get(graphite);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Field getInaccessibleField(Class klass, String name) throws NoSuchFieldException {
        Field field = klass.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }
}
