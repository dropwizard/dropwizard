package io.dropwizard.request.logging;

import ch.qos.logback.access.jetty.RequestLogImpl;
import ch.qos.logback.access.common.spi.AccessEvent;
import ch.qos.logback.access.common.spi.IAccessEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.FilterReply;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.ee10.servlet.ServletApiRequest;
import org.eclipse.jetty.ee10.servlet.ServletApiResponse;
import org.eclipse.jetty.ee10.servlet.ServletContextRequest;
import org.eclipse.jetty.ee10.servlet.ServletContextResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;

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
        ServletContextRequest servletContextRequest = Request.as(jettyRequest, ServletContextRequest.class);
        if (servletContextRequest == null) {
            throw new IllegalStateException("Expecting request to be an instance of ServletContextRequest");
        }
        HttpServletRequest httpServletRequest = new DropwizardServletApiRequest(servletContextRequest);
        HttpServletResponse httpServletResponse = new DropwizardServletApiResponse(servletContextRequest.getServletContextResponse());
        IAccessEvent accessEvent = new AccessEvent(this, httpServletRequest, httpServletResponse, adapter);
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

    private static class DropwizardServletApiRequest extends ServletApiRequest {

        private final ServletContextRequest servletContextRequest;

        public DropwizardServletApiRequest(ServletContextRequest servletContextRequest) {
            super(servletContextRequest);
            this.servletContextRequest = servletContextRequest;
        }

        @Override
        public Request getRequest() {
            return servletContextRequest;
        }
    }

    private static class DropwizardServletApiResponse extends ServletApiResponse {

        private final ServletContextResponse servletContextResponse;

        public DropwizardServletApiResponse(ServletContextResponse servletContextResponse) {
            super(servletContextResponse);
            this.servletContextResponse = servletContextResponse;
        }

        @Override
        public Response getResponse() {
            return servletContextResponse;
        }
    }
}
