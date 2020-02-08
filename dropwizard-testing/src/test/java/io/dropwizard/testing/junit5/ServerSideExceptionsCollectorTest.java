package io.dropwizard.testing.junit5;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import io.dropwizard.setup.Environment;
import io.dropwizard.testing.app.DropwizardTestApplication;
import io.dropwizard.testing.app.TestConfiguration;

@ExtendWith(DropwizardExtensionsSupport.class)
public class ServerSideExceptionsCollectorTest
{

    public static final DropwizardAppExtension<TestConfiguration> APP1_WITHOUT_LOG_COLLECTION =
        new DropwizardAppExtension<>(DropwizardTestApplication.class, resourceFilePath("test-config.yaml"));

    @Test
    public void canCollectServerSideErrors()
    {
        Assertions.assertThrows(
            InternalServerErrorException.class,
            () -> {
                APP1_WITHOUT_LOG_COLLECTION.client().target("http://localhost:" + APP1_WITHOUT_LOG_COLLECTION
                    .getLocalPort() + "/throwError")
                    .request()
                    .put(Entity.entity("This is an server side exception", MediaType.TEXT_PLAIN), String.class);
            },
            // check if the server exception message was attached to original InternalServerErrorException
            "This is an server side exception"
        );
    }

    public static class DropwizardTestApplicationWithLogCollector extends DropwizardTestApplication
    {
        @Override
        public void run(TestConfiguration configuration, Environment environment) throws Exception
        {
            super.run(configuration, environment);
            // errors will be handled on server side
            environment.jersey().register(new LogExceptionMapper());
        }

        @Provider
        private static class LogExceptionMapper implements ExceptionMapper<Throwable>{

            @Override
            public Response toResponse(Throwable exception)
            {
            return Response.status(400)
                .entity(exception.getMessage())
                .build();
            }

        }

    }

    public static final DropwizardAppExtension<TestConfiguration> APP2_WITH_LOG_COLLECTION =
        new DropwizardAppExtension<>(DropwizardTestApplicationWithLogCollector.class, resourceFilePath("test-config.yaml"));

    @Test
    public void wontCollectErrorsAlreadyHandled()
    {
        final Response response = APP2_WITH_LOG_COLLECTION.client().target("http://localhost:"
            + APP2_WITH_LOG_COLLECTION.getLocalPort() + "/throwError")
            .request()
            .put(Entity.entity("Server will capture this exception and send http error", MediaType.TEXT_PLAIN));

        assertThat(response.getStatus(), equalTo(400));
    }

}
