package com.codahale.dropwizard.spdy;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.eclipse.jetty.spdy.server.http.PushStrategy;

@JsonTypeName("none")
public class NonePushStrategyFactory implements PushStrategyFactory {
    @Override
    public PushStrategy build() {
        return new PushStrategy.None();
    }
}
