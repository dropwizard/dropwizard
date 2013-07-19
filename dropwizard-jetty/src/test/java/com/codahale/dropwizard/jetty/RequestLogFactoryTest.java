package com.codahale.dropwizard.jetty;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import com.codahale.dropwizard.configuration.ConfigurationFactory;
import com.codahale.dropwizard.jackson.Jackson;
import com.codahale.dropwizard.logging.ConsoleAppenderFactory;
import com.codahale.dropwizard.logging.FileAppenderFactory;
import com.codahale.dropwizard.logging.SyslogAppenderFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;

import javax.validation.Validation;
import java.io.File;
import java.util.TimeZone;

import static org.fest.assertions.api.Assertions.assertThat;

public class RequestLogFactoryTest {
    private RequestLogFactory requestLog;

    @Before
    public void setUp() throws Exception {
        final ObjectMapper objectMapper = Jackson.newObjectMapper();
        objectMapper.getSubtypeResolver().registerSubtypes(ConsoleAppenderFactory.class,
                                                           FileAppenderFactory.class,
                                                           SyslogAppenderFactory.class);
        this.requestLog = new ConfigurationFactory<>(RequestLogFactory.class,
                                                     Validation.buildDefaultValidatorFactory()
                                                                       .getValidator(),
                                                     objectMapper, "dw")
                .build(new File(Resources.getResource("yaml/requestLog.yml").toURI()));
    }

  @Test
  public void defaultTimeZoneIsUTC() {
    assertThat(requestLog.getTimeZone())
        .isEqualTo(TimeZone.getTimeZone("UTC"));
  }

  @Test
  public void logClassIsDefault() {
    assertThat(requestLog.build("test").getClass().getName())
        .isEqualTo( Slf4jRequestLog.class.getName() );
  }

  @Test
  public void alternateLogClass() throws Exception {
    RequestLogFactory customRequestLog = new ConfigurationFactory<>(
        RequestLogFactory.class,
        Validation.buildDefaultValidatorFactory().getValidator(),
        Jackson.newObjectMapper(), "dw" )
        .build( new File( Resources.getResource( "yaml/requestLog-alternateLogClass.yml" ).toURI() ) );

    assertThat( customRequestLog.build( "test" ).getClass().getName() )
        .isEqualTo( AlternateRequestLog.class.getName() );
  }

}

class AlternateRequestLog extends Slf4jRequestLog {

  AlternateRequestLog( AppenderAttachableImpl<ILoggingEvent> appenders, TimeZone timeZone ) {
    super( appenders, timeZone );
  }

}
