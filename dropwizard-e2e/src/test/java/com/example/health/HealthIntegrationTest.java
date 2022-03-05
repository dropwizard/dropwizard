package com.example.health;

import io.dropwizard.Configuration;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.awaitility.Awaitility;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Response;
import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
class HealthIntegrationTest {
    private static final String CONFIG_PATH = "health/config.yml";
    private static final String HOST = "localhost";
    private static final String APP_PORT_KEY = "server.connector.port";
    private static final String APP_PORT = "0";
    private static final String ENDPOINT = "/health-check";
    private static final String TEST_TIMEOUT_MS_OVERRIDE_ENV_VAR = "HEALTH_CHECK_TEST_TIMEOUT";
    private static final Duration APP_STARTUP_MAX_TIMEOUT = Duration.ofMinutes(1);
    private static final Duration POLL_DELAY = Duration.ofMillis(10);

    private static final Duration testTimeout = Optional.ofNullable(System.getenv(TEST_TIMEOUT_MS_OVERRIDE_ENV_VAR))
            .map(Long::parseLong)
            .map(Duration::ofMillis)
            // Default to 5 seconds
            .orElse(Duration.ofSeconds(5));

    public final DropwizardAppExtension<Configuration> TEST_APP_RULE = new DropwizardAppExtension<>(
            HealthApp.class,
            CONFIG_PATH,
            new ResourceConfigurationSourceProvider(),
            ConfigOverride.config(APP_PORT_KEY, APP_PORT));

    private final Client client = new JerseyClientBuilder().build();
    private String hostUrl;

    @BeforeEach
    public void setUp() {
        hostUrl = "http://" + HOST + ":" + TEST_APP_RULE.getLocalPort();
        Awaitility.waitAtMost(APP_STARTUP_MAX_TIMEOUT)
                .pollInSameThread()
                .pollDelay(POLL_DELAY)
                .until(this::isHealthCheckResponding);
    }

    @AfterEach
    public void tearDown() {
        this.client.close();
    }

    @Test
    void healthCheckShouldReportUnhealthyOnInitialStart() {
        assertThat(isAppHealthy()).isFalse();
    }

    @Test
    void healthCheckShouldReportHealthyWhenInitialStateFalseCriticalCheckGoesHealthy() {
        final HealthApp app = TEST_APP_RULE.getApplication();

        assertThat(isAppHealthy()).isFalse();

        app.getCriticalCheckHealthy1().set(true);
        app.getCriticalCheckHealthy2().set(true);

        Awaitility.waitAtMost(testTimeout)
                .pollInSameThread()
                .pollDelay(POLL_DELAY)
                .until(this::isAppHealthy);

        assertThat(app.getStateChangeCounter()).hasPositiveValue();
        assertThat(app.getHealthyCheckCounter()).hasPositiveValue();
        assertThat(app.getUnhealthyCheckCounter()).hasPositiveValue();
    }

    @Test
    void healthCheckShouldReportHealthyWhenAllHealthChecksHealthy() {
        final HealthApp app = TEST_APP_RULE.getApplication();
        app.getCriticalCheckHealthy1().set(true);
        app.getCriticalCheckHealthy2().set(true);
        app.getNonCriticalCheckHealthy().set(true);

        Awaitility.await()
                .pollInSameThread()
                .atMost(testTimeout)
                .pollDelay(POLL_DELAY)
                .until(this::isAppHealthy);
        assertThat(app.getStateChangeCounter()).hasPositiveValue();
        assertThat(app.getHealthyCheckCounter()).hasPositiveValue();
        assertThat(app.getUnhealthyCheckCounter()).hasPositiveValue();
    }

    @Test
    void nonCriticalHealthCheckFailureShouldNotResultInUnhealthyApp() {
        final HealthApp app = TEST_APP_RULE.getApplication();
        app.getCriticalCheckHealthy1().set(true);
        app.getCriticalCheckHealthy2().set(true);
        app.getNonCriticalCheckHealthy().set(false);

        Awaitility.await()
                .pollInSameThread()
                .atMost(testTimeout)
                .pollDelay(POLL_DELAY)
                .until(this::isAppHealthy);

        assertThat(app.getStateChangeCounter()).hasPositiveValue();
        assertThat(app.getHealthyCheckCounter()).hasPositiveValue();
        assertThat(app.getUnhealthyCheckCounter()).hasPositiveValue();
    }

    @Test
    void criticalHealthCheckFailureShouldResultInUnhealthyApp() {
        final HealthApp app = TEST_APP_RULE.getApplication();
        app.getCriticalCheckHealthy1().set(false);

        Awaitility.waitAtMost(testTimeout)
                .pollInSameThread()
                .pollDelay(POLL_DELAY)
                .until(() -> !isAppHealthy());
        // 2 state changes (to unhealthy) for critical checks, because of initial value of false
        assertThat(app.getStateChangeCounter()).hasPositiveValue();
        assertThat(app.getHealthyCheckCounter()).hasValue(0);
        assertThat(app.getUnhealthyCheckCounter()).hasPositiveValue();
    }

    @Test
    void appShouldRecoverOnceCriticalCheckReturnsToHealthyStatus() {
        final HealthApp app = TEST_APP_RULE.getApplication();
        app.getCriticalCheckHealthy1().set(false);

        Awaitility.waitAtMost(testTimeout)
                .pollInSameThread()
                .pollDelay(POLL_DELAY)
                .until(() -> !isAppHealthy());

        app.getCriticalCheckHealthy1().set(true);
        app.getCriticalCheckHealthy2().set(true);

        Awaitility.waitAtMost(testTimeout)
                .pollInSameThread()
                .pollDelay(POLL_DELAY)
                .until(this::isAppHealthy);

        assertThat(app.getStateChangeCounter()).hasPositiveValue();
        assertThat(app.getHealthyCheckCounter()).hasPositiveValue();
        assertThat(app.getUnhealthyCheckCounter()).hasPositiveValue();
    }

    private boolean isAppHealthy() {
        return client.target(hostUrl + ENDPOINT)
                .request()
                .get()
                .getStatus() == 200;
    }

    private boolean isHealthCheckResponding() {
        final Response response = client.target(hostUrl + ENDPOINT)
                .request()
                .get();
        return response.getStatus() == 200 || response.getStatus() == 503;
    }
}
