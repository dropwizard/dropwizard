package io.dropwizard.request.logging.layout;

import ch.qos.logback.access.common.PatternLayout;
import ch.qos.logback.core.Context;

import java.util.TimeZone;

/**
 * A base layout for Logback Access request logs.
 * <ul>
 *     <li>Extends {@link PatternLayout}.</li>
 *     <li>Disables pattern headers.</li>
 *     <li>Sets the pattern to the given timezone.</li>
 * </ul>
 */
public class LogbackAccessRequestLayout extends PatternLayout {

    static {
        // Replace upstream's RequestParameterConverter (which reads live request state and is unsafe under async
        // appenders) with our SafeRequestParameterConverter (which reads from the frozen parameter map that
        // AccessEvent.prepareForDeferredProcessing captured).
        //
        // We must write to ACCESS_DEFAULT_CONVERTER_SUPPLIER_MAP (the access-layout supplier map) rather than the
        // (deprecated) logback-classic DEFAULT_CONVERTER_MAP; the classic map is not consulted by access's
        // PatternLayout at all anymore.
        PatternLayout.ACCESS_DEFAULT_CONVERTER_SUPPLIER_MAP.put("requestParameter",
            SafeRequestParameterConverter::new);
        PatternLayout.ACCESS_DEFAULT_CONVERTER_SUPPLIER_MAP.put("reqParameter", SafeRequestParameterConverter::new);
    }

    public LogbackAccessRequestLayout(Context context, TimeZone timeZone) {
        setOutputPatternAsHeader(false);
        setPattern("%h %l %u [%t{dd/MMM/yyyy:HH:mm:ss Z," + timeZone.getID()
                + "}] \"%r\" %s %b \"%i{Referer}\" \"%i{User-Agent}\" %D");
        setContext(context);
    }
}
