package io.dropwizard.jetty;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.eclipse.jetty.ee10.servlet.ServletApiRequest;
import org.eclipse.jetty.http.BadMessageException;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;

import java.io.EOFException;
import java.io.IOException;
import java.util.Optional;
import java.util.zip.ZipException;

import static io.dropwizard.util.Throwables.findThrowableInChain;

/**
 * This {@link Filter} implementation catches {@link ZipException}s and {@link EOFException}s to properly return an HTTP
 * status code 400 instead of 500 for malformed GZIP input.
 * For this {@link Filter} to work, a {@link ZipExceptionHandlingGzipHandler} must be present in the handler chain.
 */
public class ZipExceptionHandlingServletFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            Optional<Throwable> throwable = findThrowableInChain(t -> t.getCause() == null && (t instanceof ZipException || t instanceof EOFException), e);
            if (throwable.isPresent() && request instanceof ServletApiRequest servletApiRequest) {
                handleGzipExceptionIfNecessary(servletApiRequest);
            }

            throw e;
        }
    }

    private void handleGzipExceptionIfNecessary(ServletApiRequest servletApiRequest) {

        Request currentRequest = servletApiRequest.getRequest();
        ZipExceptionHandlingRequestWrapper unwrapped = Request.as(currentRequest, ZipExceptionHandlingRequestWrapper.class);
        if (unwrapped == null || unwrapped.getGzipException() == null) {
            return;
        }
        // check, if the exception was generated while reading the HTTP input
        Throwable gzipException = unwrapped.getGzipException();
        throw new BadMessageException(HttpStatus.BAD_REQUEST_400, gzipException.getMessage(), gzipException);
    }
}
