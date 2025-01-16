package io.dropwizard.request.logging;

import ch.qos.logback.access.jetty.JettyServerAdapter;
import ch.qos.logback.access.jetty.RequestLogImpl;
import ch.qos.logback.access.jetty.RequestWrapper;
import ch.qos.logback.access.jetty.ResponseWrapper;
import ch.qos.logback.access.common.spi.AccessEvent;
import ch.qos.logback.access.common.spi.IAccessEvent;
import ch.qos.logback.access.common.spi.ServerAdapter;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.FilterReply;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * The Dropwizard request log uses logback-access, but we override it to remove the requirement for logback-access.xml
 * based configuration.
 */
public class LogbackAccessRequestLog extends RequestLogImpl {
    @Override
    public void configure() {
        setName("LogbackAccessRequestLog");
    }

    private static Map<String, String> buildHeaderMap(HttpFields headers) {
        Map<String, String> headerMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (HttpField f : headers) {
            String existing = headerMap.get(f.getName());
            String value = existing == null ? f.getValue() : existing + "," + f.getValue();
            headerMap.put(f.getName(), value);
        }
        return headerMap;
    }

    @Override
    public void log(Request jettyRequest, Response jettyResponse) {
        // TODO: remove build*HeaderMap overrides once https://github.com/qos-ch/logback-access/pull/23
        //       is merged and released in logback-access
        HttpServletRequest httpServletRequest = new RequestWrapper(jettyRequest) {
            @Override
            public Map<String, String> buildRequestHeaderMap() {
                return buildHeaderMap(jettyRequest.getHeaders());
            }
        };
        HttpServletResponse httpServletResponse = new ResponseWrapper(jettyResponse);
        ServerAdapter adapter = new JettyServerAdapter(jettyRequest, jettyResponse) {
            @Override
            public Map<String, String> buildResponseHeaderMap() {
                return buildHeaderMap(jettyResponse.getHeaders());
            }
        };
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
}
