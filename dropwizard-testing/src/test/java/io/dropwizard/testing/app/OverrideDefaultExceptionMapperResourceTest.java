package io.dropwizard.testing.app;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.ClassRule;
import org.junit.Test;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Test to make sure that the default exception mappers registered in
 * {@link ResourceTestRule} can be overridden
 */
public class OverrideDefaultExceptionMapperResourceTest {
    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new PersonResource(mock(PeopleStore.class)))
            .addProvider(new CustomMapper())
            .build();

    @Test
    public void testThatCustomConstraintExceptionMapperIsRegistered() {
        final Response resp = resources.client().target("/person/blah").request()
                .post(Entity.json("{}"));

        assertThat(resp.getStatus()).isEqualTo(204);
    }

    private static class CustomMapper implements ExceptionMapper<ConstraintViolationException> {
        @Override
        public Response toResponse(ConstraintViolationException exception) {
            return Response.noContent().build();
        }
    }
}
