package com.yammer.dropwizard.jersey.caching;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.spi.container.ResourceMethodDispatchAdapter;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;
import com.sun.jersey.spi.dispatch.RequestDispatcher;

import javax.ws.rs.core.HttpHeaders;
import java.util.concurrent.TimeUnit;

public class CacheControlledResourceMethodDispatchAdapter implements ResourceMethodDispatchAdapter {
    private static class CacheControlledResourceMethodDispatchProvider implements ResourceMethodDispatchProvider {
        private static final int ONE_YEAR_IN_SECONDS = (int) TimeUnit.DAYS.toSeconds(365);

        private final ResourceMethodDispatchProvider provider;

        private CacheControlledResourceMethodDispatchProvider(ResourceMethodDispatchProvider provider) {
            this.provider = provider;
        }

        @Override
        public RequestDispatcher create(AbstractResourceMethod abstractResourceMethod) {
            final RequestDispatcher dispatcher = provider.create(abstractResourceMethod);
            final CacheControl control = abstractResourceMethod.getAnnotation(CacheControl.class);
            if (control != null) {
                final javax.ws.rs.core.CacheControl cacheControl = new javax.ws.rs.core.CacheControl();
                cacheControl.setPrivate(control.isPrivate());
                cacheControl.setNoCache(control.noCache());
                cacheControl.setNoStore(control.noStore());
                cacheControl.setNoTransform(control.noTransform());
                cacheControl.setMustRevalidate(control.mustRevalidate());
                cacheControl.setProxyRevalidate(control.proxyRevalidate());
                cacheControl.setMaxAge((int) control.maxAgeUnit().toSeconds(control.maxAge()));
                cacheControl.setSMaxAge((int) control.sharedMaxAgeUnit()
                                                     .toSeconds(control.sharedMaxAge()));
                if (control.immutable()) {
                    cacheControl.setMaxAge(ONE_YEAR_IN_SECONDS);
                }
                return new CacheControlledRequestDispatcher(dispatcher, cacheControl);
            }
            return dispatcher;
        }
    }

    private static class CacheControlledRequestDispatcher implements RequestDispatcher {
        private final RequestDispatcher dispatcher;
        private final String cacheControl;

        private CacheControlledRequestDispatcher(RequestDispatcher dispatcher, javax.ws.rs.core.CacheControl cacheControl) {
            this.dispatcher = dispatcher;
            this.cacheControl = cacheControl.toString();
        }

        @Override
        public void dispatch(Object resource, HttpContext context) {
            dispatcher.dispatch(resource, context);
            if (!cacheControl.isEmpty()) {
                context.getResponse().getHttpHeaders().add(HttpHeaders.CACHE_CONTROL, cacheControl);
            }
        }
    }

    @Override
    public ResourceMethodDispatchProvider adapt(ResourceMethodDispatchProvider provider) {
        return new CacheControlledResourceMethodDispatchProvider(provider);
    }
}
