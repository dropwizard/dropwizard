package io.dropwizard.testing.junit;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class TestApplicationExceptionMappers extends Application<TestConfiguration> {
    @Override
    public void run(TestConfiguration configuration, Environment environment) throws Exception {
        environment.jersey().register(new ExceptionMapper<ConstraintViolationException>() {
            @Override
            public Response toResponse(ConstraintViolationException exception) {
                return Response.noContent().build();
            }
        });

        environment.jersey().register(new TestResourceWithValidations());
    }

    @Path("/")
    private static class TestResourceWithValidations {
        @POST
        public String endpoint(@NotEmpty String body) {
            return body;
        }
    }
}
