package io.dropwizard.request.logging;

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.ConsoleAppenderFactory;
import io.dropwizard.logging.FileAppenderFactory;
import io.dropwizard.logging.SyslogAppenderFactory;
import io.dropwizard.validation.BaseValidator;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RequestLogFactoryTest {
    private LogbackAccessRequestLogFactory logbackAccessRequestLogFactory;

    @Before
    public void setUp() throws Exception {
        final ObjectMapper objectMapper = Jackson.newObjectMapper();
        objectMapper.getSubtypeResolver().registerSubtypes(ConsoleAppenderFactory.class,
                                                           FileAppenderFactory.class,
                                                           SyslogAppenderFactory.class);
        this.logbackAccessRequestLogFactory = new YamlConfigurationFactory<>(LogbackAccessRequestLogFactory.class,
                                                     BaseValidator.newValidator(),
                                                     objectMapper, "dw")
                .build(new File(Resources.getResource("yaml/requestLog.yml").toURI()));
    }

    @Test
    public void fileAppenderFactoryIsSet() {
        assertThat(logbackAccessRequestLogFactory).isNotNull();
        assertThat(logbackAccessRequestLogFactory.getAppenders()).isNotNull();
        assertThat(logbackAccessRequestLogFactory.getAppenders().size()).isEqualTo(1);
        assertThat(logbackAccessRequestLogFactory.getAppenders().get(0))
            .isInstanceOf(FileAppenderFactory.class);
    }

    @Test
    public void isDiscoverable() throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
            .contains(LogbackAccessRequestLogFactory.class);
    }
}
