package com.yammer.dropwizard.jetty;

import org.eclipse.jetty.server.handler.ErrorHandler;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Writer;

/**
 * An {@link ErrorHandler} subclass which doesn't identify itself as a Jetty server.
 */
public class UnbrandedErrorHandler extends ErrorHandler {
    public UnbrandedErrorHandler() {
        super();
        setShowStacks(false);
    }

    @Override
    public void writeErrorPage(HttpServletRequest request, Writer writer, int code, String message, boolean showStacks) throws IOException {
        super.writeErrorPage(request, writer, code, message, showStacks);
    }

    @Override
    protected void writeErrorPageBody(HttpServletRequest request, Writer writer, int code, String message, boolean showStacks) throws IOException {
        final String uri = request.getRequestURI();
        writeErrorPageMessage(request, writer, code, message, uri);
        if (showStacks) {
            writeErrorPageStacks(request, writer);
        }
        for (int i= 0; i < 20; i++) {
            writer.write("<br/>                                                \n");
        }
    }
}
