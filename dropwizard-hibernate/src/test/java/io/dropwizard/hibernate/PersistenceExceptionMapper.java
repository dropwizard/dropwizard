package io.dropwizard.hibernate;

import org.glassfish.jersey.spi.ExtendedExceptionMapper;
import org.hibernate.exception.DataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.persistence.PersistenceException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

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

        // PersistenceException wraps the real exception, so we look for the real exception mapper for it
        // Cast is necessary since the return type is ExceptionMapper<? extends Throwable> and Java
        // does not allow calling toResponse on the method with a Throwable
        @SuppressWarnings("unchecked")
        final ExceptionMapper<Throwable> exceptionMapper = (ExceptionMapper<Throwable>)
            requireNonNull(providers).getExceptionMapper(t.getClass());

        return exceptionMapper.toResponse(t);
    }

    @Override
    public boolean isMappable(PersistenceException e) {
        return requireNonNull(providers).getExceptionMapper(e.getCause().getClass()) != null;
    }
}
