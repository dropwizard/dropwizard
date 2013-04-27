package com.yammer.dropwizard.views.flashscope;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;


public class FlashScopeInjectableProvider implements InjectableProvider<FlashScope, Parameter> {

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public Injectable getInjectable(ComponentContext ic, FlashScope flashScope, final Parameter parameter) {
        return new AbstractHttpContextInjectable<Flash>() {
            @Override
            public Flash getValue(HttpContext context) {
                if (parameter.getParameterClass().equals(FlashOut.class)) {
                    FlashOut flashOut = new FlashOut();
                    context.getProperties().put(FlashOut.class.getName(), flashOut);
                    return flashOut;
                }

                FlashIn flashIn = (FlashIn) context.getProperties().get(FlashIn.class.getName());
                if (flashIn != null) {
                    return flashIn;
                }

                return new FlashIn();
            }
        };
    }
}
