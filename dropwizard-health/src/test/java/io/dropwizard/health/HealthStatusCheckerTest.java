package io.dropwizard.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

class HealthStatusCheckerTest {
    @Test
    void testHealthStatusChecker() {
        HealthStatusChecker healthStatusChecker = mock(HealthStatusChecker.class);
        assertThat(healthStatusChecker.isHealthy()).isEqualTo(healthStatusChecker.isHealthy(null));
    }
}
