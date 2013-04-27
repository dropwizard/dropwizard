package com.yammer.dropwizard.views.flashscope;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;
import com.sun.jersey.spi.dispatch.RequestDispatcher;

import javax.ws.rs.core.Cookie;
import java.util.Map;

import static javax.ws.rs.core.HttpHeaders.SET_COOKIE;

public class FlashScopeResourceMethodDispatchProvider implements ResourceMethodDispatchProvider {

    private final ResourceMethodDispatchProvider provider;
    private final FlashScopeConfig config;

    public FlashScopeResourceMethodDispatchProvider(FlashScopeConfig config, ResourceMethodDispatchProvider provider) {
        this.config = config;
        this.provider = provider;
    }

    @Override
    public RequestDispatcher create(AbstractResourceMethod method) {
        final RequestDispatcher dispatcher = provider.create(method);
        if (dispatcher == null) {
            return null;
        }

        return new RequestDispatcher() {
            public void dispatch(Object resource, HttpContext context) {
                Map<String, Cookie> requestCookies = context.getRequest().getCookies();
                Map<String, Object> contextProps = context.getProperties();

                createFlashInIfCookiePresent(requestCookies, contextProps);
                dispatcher.dispatch(resource, context);
                writeFlashOutToResponse(context, contextProps);
            }

            private void writeFlashOutToResponse(HttpContext context, Map<String, Object> contextProps) {
                FlashOut flashOut = (FlashOut) contextProps.get(FlashOut.class.getName());
                if (flashOut != null && !flashOut.isEmpty()) {
                    context.getResponse().getHttpHeaders().add(SET_COOKIE, flashOut.build(config));
                } else {
                    if (contextProps.containsKey(FlashIn.class.getName())) {
                        //Because some IE versions aren't too strict about expiring cookies on time
                        context.getResponse().getHttpHeaders().add(SET_COOKIE, FlashOut.expireImmediately(config));
                    }
                }
            }

            private void createFlashInIfCookiePresent(Map<String, Cookie> requestCookies, Map<String, Object> contextProps) {
                if (requestCookies.containsKey(config.getCookieName())) {
                    Cookie cookie = requestCookies.get(config.getCookieName());
                    FlashIn flashIn = FlashIn.restoreFrom(cookie);
                    contextProps.put(FlashIn.class.getName(), flashIn);
                }
            }
        };
    }
}
