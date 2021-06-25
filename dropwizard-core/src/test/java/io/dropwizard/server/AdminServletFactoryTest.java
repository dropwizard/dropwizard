package io.dropwizard.server;

import static org.assertj.core.api.Assertions.assertThat;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.AdminServlet;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.util.Resources;
import io.dropwizard.validation.BaseValidator;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.jupiter.api.Test;

class AdminServletFactoryTest {
//    private AdminServletFactory adminServlet;
//
//    @BeforeEach
//    void setUp() throws Exception {
//        this.adminServlet = new YamlConfigurationFactory<>(AdminServletFactory.class,
//            BaseValidator.newValidator(), Jackson.newObjectMapper(), "dw")
//            .build(new File(Resources.getResource("yaml/adminservlet.yml").toURI()));
//    }
//
//    @Test
//    void servletConfigurations() {
//        final Map<String, ServletConfiguration> servletConfigurations = adminServlet.getServletConfigurations();
//
//        assertThat(servletConfigurations).isNotNull().hasSize(5);
//        final ServletConfiguration metricsConfiguration = servletConfigurations.get("metrics");
//        assertThat(metricsConfiguration).isNotNull();
//        assertThat(metricsConfiguration.isEnabled()).isFalse();
//        assertThat(metricsConfiguration.getUri()).isEqualTo("/metrics-test");
//
//        assertThat(servletConfigurations.get("ping")).isNotNull();
//        assertThat(servletConfigurations.get("threads")).isNotNull();
//        assertThat(servletConfigurations.get("healthcheck")).isNotNull();
//        assertThat(servletConfigurations.get("cpuProfile")).isNotNull();
//    }

