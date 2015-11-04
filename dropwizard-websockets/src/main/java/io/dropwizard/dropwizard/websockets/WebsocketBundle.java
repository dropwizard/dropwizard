package io.dropwizard.dropwizard.websockets;

import io.dropwizard.Bundle;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import javax.servlet.ServletException;
import javax.websocket.server.ServerEndpointConfig;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.websocket.common.events.EventDriverFactory;
import org.eclipse.jetty.websocket.common.events.EventDriverImpl;
import org.eclipse.jetty.websocket.jsr356.server.ServerContainer;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeFilter;
import static io.dropwizard.dropwizard.websockets.GeneralUtils.rethrow;

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
                    ServerContainer wsContainer = configureWsContext(environment.getApplicationContext(), new InstJsrServerEndpointImpl(environment.metrics()));
                    endClassCls.forEach(rethrow(ep -> wsContainer.addEndpoint(ep)));
                    epC.forEach(rethrow(conf -> wsContainer.addEndpoint(conf)));
                } catch (ServletException ex) {
                    throw new RuntimeException(ex);
                }
            }

            private ServerContainer configureWsContext(final MutableServletContextHandler context, final EventDriverImpl edImpl) throws ServletException {
                WebSocketUpgradeFilter filter = WebSocketUpgradeFilter.configureContext(context);
                ServerContainer wsContainer = new ServerContainer(filter, filter.getFactory(), context.getServer().getThreadPool());
                EventDriverFactory edf = filter.getFactory().getEventDriverFactory();
                edf.clearImplementations();
                edf.addImplementation(edImpl);
                context.addBean(wsContainer);
                context.setAttribute(javax.websocket.server.ServerContainer.class.getName(), wsContainer);
                return wsContainer;
            }

        });
    }

}
