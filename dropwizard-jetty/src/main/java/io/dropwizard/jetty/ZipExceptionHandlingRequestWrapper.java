package io.dropwizard.jetty;

import org.jspecify.annotations.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;

import java.io.EOFException;
import java.util.zip.ZipException;

import static io.dropwizard.util.Throwables.findThrowableInChain;

class ZipExceptionHandlingRequestWrapper extends Request.Wrapper {
    @Nullable
    private Throwable gzipException = null;
    private final Response response;

    public ZipExceptionHandlingRequestWrapper(Request wrapped, Response response) {
        super(wrapped);
        this.response = response;
    }

    @Override
    public void fail(Throwable failure) {
        findThrowableInChain(t -> t.getCause() == null && (t instanceof ZipException || t instanceof EOFException), failure)
            .ifPresent(throwable -> {
                gzipException = throwable;
                // only relevant when not in a servlet environment
                response.setStatus(HttpStatus.BAD_REQUEST_400);
            });

        super.fail(failure);
    }

    @Nullable
    public Throwable getGzipException() {
        return gzipException;
    }
}