    @Test
    void testBuild() throws URISyntaxException, ConfigurationException, IOException {
        AdminServletFactory adminServlet = new YamlConfigurationFactory<>(AdminServletFactory.class,
            BaseValidator.newValidator(), Jackson.newObjectMapper(), "dw")
            .build(new File(Resources.getResource("yaml/adminservlet.yml").toURI()));

        final MutableServletContextHandler handler = new MutableServletContextHandler();
        final MetricRegistry metricRegistry = new MetricRegistry();
        final HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();
        adminServlet.addServlet(handler, metricRegistry, healthCheckRegistry);

        assertThat(handler.getServletHandler().getServlets(AdminServlet.class)).hasSize(1);

        final ServletHolder servletHolder = handler.getServletHandler().getServlets(AdminServlet.class).get(0);
        assertThat(servletHolder.getInitParameter(AdminServlet.METRICS_ENABLED_PARAM_KEY))
            .isEqualTo(Boolean.FALSE.toString());
        assertThat(servletHolder.getInitParameter(AdminServlet.METRICS_URI_PARAM_KEY))
            .isEqualTo(AdminServlet.DEFAULT_METRICS_URI + "-test");
        assertThat(servletHolder.getInitParameter(AdminServlet.PING_ENABLED_PARAM_KEY))
            .isEqualTo(Boolean.FALSE.toString());
        assertThat(servletHolder.getInitParameter(AdminServlet.PING_URI_PARAM_KEY))
            .isEqualTo(AdminServlet.DEFAULT_PING_URI + "-test");
        assertThat(servletHolder.getInitParameter(AdminServlet.THREADS_ENABLED_PARAM_KEY))
            .isEqualTo(Boolean.FALSE.toString());
        assertThat(servletHolder.getInitParameter(AdminServlet.THREADS_URI_PARAM_KEY))
            .isEqualTo(AdminServlet.DEFAULT_THREADS_URI + "-test");
        assertThat(servletHolder.getInitParameter(AdminServlet.HEALTHCHECK_ENABLED_PARAM_KEY))
            .isEqualTo(Boolean.FALSE.toString());
        assertThat(servletHolder.getInitParameter(AdminServlet.HEALTHCHECK_URI_PARAM_KEY))
            .isEqualTo(AdminServlet.DEFAULT_HEALTHCHECK_URI + "-test");
        assertThat(servletHolder.getInitParameter(AdminServlet.CPU_PROFILE_ENABLED_PARAM_KEY))
            .isEqualTo(Boolean.FALSE.toString());
        assertThat(servletHolder.getInitParameter(AdminServlet.CPU_PROFILE_URI_PARAM_KEY))
            .isEqualTo(AdminServlet.DEFAULT_CPU_PROFILE_URI + "-test");

        assertThat(handler.getServletContext().getAttribute(MetricsServlet.METRICS_REGISTRY)).isEqualTo(metricRegistry);
        assertThat(handler.getServletContext().getAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY))
            .isEqualTo(healthCheckRegistry);
    }

    @Test
    void testBuildEmpty() throws URISyntaxException, ConfigurationException, IOException {
        AdminServletFactory adminServlet = new YamlConfigurationFactory<>(AdminServletFactory.class,
            BaseValidator.newValidator(), Jackson.newObjectMapper(), "dw")
            .build(new File(Resources.getResource("yaml/adminservlet_empty.yml").toURI()));

        final MutableServletContextHandler handler = new MutableServletContextHandler();
        final MetricRegistry metricRegistry = new MetricRegistry();
        final HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();
        adminServlet.addServlet(handler, metricRegistry, healthCheckRegistry);

        assertThat(handler.getServletHandler().getServlets(AdminServlet.class)).hasSize(1);

        final ServletHolder servletHolder = handler.getServletHandler().getServlets(AdminServlet.class).get(0);
        assertThat(servletHolder.getInitParameter(AdminServlet.METRICS_ENABLED_PARAM_KEY)).isNull();
        assertThat(servletHolder.getInitParameter(AdminServlet.METRICS_URI_PARAM_KEY)).isNull();
        assertThat(servletHolder.getInitParameter(AdminServlet.PING_ENABLED_PARAM_KEY)).isNull();
        assertThat(servletHolder.getInitParameter(AdminServlet.PING_URI_PARAM_KEY)).isNull();
        assertThat(servletHolder.getInitParameter(AdminServlet.THREADS_ENABLED_PARAM_KEY)).isNull();
        assertThat(servletHolder.getInitParameter(AdminServlet.THREADS_URI_PARAM_KEY)).isNull();
        assertThat(servletHolder.getInitParameter(AdminServlet.HEALTHCHECK_ENABLED_PARAM_KEY)).isNull();
        assertThat(servletHolder.getInitParameter(AdminServlet.HEALTHCHECK_URI_PARAM_KEY)).isNull();
        assertThat(servletHolder.getInitParameter(AdminServlet.CPU_PROFILE_ENABLED_PARAM_KEY)).isNull();
        assertThat(servletHolder.getInitParameter(AdminServlet.CPU_PROFILE_URI_PARAM_KEY)).isNull();

        assertThat(handler.getServletContext().getAttribute(MetricsServlet.METRICS_REGISTRY)).isEqualTo(metricRegistry);
        assertThat(handler.getServletContext().getAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY))
            .isEqualTo(healthCheckRegistry);
    }

    @Test
    void testBuildPartial() throws URISyntaxException, ConfigurationException, IOException {
        AdminServletFactory adminServlet = new YamlConfigurationFactory<>(AdminServletFactory.class,
            BaseValidator.newValidator(), Jackson.newObjectMapper(), "dw")
            .build(new File(Resources.getResource("yaml/adminservlet_partial.yml").toURI()));

        final MutableServletContextHandler handler = new MutableServletContextHandler();
        final MetricRegistry metricRegistry = new MetricRegistry();
        final HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();
        adminServlet.addServlet(handler, metricRegistry, healthCheckRegistry);

        assertThat(handler.getServletHandler().getServlets(AdminServlet.class)).hasSize(1);

        final ServletHolder servletHolder = handler.getServletHandler().getServlets(AdminServlet.class).get(0);
        assertThat(servletHolder.getInitParameter(AdminServlet.METRICS_ENABLED_PARAM_KEY))
            .isEqualTo(Boolean.TRUE.toString());
        assertThat(servletHolder.getInitParameter(AdminServlet.METRICS_URI_PARAM_KEY))
            .isEqualTo(AdminServlet.DEFAULT_METRICS_URI + "-test");
        assertThat(servletHolder.getInitParameter(AdminServlet.PING_ENABLED_PARAM_KEY))
            .isEqualTo(Boolean.TRUE.toString());
        assertThat(servletHolder.getInitParameter(AdminServlet.PING_URI_PARAM_KEY))
            .isEqualTo(AdminServlet.DEFAULT_PING_URI + "-test");
        assertThat(servletHolder.getInitParameter(AdminServlet.THREADS_ENABLED_PARAM_KEY))
            .isEqualTo(Boolean.FALSE.toString());
        assertThat(servletHolder.getInitParameter(AdminServlet.THREADS_URI_PARAM_KEY)).isNull();
        assertThat(servletHolder.getInitParameter(AdminServlet.HEALTHCHECK_ENABLED_PARAM_KEY)).isNull();
        assertThat(servletHolder.getInitParameter(AdminServlet.HEALTHCHECK_URI_PARAM_KEY)).isNull();
        assertThat(servletHolder.getInitParameter(AdminServlet.CPU_PROFILE_ENABLED_PARAM_KEY)).isNull();
        assertThat(servletHolder.getInitParameter(AdminServlet.CPU_PROFILE_URI_PARAM_KEY)).isNull();

        assertThat(handler.getServletContext().getAttribute(MetricsServlet.METRICS_REGISTRY)).isEqualTo(metricRegistry);
        assertThat(handler.getServletContext().getAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY))
            .isEqualTo(healthCheckRegistry);
    }
}
