package io.dropwizard.request.logging.old;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.logging.ConsoleAppenderFactory;
import io.dropwizard.logging.FileAppenderFactory;
import io.dropwizard.logging.SyslogAppenderFactory;
import io.dropwizard.validation.BaseValidator;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

public class LogbackClassicRequestLogFactoryTest {

    static {
        BootstrapLogging.bootstrap();
    }

    private LogbackClassicRequestLogFactory requestLog;

    @Before
    public void setUp() throws Exception {
        final ObjectMapper objectMapper = Jackson.newObjectMapper();
        objectMapper.getSubtypeResolver().registerSubtypes(ConsoleAppenderFactory.class, FileAppenderFactory.class,
            SyslogAppenderFactory.class);
        this.requestLog = new YamlConfigurationFactory<>(LogbackClassicRequestLogFactory.class,
            BaseValidator.newValidator(), objectMapper, "dw")
            .build(new File(Resources.getResource("yaml/logbackClassicRequestLog.yml").toURI()));
    }

    @Test
    public void testDeserialized() {
        assertThat(requestLog.getTimeZone()).isEqualTo(TimeZone.getTimeZone("Europe/Amsterdam"));
        assertThat(requestLog.getAppenders()).hasSize(3).extractingResultOf("getClass").contains(
            ConsoleAppenderFactory.class, FileAppenderFactory.class, SyslogAppenderFactory.class
        );
    }
}
