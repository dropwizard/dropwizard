package io.dropwizard.websockets;

import io.dropwizard.Bundle;
import io.dropwizard.metrics.jetty9.websockets.InstWebSocketServerContainerInitializer;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import static io.dropwizard.websockets.GeneralUtils.rethrow;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import javax.servlet.ServletException;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerEndpointConfig;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.websocket.jsr356.server.ServerContainer;
import org.eclipse.jetty.websocket.jsr356.server.ServerEndpointMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebsocketBundle implements Bundle {

    private final Collection<Class<?>> annotatedEndpoints;
    private final Collection<ServerEndpointConfig> extendsEndpoints;
    private static final Logger log = LoggerFactory.getLogger(WebsocketBundle.class);

    public WebsocketBundle(Class<?>... endpoints) {
        this(Arrays.asList(endpoints), new ArrayList<>());
    }

    public WebsocketBundle(ServerEndpointConfig... configs) {
        this(new ArrayList<>(), Arrays.asList(configs));
    }

    public WebsocketBundle(Collection<Class<?>> endClassCls, Collection<ServerEndpointConfig> epC) {
        this.annotatedEndpoints = endClassCls;
        this.extendsEndpoints = epC;
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
    }

    @Override
    public void run(Environment environment) {
        environment.lifecycle().addLifeCycleListener(new AbstractLifeCycle.AbstractLifeCycleListener() {

            @Override
            public void lifeCycleStarting(LifeCycle event) {
                try {
                    ServerContainer wsContainer = InstWebSocketServerContainerInitializer.
                            configureContext(environment.getApplicationContext(), environment.metrics());

                    StringBuilder sb = new StringBuilder("Registering websocket endpoints: ")
                            .append(System.lineSeparator())
                            .append(System.lineSeparator());

                    annotatedEndpoints.forEach(rethrow(ep -> addEndpoint(wsContainer, ep, null, sb)));
                    extendsEndpoints.forEach(rethrow(conf -> addEndpoint(wsContainer, conf.getEndpointClass(), conf, sb)));
                    log.info(sb.toString());
                } catch (ServletException ex) {
                    throw new RuntimeException(ex);
                }
            }

            private void addEndpoint(ServerContainer wsContainer, final Class<?> endpointClass, ServerEndpointConfig conf, StringBuilder sb) throws DeploymentException {
                ServerEndpointMetadata md = wsContainer.getServerEndpointMetadata(endpointClass, conf);
                wsContainer.addEndpoint(md);
                sb.append(String.format("    WS      %s (%s)", md.getPath(), endpointClass.getName())).append(System.lineSeparator());
            }
        });
    }

}
