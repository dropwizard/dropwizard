package io.dropwizard.logging.json;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ConfigurationValidationException;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.common.BootstrapLogging;
import io.dropwizard.logging.common.ConsoleAppenderFactory;
import io.dropwizard.logging.common.DefaultLoggingFactory;
import io.dropwizard.validation.BaseValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import jakarta.validation.constraints.Min;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.entry;
import static org.awaitility.Awaitility.await;

class LayoutIntegrationTests {

    static {
        BootstrapLogging.bootstrap(Level.INFO, new EventJsonLayoutBaseFactory());
    }

    private final ObjectMapper objectMapper = Jackson.newObjectMapper();

    @SuppressWarnings("rawtypes")
    private final YamlConfigurationFactory<ConsoleAppenderFactory> yamlFactory = new YamlConfigurationFactory<>(
        ConsoleAppenderFactory.class, BaseValidator.newValidator(), objectMapper, "dw-json-log");

    @BeforeEach
    void setUp() {
        objectMapper.getSubtypeResolver().registerSubtypes(EventJsonLayoutBaseFactory.class);
    }

    @SuppressWarnings("unchecked")
    private <T extends DeferredProcessingAware> ConsoleAppenderFactory<T> getAppenderFactory(String s) throws Exception {
        return yamlFactory.build(new ResourceConfigurationSourceProvider(), s);
    }

    @Test
    void testDeserializeJson() throws Exception {
        assertThat(getAppenderFactory("yaml/json-log.yml"))
            .extracting(ConsoleAppenderFactory::getLayout)
            .isInstanceOfSatisfying(EventJsonLayoutBaseFactory.class, eventJsonLayoutBaseFactory -> assertThat(eventJsonLayoutBaseFactory)
                .satisfies(factory -> assertThat(factory).isNotNull())
                .satisfies(factory -> assertThat(factory.getTimestampFormat()).isEqualTo("yyyy-MM-dd'T'HH:mm:ss.SSSZ"))
                .satisfies(factory -> assertThat(factory.isPrettyPrint()).isFalse())
                .satisfies(factory -> assertThat(factory.isAppendLineSeparator()).isTrue())
                .satisfies(factory -> assertThat(factory.getIncludes()).contains(
                    EventAttribute.LEVEL,
                    EventAttribute.MDC,
                    EventAttribute.MESSAGE,
                    EventAttribute.LOGGER_NAME,
                    EventAttribute.EXCEPTION,
                    EventAttribute.TIMESTAMP,
                    EventAttribute.CALLER_DATA))
                .satisfies(factory -> assertThat(factory.isFlattenMdc()).isTrue())
                .satisfies(factory -> assertThat(factory.getCustomFieldNames()).containsOnly(entry("timestamp", "@timestamp")))
                .satisfies(factory -> assertThat(factory.getAdditionalFields()).containsOnly(
                    entry("service-name", "user-service"),
                    entry("service-build", 218)))
                .satisfies(factory -> assertThat(factory.getIncludesMdcKeys()).containsOnly("userId"))
                .extracting(EventJsonLayoutBaseFactory::getExceptionFormat)
                .satisfies(exceptionFormat -> assertThat(exceptionFormat.getDepth()).isEqualTo("10"))
                .satisfies(exceptionFormat -> assertThat(exceptionFormat.isRootFirst()).isFalse())
                .satisfies(exceptionFormat -> assertThat(exceptionFormat.getEvaluators()).contains("io.dropwizard")));
    }

    @Test
    void testLogJsonToConsole() throws Exception {
        ConsoleAppenderFactory<ILoggingEvent> consoleAppenderFactory = getAppenderFactory("yaml/json-log-default.yml");
        DefaultLoggingFactory defaultLoggingFactory = new DefaultLoggingFactory();
        defaultLoggingFactory.setAppenders(Collections.singletonList(consoleAppenderFactory));

        assertThat(consoleAppenderFactory.getLayout())
            .isInstanceOfSatisfying(EventJsonLayoutBaseFactory.class, eventJsonLayoutBaseFactory -> assertThat(eventJsonLayoutBaseFactory)
                .satisfies(factory -> assertThat(factory).isNotNull())
                .satisfies(factory -> assertThat(factory.getIncludes()).contains(
                    EventAttribute.LEVEL,
                    EventAttribute.THREAD_NAME,
                    EventAttribute.MDC,
                    EventAttribute.MARKER,
                    EventAttribute.LOGGER_NAME,
                    EventAttribute.MESSAGE,
                    EventAttribute.EXCEPTION,
                    EventAttribute.TIMESTAMP))
                .satisfies(factory -> assertThat(factory.isFlattenMdc()).isFalse())
                .satisfies(factory -> assertThat(factory.getIncludesMdcKeys()).isEmpty())
                .satisfies(factory -> assertThat(factory.getExceptionFormat()).isNull()));

        PrintStream old = System.out;
        ByteArrayOutputStream redirectedStream = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(redirectedStream));
            defaultLoggingFactory.configure(new MetricRegistry(), "json-log-test");
            Marker marker = MarkerFactory.getMarker("marker");
            LoggerFactory.getLogger("com.example.app").info(marker, "Application log");
            // Need to wait, because the logger is async
            await().atMost(1, TimeUnit.SECONDS).until(() -> !redirectedStream.toString().isEmpty());

            JsonNode jsonNode = objectMapper.readTree(redirectedStream.toString());
            assertThat(jsonNode.fieldNames().next()).isEqualTo("timestamp");
            assertThat(jsonNode.get("timestamp").isTextual()).isTrue();
            assertThat(jsonNode.get("level").asText()).isEqualTo("INFO");
            assertThat(jsonNode.get("logger").asText()).isEqualTo("com.example.app");
            assertThat(jsonNode.get("marker").asText()).isEqualTo("marker");
            assertThat(jsonNode.get("message").asText()).isEqualTo("Application log");
        } finally {
            System.setOut(old);
        }
    }

    @Test
    void invalidJsonLogLayoutField() {
        assertThatExceptionOfType(ConfigurationValidationException.class)
            .isThrownBy(() -> getAppenderFactory("yaml/custom-json-log-invalid.yml"))
            .withMessageContaining("messageSize must be greater than or equal to 1");
    }

    @JsonTypeName("custom-json")
    public static class CustomJsonLayoutBaseFactory extends EventJsonLayoutBaseFactory {

        @JsonProperty
        @Min(1)
        private int messageSize = 8000;
    }
}
