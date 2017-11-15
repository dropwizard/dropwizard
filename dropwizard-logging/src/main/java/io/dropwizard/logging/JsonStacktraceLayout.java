package io.dropwizard.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.contrib.json.classic.JsonLayout;

import java.util.Map;

/**
 * Created by pei971 on 2/6/16.
 */
public class JsonStacktraceLayout extends JsonLayout {

    public static final String STACKTRACE_ATTR_NAME = "stacktrace";

    protected boolean includeStacktrace;

    public JsonStacktraceLayout() {
        super();
        this.includeStacktrace = true;
    }


    @Override
    protected Map toJsonMap(ILoggingEvent event) {

        Map<String, Object> map = super.toJsonMap(event);

        if (this.includeStacktrace) {
            IThrowableProxy throwableProxy = event.getThrowableProxy();
            if (throwableProxy != null && throwableProxy.getStackTraceElementProxyArray() != null) {
                StringBuilder ex = new StringBuilder();
                for (StackTraceElementProxy ste : throwableProxy.getStackTraceElementProxyArray())
                {
                    ex.append(ste.getSTEAsString() + System.lineSeparator());
                }
                if (ex != null && ex.length() > 0) {
                    map.put(STACKTRACE_ATTR_NAME, ex);
                }
            }

        }

        return map;

    }

    public boolean isIncludeStacktrace() {
        return includeStacktrace;
    }

    public void setIncludeStacktrace(boolean includeStacktrace) {
        this.includeStacktrace = includeStacktrace;
    }
}
