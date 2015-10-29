package io.dropwizard.jetty;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Optional;
import org.eclipse.jetty.server.RequestLog;

/**
 * A service provider interface for creating a Jetty {@link org.eclipse.jetty.server.RequestLog}
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = Slf4jRequestLogFactory.class)
public interface RequestLogFactory {
    Optional<? extends RequestLog> build(String name);
}
