package com.codahale.dropwizard.spdy;

import com.codahale.dropwizard.jackson.Discoverable;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.eclipse.jetty.spdy.server.http.PushStrategy;

/**
 * Builds {@link PushStrategy} instances for SPDY connectors.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface PushStrategyFactory extends Discoverable {
    PushStrategy build();
}
