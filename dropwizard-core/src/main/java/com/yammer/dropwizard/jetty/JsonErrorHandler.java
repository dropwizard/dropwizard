package com.yammer.dropwizard.jetty;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.yammer.dropwizard.logging.Log;
import org.codehaus.jackson.map.*;
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
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * An {@link ErrorHandler} subclass which returns {@code application/json} error messages.
 */
public class JsonErrorHandler extends ErrorHandler {
    private static final Log LOG = Log.forClass(JsonErrorHandler.class);

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

            response.setContentType(MimeTypes.TEXT_JSON_UTF_8);
            if (getCacheControl() != null) {
                response.setHeader(HttpHeaders.CACHE_CONTROL, getCacheControl());
            }

            Map<String,Object> errorJson = new HashMap<String,Object>();
            errorJson.put("message", errorMessage(request, jettyResponse.getStatus()));
            errorJson.put("status_code", jettyResponse.getStatus());

            ObjectMapper mapper = new ObjectMapper();
            final byte[] bytes = mapper.writeValueAsBytes(errorJson);
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
            final ResourceBundle bundle = ResourceBundle.getBundle("com.yammer.dropwizard.jetty.HttpErrorMessages",
                                                                   request.getLocale());
            final String message = bundle.getString(Integer.toString(status));
            if (message != null) {
                final MessageFormat format = new MessageFormat(message, request.getLocale());
                return format.format(new Object[]{request.getMethod()});
            }
        } catch (MissingResourceException e) {
            LOG.error(e, "Unable to load HttpErrorMessages.properties to find a message for status {}", status);
        }
        return "Your request could not be processed: " + HttpGenerator.getReasonBuffer(status);
    }
}
