package com.codahale.dropwizard.sessions;

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
public class FlashProvider implements InjectableProvider<Session, Parameter> {
    private final ThreadLocal<HttpServletRequest> request;

    public FlashProvider(@Context ThreadLocal<HttpServletRequest> request) {
        this.request = request;
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public Injectable<?> getInjectable(ComponentContext ic, final Session annotation, Parameter parameter) {
        if (parameter.getParameterClass().isAssignableFrom(Flash.class)) {
            return new Injectable<Flash<?>>() {
                @Override
                public Flash<?> getValue() {
                    final HttpServletRequest req = request.get();
                    if (req != null) {
                        final HttpSession session = req.getSession(!annotation.doNotCreate());
                        if (session != null) {
                            return new Flash<>(session);
                        }
                        return null;
                    }
                    return null;
                }
            };
        }
        return null;
    }
}
