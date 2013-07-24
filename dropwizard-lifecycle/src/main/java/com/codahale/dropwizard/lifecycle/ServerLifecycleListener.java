package com.codahale.dropwizard.lifecycle;

import java.util.EventListener;

import com.google.common.util.concurrent.Service;

public interface ServerLifecycleListener extends EventListener {
    void serverStarted(Service server);
}
