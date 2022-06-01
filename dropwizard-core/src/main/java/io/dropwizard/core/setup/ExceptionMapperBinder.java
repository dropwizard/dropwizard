package io.dropwizard.core.setup;

import io.dropwizard.jersey.errors.EarlyEofExceptionMapper;
import io.dropwizard.jersey.errors.EofExceptionWriterInterceptor;
import io.dropwizard.jersey.errors.IllegalStateExceptionMapper;
import io.dropwizard.jersey.errors.LoggingExceptionMapper;
import io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper;
import io.dropwizard.jersey.optional.EmptyOptionalExceptionMapper;
import io.dropwizard.jersey.validation.JerseyViolationExceptionMapper;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.WriterInterceptor;
import org.glassfish.jersey.internal.inject.AbstractBinder;

/**
 * A binder that registers all the default exception mappers while allowing users to override
 * individual exception mappers without disabling all others.
 */
public class ExceptionMapperBinder extends AbstractBinder {
    private final boolean showDetails;

    public ExceptionMapperBinder(boolean showDetails) {
        this.showDetails = showDetails;
    }

    @Override
    protected void configure() {
        bind(new LoggingExceptionMapper<Throwable>() {}).to(ExceptionMapper.class);
        bind(JerseyViolationExceptionMapper.class).to(ExceptionMapper.class);
        bind(new JsonProcessingExceptionMapper(isShowDetails())).to(ExceptionMapper.class);
        bind(EarlyEofExceptionMapper.class).to(ExceptionMapper.class);
        bind(EofExceptionWriterInterceptor.class).to(WriterInterceptor.class);
        bind(EmptyOptionalExceptionMapper.class).to(ExceptionMapper.class);
        bind(IllegalStateExceptionMapper.class).to(ExceptionMapper.class);
    }

    public boolean isShowDetails() {
        return showDetails;
    }
}
