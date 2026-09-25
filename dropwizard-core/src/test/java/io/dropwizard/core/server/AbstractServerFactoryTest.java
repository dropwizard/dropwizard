package io.dropwizard.core.server;

import com.codahale.metrics.annotation.ResponseMeteredLevel;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.setup.JerseyContainerHolder;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.logging.common.ConsoleAppenderFactory;
import io.dropwizard.request.logging.ExternalRequestLogFactory;
import io.dropwizard.request.logging.LogbackAccessRequestLog;
import io.dropwizard.request.logging.LogbackAccessRequestLogAwareHandler;
import io.dropwizard.request.logging.LogbackAccessRequestLogFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests that the {@link JerseyEnvironment#getUrlPattern()} is set by the following priority order:
 * <ol>
 *     <li>YAML defined value</li>
 *     <li>{@link Application#run(Configuration, Environment)} defined value</li>
 *     <li>Default value defined by {@link DropwizardResourceConfig#urlPattern}</li>
 * </ol>
 */
class AbstractServerFactoryTest {

    private final JerseyContainerHolder holder = mock(JerseyContainerHolder.class);
    private final DropwizardResourceConfig config = new DropwizardResourceConfig();
    private final JerseyEnvironment jerseyEnvironment = new JerseyEnvironment(holder, config);
    private final Environment environment = mock(Environment.class, RETURNS_DEEP_STUBS);
    private final TestServerFactory serverFactory = new TestServerFactory();

    private static final String DEFAULT_PATTERN = "/*";
    private static final String RUN_SET_PATTERN = "/set/from/run/*";
    private static final String YAML_SET_PATTERN = "/set/from/yaml/*";

    @BeforeEach
    void before() {
        when(environment.jersey()).thenReturn(jerseyEnvironment);
        when(environment.getApplicationContext()).thenReturn(new MutableServletContextHandler());
    }

    @Test
    void usesYamlDefinedPattern() {
        serverFactory.setJerseyRootPath(YAML_SET_PATTERN);
        jerseyEnvironment.setUrlPattern(RUN_SET_PATTERN);

        serverFactory.build(environment);

        assertThat(jerseyEnvironment.getUrlPattern()).isEqualTo(YAML_SET_PATTERN);
    }

    @Test
    void usesRunDefinedPatternWhenNoYaml() {
        jerseyEnvironment.setUrlPattern(RUN_SET_PATTERN);

        serverFactory.build(environment);

        assertThat(jerseyEnvironment.getUrlPattern()).isEqualTo(RUN_SET_PATTERN);
    }

    @Test
    void usesDefaultPatternWhenNoneSet() {
        serverFactory.build(environment);

        assertThat(jerseyEnvironment.getUrlPattern()).isEqualTo(DEFAULT_PATTERN);
    }

    @Test
    void usesDefaultResponseMeteredLevelWhenNotSet() {
        assertThat(serverFactory.getResponseMeteredLevel()).isEqualTo(ResponseMeteredLevel.COARSE);
    }

    @Test
    void usesDefaultMetricPrefixWhenNotSet() {
        assertThat(serverFactory.getMetricPrefix()).isNull();
    }

    @Test
    void addRequestLogWithLogbackAddsSpecialHandler() {
        LogbackAccessRequestLogFactory requestLogFactory = new LogbackAccessRequestLogFactory();
        requestLogFactory.setAppenders(List.of(new ConsoleAppenderFactory<>()));
        serverFactory.setRequestLogFactory(requestLogFactory);

        Server server = serverFactory.build(environment);

        assertThat(server.getRequestLog()).isNotNull();
        assertThat(server.getRequestLog()).isInstanceOf(LogbackAccessRequestLog.class);

        // Check that LogbackAccessRequestLogAwareHandler was added to the application context
        MutableServletContextHandler appContext = environment.getApplicationContext();
        assertThat(appContext.getHandler()).isInstanceOf(LogbackAccessRequestLogAwareHandler.class);
    }

    @Test
    void addRequestLogWithoutLogbackDoesNotAddSpecialHandler() {
        ExternalRequestLogFactory requestLogFactory = new ExternalRequestLogFactory();
        requestLogFactory.setEnabled(true);
        serverFactory.setRequestLogFactory(requestLogFactory);

        Server server = serverFactory.build(environment);

        assertThat(server.getRequestLog()).isNotNull();
        assertThat(server.getRequestLog()).isNotInstanceOf(LogbackAccessRequestLog.class);

        // Check that no LogbackAccessRequestLogAwareHandler was added
        MutableServletContextHandler appContext = environment.getApplicationContext();
        assertThat(appContext.getHandler()).isNotInstanceOf(LogbackAccessRequestLogAwareHandler.class);
    }

    @Test
    void registersCspFilterWhenCspConfigured() {
        serverFactory.getCsp().setPolicy("default-src 'self'; script-src 'nonce-$NONCE';");
        serverFactory.build(environment);

        final boolean hasCspFilter = config.getSingletons().stream()
                .anyMatch(s -> s instanceof io.dropwizard.jersey.filter.CspFilter);
        assertThat(hasCspFilter).isTrue();
    }

    @Test
    void registersCspFilterWhenCspReportOnlyConfigured() {
        serverFactory.getCsp().setReportOnlyPolicy("default-src 'self';");
        serverFactory.build(environment);

        final boolean hasCspFilter = config.getSingletons().stream()
                .anyMatch(s -> s instanceof io.dropwizard.jersey.filter.CspFilter);
        assertThat(hasCspFilter).isTrue();
    }

    @Test
    void doesNotRegisterCspFilterWhenNotConfigured() {
        serverFactory.build(environment);

        final boolean hasCspFilter = config.getSingletons().stream()
                .anyMatch(s -> s instanceof io.dropwizard.jersey.filter.CspFilter);
        assertThat(hasCspFilter).isFalse();
    }

    @Test
    void cspPolicyWithoutNoncePlaceholderFailsValidation() {
        final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        final CspConfiguration cspConfig = new CspConfiguration();
        cspConfig.setPolicy("default-src 'self';"); // missing $NONCE

        final Set<ConstraintViolation<CspConfiguration>> violations = validator.validate(cspConfig);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getMessage().contains("$NONCE") &&
                v.getMessage().contains("policy"));
    }

    @Test
    void cspReportOnlyPolicyWithoutNoncePlaceholderFailsValidation() {
        final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        final CspConfiguration cspConfig = new CspConfiguration();
        cspConfig.setReportOnlyPolicy("default-src 'self';"); // missing $NONCE

        final Set<ConstraintViolation<CspConfiguration>> violations = validator.validate(cspConfig);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getMessage().contains("$NONCE") &&
                v.getMessage().contains("report-only"));
    }

    @Test
    void cspPolicyWithNoncePlaceholderPassesValidation() {
        final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        final CspConfiguration cspConfig = new CspConfiguration();
        cspConfig.setPolicy("default-src 'self'; script-src 'nonce-$NONCE';");
        cspConfig.setReportOnlyPolicy("default-src 'self'; script-src 'nonce-$NONCE';");

        final Set<ConstraintViolation<CspConfiguration>> violations = validator.validate(cspConfig);

        assertThat(violations).isEmpty();
    }

    /**
     * Test implementation of {@link AbstractServerFactory} used to run {@link #createAppServlet}, which triggers the
     * setting of {@link JerseyEnvironment#setUrlPattern(String)}.
     */
    public static class TestServerFactory extends AbstractServerFactory {
        @Override
        public Server build(Environment environment) {
            // mimics the current default + simple server factory build() methods
            ThreadPool threadPool = createThreadPool(environment.metrics());
            Server server = buildServer(environment.lifecycle(), threadPool);
            createAppServlet(server,
                                  environment.jersey(),
                                  environment.getObjectMapper(),
                                  environment.getValidator(),
                                  environment.getApplicationContext(),
                                  environment.getJerseyServletContainer(),
                                  environment.metrics());
            addRequestLog(server, environment.getName(), environment.getApplicationContext());
            return server;
        }

        @Override
        public void configure(Environment environment) {
            // left blank intentionally
        }
    }
}
