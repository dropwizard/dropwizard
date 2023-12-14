package io.dropwizard.jetty;

import org.eclipse.jetty.http.BadMessageException;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.util.Callback;

import java.io.EOFException;
import java.util.Optional;
import java.util.zip.ZipException;

import static io.dropwizard.util.Throwables.findThrowableInChain;

/**
 * This customization of Jetty's {@link GzipHandler} catches {@link ZipException}s and {@link EOFException}s to properly return an
 * HTTP status code 400 instead of 500.
 */
class ZipExceptionHandlingGzipHandler extends GzipHandler {
    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        return super.handle(new ZipExceptionHandlingRequestWrapper(request), response, callback);
    }

    public static class ZipExceptionHandlingRequestWrapper extends Request.Wrapper {
        public ZipExceptionHandlingRequestWrapper(Request wrapped) {
            super(wrapped);
        }

        @Override
        public void fail(Throwable failure) {
            Optional<BadMessageException> badMessageException = findThrowableInChain(t -> t.getCause() == null && (t instanceof ZipException || t instanceof EOFException), failure)
                .map(e -> new BadMessageException(HttpStatus.BAD_REQUEST_400, e.getMessage(), e));

            if (badMessageException.isPresent()) {
                failure = badMessageException.get();
            }

            super.fail(failure);
        }
    }
}
