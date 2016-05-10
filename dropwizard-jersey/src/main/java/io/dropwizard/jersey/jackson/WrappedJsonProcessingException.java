package io.dropwizard.jersey.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;

public class WrappedJsonProcessingException extends JsonProcessingException {

    WrappedJsonProcessingException(Throwable cause) {
        super(cause.getMessage(), cause);
    }

}
