package io.dropwizard.request.logging;

import ch.qos.logback.access.jetty.RequestLogImpl;
import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.FilterReply;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;

import java.util.EventListener;
import java.util.Iterator;

/**
 * The Dropwizard request log uses logback-access, but we override it to remove the requirement for logback-access.xml
 * based configuration.
 */
public class LogbackAccessRequestLog extends RequestLogImpl {
    @Override
    public void configure() {
        setName("LogbackAccessRequestLog");
    }

    @Override
    public void log(Request jettyRequest, Response jettyResponse) {
        DropwizardJettyServerAdapter adapter = new DropwizardJettyServerAdapter(jettyRequest, jettyResponse);
        IAccessEvent accessEvent = new AccessEvent(this, jettyRequest, jettyResponse, adapter);
        if (getFilterChainDecision(accessEvent) == FilterReply.DENY) {
            return;
        }
        appendLoopOnAppenders(accessEvent);
    }

    private void appendLoopOnAppenders(IAccessEvent iAccessEvent) {
        Iterator<Appender<IAccessEvent>> appenderIterator = this.iteratorForAppenders();
        while (appenderIterator.hasNext()) {
            appenderIterator.next().doAppend(iAccessEvent);
        }
    }

    @Override
    public boolean addEventListener(EventListener eventListener) {
        if (eventListener instanceof Listener) {
            addLifeCycleListener((Listener) eventListener);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeEventListener(EventListener eventListener) {
        if (eventListener instanceof Listener) {
            removeLifeCycleListener((Listener) eventListener);
            return true;
        }
        return false;
    }
}
