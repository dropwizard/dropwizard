package io.dropwizard.health;

import com.codahale.metrics.health.HealthCheckRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

@ExtendWith(MockitoExtension.class)
class HealthEnvironmentTest {
    private final HealthCheckRegistry healthChecks = new HealthCheckRegistry();

    private HealthEnvironment healthEnvironment;

    @Mock
    private HealthStateListener healthStateListener;

    @BeforeEach
    void setUp() {
        healthEnvironment = new HealthEnvironment(healthChecks);
    }

    @Test
    void gettingHealthStateAggregatorBeforeSetShouldResultInException() {
        assertThatIllegalStateException()
            .isThrownBy(healthEnvironment::healthStateAggregator);
    }

    @Test
    void shouldRegisterAListener() {
        healthEnvironment.addHealthStateListener(healthStateListener);

        assertThat(healthEnvironment.healthStateListeners()).contains(healthStateListener);
    }
}
