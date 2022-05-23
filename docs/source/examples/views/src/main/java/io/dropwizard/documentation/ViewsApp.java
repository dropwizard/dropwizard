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
        // views: ViewsApp#initialize->ViewBundle
        bootstrap.addBundle(new ViewBundle<>());
        // views: ViewsApp#initialize->ViewBundle

        // Custom configuration
        // views: ViewsApp#initialize->ViewBundle->custom
        bootstrap.addBundle(new ViewBundle<ViewsConfiguration>() {
            @Override
            public Map<String, Map<String, String>> getViewConfiguration(ViewsConfiguration config) {
                return config.getViewRendererConfiguration();
            }
        });
        // views: ViewsApp#initialize->ViewBundle->custom
    }

    @Override
    public void run(ViewsConfiguration configuration, Environment environment) throws Exception {
        // views: ViewsApp#run->ExtendedExceptionMapper->ViewRenderException
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
        // views: ViewsApp#run->ExtendedExceptionMapper->ViewRenderException

        // views: ViewsApp#run->ExtendedExceptionMapper->MustacheNotFoundException
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
        // views: ViewsApp#run->ExtendedExceptionMapper->MustacheNotFoundException

        // views: ViewsApp#run->ErrorEntityWriter->ErrorMessage
        environment.jersey().register(new ErrorEntityWriter<ErrorMessage, View>(MediaType.TEXT_HTML_TYPE, View.class) {
            @Override
            protected View getRepresentation(ErrorMessage errorMessage) {
                return new ErrorView(errorMessage);
            }
        });
        // views: ViewsApp#run->ErrorEntityWriter->ErrorMessage
        // views: ViewsApp#run->ErrorEntityWriter->ValidationErrorMessage
        environment.jersey().register(new ErrorEntityWriter<ValidationErrorMessage, View>(MediaType.TEXT_HTML_TYPE, View.class) {
            @Override
            protected View getRepresentation(ValidationErrorMessage message) {
                return new ValidationErrorView(message);
            }
        });
        // views: ViewsApp#run->ErrorEntityWriter->ValidationErrorMessage
    }
}
