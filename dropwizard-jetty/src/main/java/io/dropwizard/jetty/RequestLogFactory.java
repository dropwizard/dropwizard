package io.dropwizard.jetty;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.eclipse.jetty.server.RequestLog;

/**
 * A service provider interface for creating a Jetty {@link RequestLog}
 *
 * @param <T> type of a {@link RequestLog} implementation
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = Slf4jRequestLogFactory.class)
public interface RequestLogFactory<T extends RequestLog> {

    boolean isEnabled();

    T build(String name);
}
