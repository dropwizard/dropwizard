package io.dropwizard.jetty;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.util.Callback;

import java.io.EOFException;
import java.util.zip.ZipException;

/**
 * This customization of Jetty's {@link GzipHandler} catches {@link ZipException}s and {@link EOFException}s to properly return an
 * HTTP status code 400 instead of 500 for malformed GZIP input.
 */
class ZipExceptionHandlingGzipHandler extends GzipHandler {
    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        ZipExceptionHandlingRequestWrapper requestWrapper = new ZipExceptionHandlingRequestWrapper(request, response);

        boolean handled = super.handle(requestWrapper, response, callback);
        // if already committed, we cannot modify the status code anymore
        if (handled && response.isCommitted()) {
            return true;
        }

        // if the response isn't committed by Jetty, detect if the status code should be changed
        if (handleGzipExceptionIfNecessary(requestWrapper, response, callback)) {
            return true;
        }

        return handled;
    }

    // ensures the ZipException/EofException is thrown while processing GZIP input
    private boolean handleGzipExceptionIfNecessary(ZipExceptionHandlingRequestWrapper request, Response response, Callback callback) {

        if (request.getGzipException() == null) {
            return false;
        }

        Throwable throwable = request.getGzipException();
        Response.writeError(request, response, callback, HttpStatus.BAD_REQUEST_400, throwable.getMessage(), throwable);

        return true;
    }
}
