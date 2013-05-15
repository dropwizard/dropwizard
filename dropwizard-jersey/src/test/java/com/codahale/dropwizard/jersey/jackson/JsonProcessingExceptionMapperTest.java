package com.codahale.dropwizard.jersey.jackson;

import com.codahale.dropwizard.jersey.errors.ErrorMessage;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.fest.assertions.api.Assertions.assertThat;

public class JsonProcessingExceptionMapperTest {
    private final JsonProcessingExceptionMapper mapper = new JsonProcessingExceptionMapper();

    @Test
    public void returnsA500ForAJsonGenerationException() throws Exception {
        final JsonGenerationException e = new JsonGenerationException("oh no");

        final Response response = mapper.toResponse(e);
        assertThat(response.getStatus())
                .isEqualTo(500);
    }

    @Test
    public void returnsA500ForDeserializationProblems() throws Exception {
        final JsonMappingException e = new JsonMappingException("No suitable constructor found");

        final Response response = mapper.toResponse(e);
        assertThat(response.getStatus())
                .isEqualTo(500);
    }

    @Test
    public void returnsA400ForAllOtherProblems() throws Exception {
        final JsonMappingException e = new JsonMappingException("Bad stuff");

        final Response response = mapper.toResponse(e);

        assertThat(response.getStatus())
                .isEqualTo(400);

        assertThat(response.getEntity())
                .isInstanceOf(ErrorMessage.class);

        final ErrorMessage message = (ErrorMessage) response.getEntity();
        assertThat(message.getMessage())
                .isEqualTo("Bad stuff");
    }
}
