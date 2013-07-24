package com.codahale.dropwizard.lifecycle.setup;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.dropwizard.lifecycle.Managed;
import com.codahale.dropwizard.lifecycle.ServerLifecycleListener;
import com.codahale.dropwizard.lifecycle.ServiceManaged;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.Service.State;

public class LifecycleEnvironment {
    private static final Logger LOGGER = LoggerFactory.getLogger(LifecycleEnvironment.class);

    private final List<Service.Listener> managedObjects;
    private final List<Service.Listener> lifecycleListeners;
    private final List<ServerLifecycleListener> serverListeners;

    public LifecycleEnvironment() {
        this.lifecycleListeners = Lists.newArrayList();
        this.managedObjects = Lists.newArrayList();
        this.serverListeners = Lists.newArrayList();
    }

    /**
     * Adds the given {@link Managed} instance to the set of objects managed by the server's
     * lifecycle. When the server starts, {@code managed} will be started. When the server stops,
     * {@code managed} will be stopped.
     *
     * @param managed a managed object
     */
    public void manage(Managed managed) {
        manage(new ServiceManaged(checkNotNull(managed)));
    }

    /**
     * Adds the given Jetty {@link LifeCycle} instances to the server's lifecycle.
     *
     * @param managed a Jetty-managed object
     */
    public void manage(Service.Listener managed) {
        managedObjects.add(checkNotNull(managed));
    }

    public ExecutorServiceBuilder executorService(String nameFormat) {
        return new ExecutorServiceBuilder(this, nameFormat);
    }

    public ScheduledExecutorServiceBuilder scheduledExecutorService(String nameFormat) {
        return new ScheduledExecutorServiceBuilder(this, nameFormat);
    }

    public void addServerLifecycleListener(ServerLifecycleListener listener) {
        serverListeners.add(listener);
    }

    public void attach(final Service service) {
        final ListeningExecutorService executor = MoreExecutors.sameThreadExecutor();
        
        for (Service.Listener listener : Iterables.concat(managedObjects,lifecycleListeners)) {
            service.addListener(listener,executor);
        }
        
        for (final ServerLifecycleListener serverListener : serverListeners) {
            service.addListener(new Service.Listener() {
                
                public void terminated(State from)
                {
                }
                
                public void stopping(State from)
                {
                }
                
                public void starting()
                {
                }
                
                public void running()
                {
                    serverListener.serverStarted(service);
                }
                
                public void failed(State from, Throwable failure)
                {
                }
            }, executor);
        }
        
        service.addListener(new Service.Listener() {
            
            public void terminated(State from)
            {
            }
            
            public void stopping(State from)
            {
            }
            
            @Override
            public void starting() {
                LOGGER.debug("managed objects = {}", lifecycleListeners);
            }
            
            public void running()
            {
            }
            
            public void failed(State from, Throwable failure)
            {
            }
        },executor);
    }
}
