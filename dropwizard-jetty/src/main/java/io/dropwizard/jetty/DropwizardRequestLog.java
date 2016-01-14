package io.dropwizard.jetty;

import ch.qos.logback.access.jetty.RequestLogImpl;

/**
 * The Dropwizard request log uses logback-access, but we override it to remove the requirement for logback-access.xml
 * based configuration.
 */
public class DropwizardRequestLog extends RequestLogImpl {
    @Override
    public void configure() {
        setName("DropwizardRequestLog");
    }
}
