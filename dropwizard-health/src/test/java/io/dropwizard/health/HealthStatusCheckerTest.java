package io.dropwizard.health;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class HealthStatusCheckerTest {
    @Test
    void testHealthStatusChecker() {
        HealthStatusChecker healthStatusChecker = mock(HealthStatusChecker.class);
        assertThat(healthStatusChecker.isHealthy()).isEqualTo(healthStatusChecker.isHealthy(null));
    }
}
