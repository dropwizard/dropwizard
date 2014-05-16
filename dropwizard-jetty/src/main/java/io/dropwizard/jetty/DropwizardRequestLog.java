package io.dropwizard.jetty;

import ch.qos.logback.access.jetty.RequestLogImpl;

public class DropwizardRequestLog extends RequestLogImpl {
    @Override
    public void configure() {
        // Override configure to skip configuration from logback-access.xml
    }
}
