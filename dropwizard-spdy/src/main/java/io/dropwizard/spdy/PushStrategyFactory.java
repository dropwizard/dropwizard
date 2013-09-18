package io.dropwizard.spdy;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.dropwizard.jackson.Discoverable;
import org.eclipse.jetty.spdy.server.http.PushStrategy;

/**
 * Builds {@link PushStrategy} instances for SPDY connectors.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface PushStrategyFactory extends Discoverable {
    PushStrategy build();
}
