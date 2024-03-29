package io.dropwizard.hibernate;

import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.Providers;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.glassfish.jersey.spi.ExtendedExceptionMapper;
import org.hibernate.exception.DataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

@Provider
public class PersistenceExceptionMapper implements ExtendedExceptionMapper<PersistenceException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataException.class);

    @Context
    @Nullable
    private Providers providers;

    @Override
    public Response toResponse(PersistenceException e) {
        LOGGER.error("Hibernate error", e);

        Throwable t = e.getCause();
        Class<? extends Throwable> exceptionClass = t == null ? Exception.class : t.getClass();

        // PersistenceException wraps the real exception, so we look for the real exception mapper for it
        // Cast is necessary since the return type is ExceptionMapper<? extends Throwable> and Java
        // does not allow calling toResponse on the method with a Throwable
        @SuppressWarnings("unchecked")
        final ExceptionMapper<Throwable> exceptionMapper = (ExceptionMapper<Throwable>)
            requireNonNull(providers).getExceptionMapper(exceptionClass);

        return exceptionMapper.toResponse(t);
    }

    @Override
    public boolean isMappable(PersistenceException e) {
        Throwable cause = e.getCause();
        return cause != null && requireNonNull(providers).getExceptionMapper(cause.getClass()) != null;
    }
}
