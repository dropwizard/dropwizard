package io.dropwizard.health.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.health.HealthStateAggregator;
import io.dropwizard.health.HealthStatusChecker;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import java.util.Collections;
import java.util.List;
import jakarta.validation.Validator;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JsonHealthResponseProviderFactoryTest {
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
        assertThat(subtypes).contains(JsonHealthResponseProviderFactory.class);
    }

    @Test
    void testBuildJsonHealthResponseProvider() throws Exception {
        // given
        final HealthResponseProviderFactory factory = configFactory.build(new ResourceConfigurationSourceProvider(), "/yml/json-response-provider.yml");

        // when
        when(healthStatusChecker.isHealthy(isNull())).thenReturn(true);
        final HealthResponseProvider responseProvider = factory.build(healthStatusChecker, healthStateAggregator,
                mapper);
        final HealthResponse response = responseProvider.healthResponse(Collections.emptyMap());

        // then
        assertThat(response.isHealthy()).isTrue();
        assertThat(response.getMessage()).isEqualToIgnoringWhitespace("[]");
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        verifyNoInteractions(healthStateAggregator);
    }
}
