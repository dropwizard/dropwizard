package io.dropwizard.jersey.protobuf;

import io.dropwizard.jersey.protobuf.protos.DropwizardProtos.ErrorMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class InvalidProtocolBufferExceptionMapper implements ExceptionMapper<InvalidProtocolBufferException> {
    private static final Logger LOGGER = LoggerFactory.getLogger(InvalidProtocolBufferExceptionMapper.class);

    @Override
    public Response toResponse(InvalidProtocolBufferException exception) {
        final ErrorMessage message = ErrorMessage.newBuilder()
                             .setMessage(exception.getMessage()).build();

        LOGGER.debug("Unable to process protocol buffer message", exception);
        return Response.status(Response.Status.BAD_REQUEST)
                       .type(ProtocolBufferMediaType.APPLICATION_PROTOBUF_TYPE)
                       .entity(message)
                       .build();
    }
}
