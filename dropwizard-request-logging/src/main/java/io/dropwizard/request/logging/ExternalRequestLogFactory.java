package io.dropwizard.request.logging;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Slf4jRequestLog;

/**
 * A request log factory which writes request logs via Slf4j and doesn't configure any logging infrastructure.
 * Useful when the user doesn't want to configure request logging via the Dropwizard configuration.
 */
@JsonTypeName("external")
public class ExternalRequestLogFactory implements RequestLogFactory {

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
        return new Slf4jRequestLog();
    }
}
