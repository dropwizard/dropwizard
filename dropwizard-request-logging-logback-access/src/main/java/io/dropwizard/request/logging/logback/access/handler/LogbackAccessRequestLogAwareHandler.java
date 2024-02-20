package io.dropwizard.request.logging.logback.access.handler;

import org.eclipse.jetty.ee10.servlet.ServletContextRequest;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.internal.HttpChannelState;
import org.eclipse.jetty.util.Callback;

public class LogbackAccessRequestLogAwareHandler extends Handler.Wrapper {

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        boolean handled = super.handle(request, response, callback);
        if (handled) {
            ServletContextRequest servletContextRequest = Request.as(request, ServletContextRequest.class);
            if (servletContextRequest != null) {
                Request unwrapped = Request.unWrap(request);
                if (!(unwrapped instanceof HttpChannelState.ChannelRequest channelRequest)) {
                    throw new IllegalStateException("Expecting unwrapped request to be an instance of HttpChannelState.ChannelRequest");
                }
                channelRequest.setLoggedRequest(servletContextRequest);
            }
        }
        return handled;
    }
}
