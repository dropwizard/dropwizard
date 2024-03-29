package io.dropwizard.testing.junit5;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.JerseyViolationException;
import io.dropwizard.testing.app.PeopleStore;
import io.dropwizard.testing.app.PersonResource;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.glassfish.jersey.spi.ExtendedExceptionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(DropwizardExtensionsSupport.class)
class PersonResourceExceptionMapperTest {

    private static final ObjectMapper OBJECT_MAPPER = Jackson.newObjectMapper()
        .registerModule(new GuavaModule());

    private PeopleStore peopleStore = mock(PeopleStore.class);
    private ResourceExtension resources = ResourceExtension.builder()
        .addResource(new PersonResource(peopleStore))
        .setRegisterDefaultExceptionMappers(false)
        .addProvider(new MyJerseyExceptionMapper())
        .addProvider(new GenericExceptionMapper())
        .setMapper(OBJECT_MAPPER)
        .build();

    @Test
    void testDefaultConstraintViolation() {
        assertThat(resources.target("/person/blah/index")
            .queryParam("ind", -1).request()
            .get().readEntity(String.class))
            .isEqualTo("Invalid data");
    }

    @Test
    void testDefaultJsonProcessingMapper() {
        assertThat(resources.target("/person/blah/runtime-exception")
            .request()
            .post(Entity.json("{ \"he: \"ho\"}"))
            .readEntity(String.class))
            .startsWith("Something went wrong: Unexpected character");
    }

    @Test
    void testDefaultExceptionMapper() {
        assertThat(resources.target("/person/blah/runtime-exception")
            .request()
            .post(Entity.json("{}"))
            .readEntity(String.class))
            .isEqualTo("Something went wrong: I'm an exception!");
    }

    @Test
    void testDefaultEofExceptionMapper() {
        assertThat(resources.target("/person/blah/eof-exception")
            .request()
            .get().readEntity(String.class))
            .isEqualTo("Something went wrong: I'm an eof exception!");
    }

    private static class MyJerseyExceptionMapper implements ExceptionMapper<JerseyViolationException> {
        @Override
        public Response toResponse(JerseyViolationException exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.TEXT_PLAIN)
                .entity("Invalid data")
                .build();
        }
    }

    private static class GenericExceptionMapper implements ExtendedExceptionMapper<Throwable> {
        @Override
        public boolean isMappable(Throwable throwable) {
            return !(throwable instanceof JerseyViolationException);
        }

        @Override
        public Response toResponse(Throwable exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.TEXT_PLAIN)
                .entity(String.format("Something went wrong: %s", exception.getMessage()))
                .build();
        }
    }
}
