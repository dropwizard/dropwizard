package com.yammer.dropwizard.jetty;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.yammer.dropwizard.logging.Log;
import org.eclipse.jetty.http.HttpGenerator;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.AbstractHttpConnection;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.ErrorHandler;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * An {@link ErrorHandler} subclass which returns concise, {@code text/plain} error messages.
 */
public class QuietErrorHandler extends ErrorHandler {
    private static final Log LOG = Log.forClass(QuietErrorHandler.class);
    private static final int RESPONSE_BUFFER_SIZE = 4096;

    /*
     * Sadly, this class is basically untestable.
     */
    private static final ImmutableSet<String> ALLOWED_METHODS = ImmutableSet.of(
            HttpMethods.GET, HttpMethods.HEAD, HttpMethods.POST, HttpMethods.PUT, HttpMethods.DELETE
    );

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException {
        final AbstractHttpConnection connection = AbstractHttpConnection.getCurrentConnection();
        if (connection != null) {
            connection.getRequest().setHandled(true);
            final Response jettyResponse = connection.getResponse();
            jettyResponse.setStatus(jettyResponse.getStatus());

            connection.getRequest().setHandled(true);
            final String method = request.getMethod();

            if (!ALLOWED_METHODS.contains(method)) {
                return;
            }

            response.setContentType(MimeTypes.TEXT_PLAIN_UTF_8);
            if (getCacheControl() != null) {
                response.setHeader(HttpHeaders.CACHE_CONTROL, getCacheControl());
            }

            final StringBuilder builder = new StringBuilder(RESPONSE_BUFFER_SIZE);
            builder.append(errorMessage(request, jettyResponse.getStatus()))
                   .append('\n')
                   .append('\n');
            final byte[] bytes = builder.toString().getBytes(Charsets.UTF_8);
            response.setContentLength(bytes.length);
            final ServletOutputStream output = response.getOutputStream();
            try {
                output.write(bytes);
            } finally {
                output.close();
            }
        }
    }

    private static String errorMessage(HttpServletRequest request, int status) {
        try {
            final ResourceBundle bundle = ResourceBundle.getBundle("HttpErrorMessages",
                                                                   request.getLocale());
            final String message = bundle.getString(Integer.toString(status));
            if (message != null) {
                final MessageFormat format = new MessageFormat(message, request.getLocale());
                return format.format(new Object[]{request.getMethod()});
            }
        } catch (MissingResourceException e) {
            LOG.error(e, "Unable to load HttpErrorMessages.properties");
        }
        return "Your request could not be processed: " + HttpGenerator.getReasonBuffer(status);
    }
}
