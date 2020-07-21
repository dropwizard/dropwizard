package io.dropwizard.jersey.errors;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.jersey.AbstractJerseyTest;
import io.dropwizard.jersey.DropwizardResourceConfig;
import org.eclipse.jetty.io.EofException;
import org.glassfish.jersey.test.jetty.JettyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.LongAdder;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EofExceptionWriterInterceptorJerseyTest extends AbstractJerseyTest {
    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new JettyTestContainerFactory();
    }

    @Override
    protected Application configure() {
        return DropwizardResourceConfig.forTesting(new MetricRegistry())
            .register(EofExceptionWriterInterceptor.class)
            .register(EofExceptionCountingInterceptor.class)
            .register(TestResource.class);


    }

    @Test
    public void shouldCountZeroEofExceptions() throws IOException {
        target("/").request().get(InputStream.class).close();
        assertThat(EofExceptionCountingInterceptor.exceptionCount.doubleValue()).isEqualTo(0L);
    }

    @Path("/")
    public static class TestResource {
        @GET
        public Response streamForever() {
            final StreamingOutput output = os -> {
                //noinspection InfiniteLoopStatement
                while (true) {
                    os.write('a');
                    os.flush();
                }
            };

            return Response.ok(output).build();
        }
    }

    @Provider
    public static class EofExceptionCountingInterceptor implements WriterInterceptor {
        static final LongAdder exceptionCount = new LongAdder();

        @Override
        public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
            try {
                context.proceed();
            } catch (EofException e) {
                exceptionCount.increment();
                throw e;
            }
        }
    }
}
