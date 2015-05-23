package io.dropwizard.jersey.errors;

import org.eclipse.jetty.io.EofException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
* This class is intended to catch Early EOF errors that occur when the client disconnects while the server is reading
* from the input stream.
*
* We catch the org.ecplise.jetty.io.EofException rather than the more generic java.io.EOFException to ensure that we're
* only catching jetty server based errors where the client disconnects, as specified by {@link EofException}.
*/
@Provider
public class EarlyEofExceptionMapper implements ExceptionMapper<EofException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EarlyEofExceptionMapper.class);

    @Override
    public Response toResponse(EofException e) {
        LOGGER.debug("EOF Exception encountered - client disconnected during stream processing.", e);

        return Response.status(Response.Status.BAD_REQUEST).build();
    }
}
