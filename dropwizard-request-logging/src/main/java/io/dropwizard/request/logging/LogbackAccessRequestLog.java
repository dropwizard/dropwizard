package io.dropwizard.request.logging;

import ch.qos.logback.access.common.spi.IAccessEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import ch.qos.logback.core.spi.BasicSequenceNumberGenerator;
import ch.qos.logback.core.spi.SequenceNumberGenerator;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.component.AbstractLifeCycle;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class LogbackAccessRequestLog extends AbstractLifeCycle implements RequestLog, AppenderAttachable<IAccessEvent> {

    private final AppenderAttachableImpl<IAccessEvent> appenderAttachable = new AppenderAttachableImpl<>();
    private final SequenceNumberGenerator sequenceNumberGenerator = new BasicSequenceNumberGenerator();

    @Override
    public void log(Request request, Response response) {
        IAccessEvent accessEvent = new JettyAccessEvent(request, response, sequenceNumberGenerator);
        appenderAttachable.appendLoopOnAppenders(accessEvent);
    }

    @Override
    public void addAppender(Appender<IAccessEvent> newAppender) {
        appenderAttachable.addAppender(newAppender);
    }

    @Override
    public Iterator<Appender<IAccessEvent>> iteratorForAppenders() {
        return appenderAttachable.iteratorForAppenders();
    }

    @Override
    public Appender<IAccessEvent> getAppender(String name) {
        return appenderAttachable.getAppender(name);
    }

    @Override
    public boolean isAttached(Appender<IAccessEvent> appender) {
        return appenderAttachable.isAttached(appender);
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
    public void detachAndStopAllAppenders() {
        appenderAttachable.detachAndStopAllAppenders();
    }

    @Override
    public boolean detachAppender(Appender<IAccessEvent> appender) {
        return appenderAttachable.detachAppender(appender);
    }

    @Override
    public boolean detachAppender(String name) {
        return appenderAttachable.detachAppender(name);
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        detachAndStopAllAppenders();
    }
}
