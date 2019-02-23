package io.dropwizard.request.logging;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.request.logging.old.ClassicLogFormat;
import org.eclipse.jetty.server.CustomRequestLog;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Slf4jRequestLogWriter;

/**
 * A request log factory which writes request logs via Slf4j and doesn't configure any logging infrastructure.
 * Useful when the user doesn't want to configure request logging via the Dropwizard configuration.
 */
@JsonTypeName("external")
public class ExternalRequestLogFactory implements RequestLogFactory<RequestLog> {

    private boolean enabled = true;

    @JsonProperty
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    @JsonProperty
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public RequestLog build(String name) {
        return new CustomRequestLog(new Slf4jRequestLogWriter(), ClassicLogFormat.pattern());
    }
}
