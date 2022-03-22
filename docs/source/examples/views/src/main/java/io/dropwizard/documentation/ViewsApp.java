package io.dropwizard.documentation;

import com.github.mustachejava.MustacheNotFoundException;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.jersey.errors.ErrorEntityWriter;
import io.dropwizard.jersey.errors.ErrorMessage;
import io.dropwizard.jersey.validation.ValidationErrorMessage;
import io.dropwizard.views.common.View;
import io.dropwizard.views.common.ViewBundle;
import io.dropwizard.views.common.ViewRenderException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.glassfish.jersey.spi.ExtendedExceptionMapper;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

public class ViewsApp extends Application<ViewsConfiguration> {
    @Override
    public void initialize(Bootstrap<ViewsConfiguration> bootstrap) {
        // Default configuration
        bootstrap.addBundle(new ViewBundle<>());

        // Custom configuration
        bootstrap.addBundle(new ViewBundle<ViewsConfiguration>() {
            @Override
            public Map<String, Map<String, String>> getViewConfiguration(ViewsConfiguration config) {
                return config.getViewRendererConfiguration();
            }
        });
    }

    @Override
    public void run(ViewsConfiguration configuration, Environment environment) throws Exception {
        environment.jersey().register(new ExtendedExceptionMapper<WebApplicationException>() {
            @Override
            public Response toResponse(WebApplicationException exception) {
                // Return a response here, for example HTTP 500 (Internal Server Error)
                return Response.serverError().build();
            }

            @Override
            public boolean isMappable(WebApplicationException e) {
                return ExceptionUtils.indexOfThrowable(e, ViewRenderException.class) != -1;
            }
        });

        environment.jersey().register(new ExtendedExceptionMapper<WebApplicationException>() {
            @Override
            public Response toResponse(WebApplicationException exception) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            @Override
            public boolean isMappable(WebApplicationException e) {
                return ExceptionUtils.getRootCause(e).getClass() == MustacheNotFoundException.class;
            }
        });

        environment.jersey().register(new ErrorEntityWriter<ErrorMessage, View>(MediaType.TEXT_HTML_TYPE, View.class) {
            @Override
            protected View getRepresentation(ErrorMessage errorMessage) {
                return new ErrorView(errorMessage);
            }
        });
        environment.jersey().register(new ErrorEntityWriter<ValidationErrorMessage, View>(MediaType.TEXT_HTML_TYPE, View.class) {
            @Override
            protected View getRepresentation(ValidationErrorMessage message) {
                return new ValidationErrorView(message);
            }
        });
    }
}
