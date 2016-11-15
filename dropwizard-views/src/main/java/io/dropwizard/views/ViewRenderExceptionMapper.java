package io.dropwizard.views;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * An {@link ExceptionMapper} that returns a 500 error response with a generic
 * HTML error page when a {@link ViewRenderException} is thrown.
 * 
 * @since 1.1.0
 */
@Provider
public class ViewRenderExceptionMapper implements ExceptionMapper<WebApplicationException> {

    /**
     * The generic HTML error page template.
     */
    public static final String TEMPLATE_ERROR_MSG =
            "<html>" +
                "<head><title>Template Error</title></head>" +
                "<body><h1>Template Error</h1><p>Something went wrong rendering the page</p></body>" +
            "</html>";

    @Override
    public Response toResponse(WebApplicationException exception) {
        if (exception.getCause() instanceof ViewRenderException) {
            return Response.serverError()
                    .type(MediaType.TEXT_HTML_TYPE)
                    .entity(TEMPLATE_ERROR_MSG)
                    .build();
        }
        return exception.getResponse();
    }

}
