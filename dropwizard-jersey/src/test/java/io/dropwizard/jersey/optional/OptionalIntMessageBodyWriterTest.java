package io.dropwizard.jersey.optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import io.dropwizard.jersey.AbstractJerseyTest;
import io.dropwizard.jersey.DropwizardResourceConfig;
import java.util.OptionalInt;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

class OptionalIntMessageBodyWriterTest extends AbstractJerseyTest {

    @Override
    protected Application configure() {
        return DropwizardResourceConfig.forTesting()
                .register(new EmptyOptionalExceptionMapper())
                .register(OptionalIntReturnResource.class);
    }

    @Test
    void presentOptionalsReturnTheirValue() {
        assertThat(target("optional-return").queryParam("id", "1").request().get(Integer.class))
                .isEqualTo(1);
    }

    @Test
    void presentOptionalsReturnTheirValueWithResponse() {
        assertThat(target("optional-return/response-wrapped")
                        .queryParam("id", "1")
                        .request()
                        .get(Integer.class))
                .isEqualTo(1);
    }

    @Test
    void absentOptionalsThrowANotFound() {
        Invocation.Builder request = target("optional-return").request();
        assertThatExceptionOfType(WebApplicationException.class)
                .isThrownBy(() -> request.get(Integer.class))
                .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(404));
    }

    @Test
    void valueSetIgnoresDefault() {
        assertThat(target("optional-return/default")
                        .queryParam("id", "1")
                        .request()
                        .get(Integer.class))
                .isEqualTo(target("optional-return/int/default")
                        .queryParam("id", "1")
                        .request()
                        .get(Integer.class))
                .isEqualTo(1);
    }

    @Test
    void valueNotSetReturnsDefault() {
        assertThat(target("optional-return/default").request().get(Integer.class))
                .isEqualTo(target("optional-return/int/default").request().get(Integer.class))
                .isEqualTo(0);
    }

    @Test
    void valueEmptyReturns404() {
        assertThat(target("optional-return/default")
                        .queryParam("id", "")
                        .request()
                        .get())
                .extracting(Response::getStatus)
                .isEqualTo(404);
    }

    @Test
    void valueInvalidReturns404() {
        Invocation.Builder request =
                target("optional-return/default").queryParam("id", "invalid").request();
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> request.get(Integer.class));
        Invocation.Builder intRequest = target("optional-return/int/default")
                .queryParam("id", "invalid")
                .request();
        assertThatExceptionOfType(NotFoundException.class).isThrownBy(() -> intRequest.get(Integer.class));
    }

    @Path("optional-return")
    @Produces(MediaType.TEXT_PLAIN)
    public static class OptionalIntReturnResource {
        @GET
        public OptionalInt showWithQueryParam(@QueryParam("id") OptionalInt id) {
            return id;
        }

        @POST
        public OptionalInt showWithFormParam(@FormParam("id") OptionalInt id) {
            return id;
        }

        @Path("response-wrapped")
        @GET
        public Response showWithQueryParamResponse(@QueryParam("id") OptionalInt id) {
            return Response.ok(id).build();
        }

        @Path("default")
        @GET
        public OptionalInt showWithQueryParamAndDefaultValue(@QueryParam("id") @DefaultValue("0") OptionalInt id) {
            return id;
        }

        @Path("int/default")
        @GET
        public Integer showWithLongQueryParamAndDefaultValue(@QueryParam("id") @DefaultValue("0") Integer id) {
            return id;
        }
    }
}
