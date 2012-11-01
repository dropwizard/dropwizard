package com.yammer.dropwizard.jersey;

import com.google.common.base.Optional;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.spi.container.ResourceMethodDispatchAdapter;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;
import com.sun.jersey.spi.dispatch.RequestDispatcher;

import javax.ws.rs.ext.Provider;

@Provider
public class OptionalResourceMethodDispatchAdapter implements ResourceMethodDispatchAdapter {
    private static class OptionalResourceMethodDispatchProvider implements ResourceMethodDispatchProvider {
        private final ResourceMethodDispatchProvider provider;

        private OptionalResourceMethodDispatchProvider(ResourceMethodDispatchProvider provider) {
            this.provider = provider;
        }

        @Override
        public RequestDispatcher create(AbstractResourceMethod abstractResourceMethod) {
            return new OptionalRequestDispatcher(provider.create(abstractResourceMethod));
        }
    }

    private static class OptionalRequestDispatcher implements RequestDispatcher {
        private final RequestDispatcher dispatcher;

        private OptionalRequestDispatcher(RequestDispatcher dispatcher) {
            this.dispatcher = dispatcher;
        }

        @Override
        public void dispatch(Object resource, HttpContext context) {
            dispatcher.dispatch(resource, context);
            final Object entity = context.getResponse().getEntity();
            if (entity instanceof Optional) {
                final Optional<?> optional = (Optional<?>) entity;
                if (optional.isPresent()) {
                    context.getResponse().setEntity(optional.get());
                } else {
                    throw new NotFoundException();
                }
            }
        }
    }

    @Override
    public ResourceMethodDispatchProvider adapt(ResourceMethodDispatchProvider provider) {
        return new OptionalResourceMethodDispatchProvider(provider);
    }
}
