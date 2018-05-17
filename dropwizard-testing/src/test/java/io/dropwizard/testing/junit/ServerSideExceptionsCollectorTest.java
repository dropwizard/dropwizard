package io.dropwizard.testing.junit;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.model.MultipleFailureException;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import io.dropwizard.setup.Environment;
import io.dropwizard.testing.app.DropwizardTestApplication;
import io.dropwizard.testing.app.TestConfiguration;
import io.dropwizard.testing.junit.DropwizardAppRule.ServerSideExceptionsCollector;

public class ServerSideExceptionsCollectorTest
{

    public static class DropwizardTestApplicationWithLogCollector extends DropwizardTestApplication
    {
        @Override
        public void run(TestConfiguration configuration, Environment environment) throws Exception
        {
            super.run(configuration, environment);

            // errors will be handled on server side
            environment.jersey().register(new ExceptionMapper<Throwable>()
            {
                @Override
                public Response toResponse(Throwable exception)
                {
                    return Response.status(400).build();
                }
            });
        }
    }

    @ClassRule
    public static final DropwizardAppRule<TestConfiguration> APP1_WITHOUT_LOG_COLLECTION = new DropwizardAppRule<>(
        DropwizardTestApplication.class, resourceFilePath("test-config.yaml"));
    @Rule
    public final ServerSideExceptionsCollector app1ErrorCollector = APP1_WITHOUT_LOG_COLLECTION
        .collectServerSideExceptions();

    @ClassRule
    public static final DropwizardAppRule<TestConfiguration> APP2_WITH_LOG_COLLECTION = new DropwizardAppRule<>(
        DropwizardTestApplicationWithLogCollector.class, resourceFilePath("test-config.yaml"));
    @Rule
    public final ServerSideExceptionsCollector app2ErrorCollector = APP2_WITH_LOG_COLLECTION
        .collectServerSideExceptions();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void canCollectServerSideErrors()
    {
        thrown.expect(MultipleFailureException.class);
        thrown.expectMessage("This is an server side exception");

        APP1_WITHOUT_LOG_COLLECTION.client().target("http://localhost:" + APP1_WITHOUT_LOG_COLLECTION.getLocalPort() + "/throwError")
            .request()
            .put(Entity.entity("This is an server side exception", MediaType.TEXT_PLAIN), String.class);
    }

    @Test
    public void wontCollectErrorsAlreadyHandled()
    {
        final Response response = APP2_WITH_LOG_COLLECTION.client().target("http://localhost:"
            + APP2_WITH_LOG_COLLECTION.getLocalPort() + "/throwError")
            .request()
            .put(Entity.entity("This is an server side exception", MediaType.TEXT_PLAIN));

        assertThat(response.getStatus(), equalTo(400));
    }

}
