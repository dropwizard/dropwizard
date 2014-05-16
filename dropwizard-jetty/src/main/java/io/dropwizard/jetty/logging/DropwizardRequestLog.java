package io.dropwizard.jetty.logging;

import ch.qos.logback.access.jetty.RequestLogImpl;

public class DropwizardRequestLog extends RequestLogImpl {
    @Override
    public void configure() {
        // Override configure to skip configuration from logback-access.xml
    }
}
