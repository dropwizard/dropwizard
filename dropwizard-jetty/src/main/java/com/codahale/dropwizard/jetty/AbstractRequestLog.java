package com.codahale.dropwizard.jetty;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import org.eclipse.jetty.server.AbstractNCSARequestLog;

import java.util.TimeZone;

/**
 * A base class to allow alternate request log implementations to be used. Really only needed to define
 * a common constructor signature.
 */
public abstract class AbstractRequestLog extends AbstractNCSARequestLog {

  private final AppenderAttachableImpl<ILoggingEvent> appenders;
  private final TimeZone timeZone;

  protected AbstractRequestLog( AppenderAttachableImpl<ILoggingEvent> appenders, TimeZone timeZone ) {
    this.appenders = appenders;
    this.timeZone = timeZone;
  }

}
