package io.dropwizard.health;

import com.codahale.metrics.health.HealthCheckRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class HealthEnvironmentTest {
    private final HealthCheckRegistry healthChecks = new HealthCheckRegistry();

    private HealthEnvironment healthEnvironment;

    @Mock
    private HealthStateListener healthStateListener;
    @Mock
    private HealthStateListenerListener healthStateListenerListener;

    @BeforeEach
    void setUp() {
        healthEnvironment = new HealthEnvironment(healthChecks);
    }

    @Test
    void addingAHealthStateListenerBeforeProperInitializationShouldFail() {
        assertThrows(IllegalStateException.class, () -> healthEnvironment.addHealthStateListener(healthStateListener));
    }

    @Test
    void addingAHealthStateListenerShouldResultInListenerListenerCallback() {
        healthEnvironment.setHealthStateListenerListener(healthStateListenerListener);

        healthEnvironment.addHealthStateListener(healthStateListener);

        verify(healthStateListenerListener).onHealthStateListenerAdded(healthStateListener);
    }
}
