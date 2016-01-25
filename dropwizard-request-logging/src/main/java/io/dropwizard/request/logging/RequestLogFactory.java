package io.dropwizard.request.logging;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.eclipse.jetty.server.RequestLog;

import io.dropwizard.jackson.Discoverable;

/**
 * A service provider interface for creating a Jetty {@link RequestLog}
 *
 * @param <T> type of a {@link RequestLog} implementation
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = LogbackAccessRequestLogFactory.class)
public interface RequestLogFactory<T extends RequestLog> extends Discoverable {

    boolean isEnabled();

    T build(String name);
}
