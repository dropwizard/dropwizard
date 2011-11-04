package com.yammer.dropwizard.jetty;

import org.eclipse.jetty.http.HttpGenerator;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.server.HttpConnection;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.util.ByteArrayISO8859Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

// TODO: 10/12/11 <coda> -- write tests for QuietErrorHandler
// TODO: 10/12/11 <coda> -- write docs for QuietErrorHandler

public class QuietErrorHandler extends ErrorHandler {
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        final HttpConnection connection = HttpConnection.getCurrentConnection();
        final Response jettyResponse = connection.getResponse();
        jettyResponse.setStatus(jettyResponse.getStatus());

        connection.getRequest().setHandled(true);
        final String method = request.getMethod();
        if (!method.equals(HttpMethods.GET) && !method.equals(HttpMethods.POST) && !method.equals(
                HttpMethods.HEAD)) {
            return;
        }
        response.setContentType(MimeTypes.TEXT_HTML_8859_1);
        if (getCacheControl() != null) {
            response.setHeader(HttpHeaders.CACHE_CONTROL, this.getCacheControl());
        }
        final ByteArrayISO8859Writer writer = new ByteArrayISO8859Writer(4096);
        writer.append(errorMessage(request, jettyResponse.getStatus())).append("\n\n");
        writer.flush();
        response.setContentLength(writer.size());
        writer.writeTo(response.getOutputStream());
        writer.destroy();
    }

    private String errorMessage(HttpServletRequest request, int status) {
        switch (status) {
            case SC_BAD_REQUEST:
                return "Your HTTP client sent a request that this server could " +
                        "not understand.";
            case SC_CONFLICT:
                return "The request could not be completed due to a conflict" +
                        " with the return current state of the resource.";
            case SC_EXPECTATION_FAILED:
                return "The server could not meet the expectation given in the " +
                        "Expect return request header.";
            case SC_FORBIDDEN:
                return "You don't have permission to access the requested" +
                        " resource.";
            case SC_GONE:
                return "The requested resource used to exist but no longer" +
                        " does.";
            case SC_INTERNAL_SERVER_ERROR:
                return "The server encountered an internal error and was" +
                        " unable to complete your request.";
            case SC_LENGTH_REQUIRED:
                return "A request with the " + request.getMethod() + " method " +
                        "requires a valid Content-Length header.";
            case SC_METHOD_NOT_ALLOWED:
                return "The " + request.getMethod() + " method is not allowed" +
                        "for the requested resource";
            case SC_NOT_ACCEPTABLE:
                return "The resource identified by the request is only capable" +
                        " of generating response entities which have content" +
                        " characteristics not acceptable according to the" +
                        " accept headers sent in the request.";
            case SC_NOT_FOUND:
                return "The requested resource could not be found on this" +
                        " server.";
            case SC_OK:
                return "";
            case SC_PRECONDITION_FAILED:
                return "The precondition on the request for the resource failed" +
                        " positive evaluation.";
            case SC_REQUEST_ENTITY_TOO_LARGE:
                return "The " + request.getMethod() + " method does not allow" +
                        "the data transmitted, or the data volume exceeds the" +
                        "capacity limit.";
            case SC_REQUEST_TIMEOUT:
                return "The server closed the network connection because your HTTP client" +
                        " didn't finish the request within the specified time.";
            case SC_REQUEST_URI_TOO_LONG:
                return "The length of the requested URL exceeds the capacity limit for this" +
                        " server. The request cannot be processed.";
            case SC_REQUESTED_RANGE_NOT_SATISFIABLE:
                return "The server cannot serve the requested byte range.";
            case SC_SERVICE_UNAVAILABLE:
                return "The server is temporarily unable to service your request due to" +
                        " maintenance downtime or capacity problems. Please try again later.";
            case SC_UNAUTHORIZED:
                return "This server could not verify that you are authorized to access" +
                        " this resource.\n" +
                        "You either supplied the wrong credentials (e.g., bad password)," +
                        " or your HTTP client doesn't understand how to supply the" +
                        " required credentials.";
            case SC_UNSUPPORTED_MEDIA_TYPE:
                return "The server does not support the media type transmitted in the request.";
            default:
                return "Your request could not be processed: " + HttpGenerator.getReasonBuffer(
                        status);
        }
    }
}
