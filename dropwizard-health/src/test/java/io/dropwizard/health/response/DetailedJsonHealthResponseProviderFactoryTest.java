package io.dropwizard.health.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.health.HealthStateAggregator;
import io.dropwizard.health.HealthStatusChecker;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.Validator;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DetailedJsonHealthResponseProviderFactoryTest {
    private final ObjectMapper mapper = Jackson.newObjectMapper();
    private final Validator validator = Validators.newValidator();
    private final YamlConfigurationFactory<HealthResponseProviderFactory> configFactory =
        new YamlConfigurationFactory<>(HealthResponseProviderFactory.class, validator, mapper, "dw");

    @Mock
    private HealthStatusChecker healthStatusChecker;
    @Mock
    private HealthStateAggregator healthStateAggregator;

    @Test
    void isDiscoverable() {
        // given
        final DiscoverableSubtypeResolver resolver = new DiscoverableSubtypeResolver();

        // when
        final List<Class<?>> subtypes = resolver.getDiscoveredSubtypes();

        // then
        assertThat(subtypes).contains(DetailedJsonHealthResponseProviderFactory.class);
    }

    @Test
    void testBuildDetailedJsonHealthResponseProvider() throws Exception {
        // given
        final File yml = new File(Resources.getResource("yml/detailed-json-response-provider.yml").toURI());

        // when
        when(healthStatusChecker.isHealthy(isNull())).thenReturn(true);
        final HealthResponseProviderFactory factory = configFactory.build(yml);
        final HealthResponseProvider responseProvider = factory.build(healthStatusChecker, healthStateAggregator,
            mapper);
        final HealthResponse response = responseProvider.minimalHealthResponse(null);

        // then
        assertThat(response.isHealthy()).isTrue();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        verifyNoInteractions(healthStateAggregator);
    }
}
