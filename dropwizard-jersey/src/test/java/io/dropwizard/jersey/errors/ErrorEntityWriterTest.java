package io.dropwizard.jersey.errors;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.jersey.AbstractJerseyTest;
import io.dropwizard.jersey.DropwizardResourceConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class ErrorEntityWriterTest extends AbstractJerseyTest {

    public static class ErrorEntityWriterTestResourceConfig extends DropwizardResourceConfig {
        public ErrorEntityWriterTestResourceConfig() {
            super(true, new MetricRegistry());

            register(DefaultLoggingExceptionMapper.class);
            register(DefaultJacksonMessageBodyProvider.class);
            register(ExceptionResource.class);
            register(new ErrorEntityWriter<ErrorMessage, String>(MediaType.TEXT_HTML_TYPE, String.class) {
                @Override
                protected String getRepresentation(ErrorMessage entity) {
                    return "<!DOCTYPE html><html><body>" + entity.getMessage() + "</body></html>";
                }
            });
        }
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new GrizzlyWebTestContainerFactory();
    }

    @Override
    protected DeploymentContext configureDeployment() {
        final ResourceConfig rc = new ErrorEntityWriterTestResourceConfig();
        return ServletDeploymentContext.builder(rc)
            .initParam(ServletProperties.JAXRS_APPLICATION_CLASS, ErrorEntityWriterTestResourceConfig.class.getName())
            .build();
    }

    @Test
    public void formatsErrorsAsHtml() {

        try {
            target("/exception/html-exception")
                .request(MediaType.TEXT_HTML_TYPE)
                .get(String.class);

            failBecauseExceptionWasNotThrown(WebApplicationException.class);

        } catch (WebApplicationException e) {
            final Response response = e.getResponse();
            assertThat(response.getStatus()).isEqualTo(400);
            assertThat(response.getMediaType()).isEqualTo(MediaType.TEXT_HTML_TYPE);
            assertThat(response.readEntity(String.class)).isEqualTo("<!DOCTYPE html><html><body>BIFF</body></html>");
        }
    }

}
