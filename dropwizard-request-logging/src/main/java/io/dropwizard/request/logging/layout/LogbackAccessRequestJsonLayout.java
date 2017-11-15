package io.dropwizard.request.logging.layout;

import ch.qos.logback.access.PatternLayout;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.contrib.json.JsonLayoutBase;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.util.CachingDateFormatter;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * A base layout for Logback Access request logs.
 * <ul>
 *     <li>Extends {@link PatternLayout}.</li>
 *     <li>Disables pattern headers.</li>
 *     <li>Sets the pattern to the given timezone.</li>
 * </ul>
 */
public class LogbackAccessRequestJsonLayout extends JsonLayoutBase<IAccessEvent> {
    private final CachingDateFormatter cachingDateFormatter;

    public LogbackAccessRequestJsonLayout(Context context, TimeZone timeZone, String timestampFormat) {
        setContext(context);

        cachingDateFormatter = new CachingDateFormatter(timestampFormat !=null && timestampFormat.trim().length() > 0
                                                                ? timestampFormat : CoreConstants.CLF_DATE_PATTERN);
        cachingDateFormatter.setTimeZone(timeZone);
    }

    @Override
    protected Map toJsonMap(IAccessEvent event) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("timestamp", event.getTimeStamp());
        map.put("formattedTimestamp", cachingDateFormatter.format(event.getTimeStamp()));
        map.put("method", event.getMethod());
        map.put("url", event.getRequest().getRequestURL().append(event.getQueryString()).toString());
        map.put("protocol", event.getProtocol());
        map.put("status", event.getResponse().getStatus());
        return map;
    }
}
