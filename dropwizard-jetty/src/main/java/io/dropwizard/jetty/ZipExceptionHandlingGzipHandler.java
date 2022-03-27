package io.dropwizard.jetty;

import org.eclipse.jetty.http.BadMessageException;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.EOFException;
import java.io.IOException;
import java.util.Optional;
import java.util.zip.ZipException;

import static io.dropwizard.util.Throwables.findThrowableInChain;

/**
 * This customization of Jetty's {@link GzipHandler} catches {@link ZipException}s and {@link EOFException}s to properly return an
 * HTTP status code 400 instead of 500.
 */
class ZipExceptionHandlingGzipHandler extends GzipHandler {
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            super.handle(target, baseRequest, request, response);
        } catch (Exception ex) {
            Optional<BadMessageException> badMessageException = findThrowableInChain(t -> t.getCause() == null && (t instanceof ZipException || t instanceof EOFException), ex)
                .map(e -> new BadMessageException(HttpStatus.BAD_REQUEST_400, e.getMessage(), e));

            if (badMessageException.isPresent()) {
                throw badMessageException.get();
            } else {
                throw ex;
            }
        }
    }
}
