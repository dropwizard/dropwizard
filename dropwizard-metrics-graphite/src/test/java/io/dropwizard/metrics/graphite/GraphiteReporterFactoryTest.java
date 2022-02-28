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
        final FieldAccessor<Graphite> graphiteFieldAccessor = new FieldAccessor<>(graphite);
        assertThat(graphiteFieldAccessor.getField("hostname")).isEqualTo("localhost");
        assertThat(graphiteFieldAccessor.getField("port")).isEqualTo(2003);
        assertThat(graphiteFieldAccessor.getField("address")).isNull();
    }

    @Test
    void testCorrectTransportForGraphiteUDP() throws Exception {
        graphiteReporterFactory.setTransport("udp");
        graphiteReporterFactory.build(new MetricRegistry());

        final ArgumentCaptor<GraphiteUDP> argument = ArgumentCaptor.forClass(GraphiteUDP.class);
        verify(builderSpy).build(argument.capture());

        final GraphiteUDP graphite = argument.getValue();
        final FieldAccessor<GraphiteUDP> graphiteUDPFieldAccessor = new FieldAccessor<>(graphite);
        assertThat(graphiteUDPFieldAccessor.getField("hostname")).isEqualTo("localhost");
        assertThat(graphiteUDPFieldAccessor.getField("port")).isEqualTo(2003);
        assertThat(graphiteUDPFieldAccessor.getField("address")).isNull();
    }

    private static class FieldAccessor<T> {
        T obj;

        FieldAccessor(T obj) {
            this.obj = obj;
        }

        Object getField(String name) throws IllegalAccessException, NoSuchFieldException {
            Field field = obj.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(obj);
        }
    }
}
