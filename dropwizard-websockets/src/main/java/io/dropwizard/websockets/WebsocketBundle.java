package io.dropwizard.websockets;

import io.dropwizard.metrics.jetty9.websockets.InstWebSocketServerContainerInitializer;
import io.dropwizard.Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import javax.servlet.ServletException;
import javax.websocket.server.ServerEndpointConfig;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.websocket.jsr356.server.ServerContainer;
import static io.dropwizard.websockets.GeneralUtils.rethrow;

public class WebsocketBundle implements Bundle {

    private final Collection<Class<?>> endClassCls;
    private final Collection<ServerEndpointConfig> epC;

    public WebsocketBundle(Class<?>... endpoints) {
        this(Arrays.asList(endpoints), new ArrayList<>());
    }

    public WebsocketBundle(ServerEndpointConfig... configs) {
        this(new ArrayList<>(), Arrays.asList(configs));
    }

    public WebsocketBundle(Collection<Class<?>> endClassCls, Collection<ServerEndpointConfig> epC) {
        this.endClassCls = endClassCls;
        this.epC = epC;
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
                    endClassCls.forEach(rethrow(ep -> wsContainer.addEndpoint(ep)));
                    epC.forEach(rethrow(conf -> wsContainer.addEndpoint(conf)));
                } catch (ServletException ex) {
                    throw new RuntimeException(ex);
                }
            }

        });
    }

}
