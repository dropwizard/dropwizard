package io.dropwizard.metrics.graphite;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.google.common.base.Optional;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.validation.BaseValidator;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class GraphiteReporterFactoryTest {

    @Test
    public void isDiscoverable() throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
            .contains(GraphiteReporterFactory.class);
    }

    @Test
    public void createDefaultFactory() throws Exception {
        final GraphiteReporterFactory factory = new ConfigurationFactory<>(GraphiteReporterFactory.class,
            BaseValidator.newValidator(), Jackson.newObjectMapper(), "dw")
            .build();
        assertThat(factory.getFrequency()).isEqualTo(Optional.absent());
    }

    @Test
    public void testNoAddressResolutionForGraphite() throws Exception {
        final GraphiteReporter.Builder builderSpy = mock(GraphiteReporter.Builder.class);
        new GraphiteReporterFactory() {
            @Override
            protected GraphiteReporter.Builder builder(MetricRegistry registry) {
                return builderSpy;
            }
        }.build(new MetricRegistry());

        final ArgumentCaptor<Graphite> argument = ArgumentCaptor.forClass(Graphite.class);
        verify(builderSpy).build(argument.capture());

        final Graphite graphite = argument.getValue();
        assertThat(getField(graphite, "hostname")).isEqualTo("localhost");
        assertThat(getField(graphite, "port")).isEqualTo(8080);
        assertThat(getField(graphite, "address")).isNull();
    }

    private static Object getField(Graphite graphite, String name) {
        try {
            return FieldUtils.getDeclaredField(Graphite.class, name, true).get(graphite);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
