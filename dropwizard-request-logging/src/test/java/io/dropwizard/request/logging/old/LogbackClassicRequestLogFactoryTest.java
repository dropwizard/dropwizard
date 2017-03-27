package io.dropwizard.request.logging.old;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.logging.ConsoleAppenderFactory;
import io.dropwizard.logging.FileAppenderFactory;
import io.dropwizard.logging.SyslogAppenderFactory;
import io.dropwizard.request.logging.RequestLogFactory;
import io.dropwizard.validation.BaseValidator;
import org.eclipse.jetty.server.RequestLog;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

public class LogbackClassicRequestLogFactoryTest {

    static {
        BootstrapLogging.bootstrap();
    }

    private RequestLogFactory requestLog;

    @Before
    public void setUp() throws Exception {
        final ObjectMapper objectMapper = Jackson.newObjectMapper();
        objectMapper.getSubtypeResolver().registerSubtypes(ConsoleAppenderFactory.class, FileAppenderFactory.class,
            SyslogAppenderFactory.class);
        this.requestLog = new YamlConfigurationFactory<>(RequestLogFactory.class,
            BaseValidator.newValidator(), objectMapper, "dw")
            .build(new File(Resources.getResource("yaml/logbackClassicRequestLog.yml").toURI()));
    }

    @Test
    public void testDeserialized() {
        LogbackClassicRequestLogFactory classicRequestLogFactory = (LogbackClassicRequestLogFactory) requestLog;
        assertThat(classicRequestLogFactory.getTimeZone()).isEqualTo(TimeZone.getTimeZone("Europe/Amsterdam"));
        assertThat(classicRequestLogFactory.getAppenders()).hasSize(3).extractingResultOf("getClass").contains(
            ConsoleAppenderFactory.class, FileAppenderFactory.class, SyslogAppenderFactory.class
        );
    }

    @Test
    public void testBuild() {
        final RequestLog requestLog = this.requestLog.build("classic-request-log");
        assertThat(requestLog).isInstanceOf(DropwizardSlf4jRequestLog.class);
    }

    @Test
    public void isDiscoverable() throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
            .contains(LogbackClassicRequestLogFactory.class);
    }
}
