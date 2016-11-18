package io.dropwizard.request.logging.layout;

import ch.qos.logback.access.pattern.AccessConverter;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.util.OptionHelper;

import java.util.Arrays;

/**
 * A safe version of {@link ch.qos.logback.access.pattern.RequestParameterConverter} which works
 * with async appenders. It loads request parameters from a cached map rather than trying to load
 * request data from the original request which may be closed.
 */
public class SafeRequestParameterConverter extends AccessConverter {

    private String key;

    @Override
    public void start() {
        key = getFirstOption();
        if (OptionHelper.isEmpty(key)) {
            addWarn("Missing key for the request parameter");
        } else {
            super.start();
        }
    }

    @Override
    public String convert(IAccessEvent accessEvent) {
        if (!isStarted()) {
            return "INACTIVE_REQUEST_PARAM_CONV";
        }

        // This call should be safe, because the request map is cached beforehand
        final String[] paramArray = accessEvent.getRequestParameterMap().get(key);
        if (paramArray == null || paramArray.length == 0) {
            return "-";
        } else if (paramArray.length == 1) {
            return paramArray[0];
        } else {
            return Arrays.toString(paramArray);
        }
    }
}
