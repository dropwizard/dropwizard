package com.codahale.dropwizard.sessions;

import com.codahale.dropwizard.sessions.Session;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

@Provider
public class HttpSessionProvider implements InjectableProvider<Session, Parameter> {
    private final ThreadLocal<HttpServletRequest> request;

    public HttpSessionProvider(@Context ThreadLocal<HttpServletRequest> request) {
        this.request = request;
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public Injectable<?> getInjectable(ComponentContext ic, final Session session, Parameter parameter) {
        if (parameter.getParameterClass().isAssignableFrom(HttpSession.class)) {
            return new Injectable<HttpSession>() {
                @Override
                public HttpSession getValue() {
                    final HttpServletRequest req = request.get();
                    if (req != null) {
                        return req.getSession(!session.doNotCreate());
                    }
                    return null;
                }
            };
        }
        return null;
    }
}
