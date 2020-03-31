package io.dropwizard.jersey.optional;

import io.dropwizard.jersey.AbstractJerseyTest;
import io.dropwizard.jersey.DropwizardResourceConfig;
import org.junit.jupiter.api.Test;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.OptionalInt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class OptionalIntMessageBodyWriterTest extends AbstractJerseyTest {

    @Override
    protected Application configure() {
        return DropwizardResourceConfig.forTesting()
                .register(new EmptyOptionalExceptionMapper())
                .register(OptionalIntReturnResource.class);
    }

    @Test
    public void presentOptionalsReturnTheirValue() throws Exception {
        assertThat(target("optional-return")
                .queryParam("id", "1").request()
                .get(Integer.class))
                .isEqualTo(1);
    }

    @Test
    public void presentOptionalsReturnTheirValueWithResponse() throws Exception {
        assertThat(target("optional-return/response-wrapped")
                .queryParam("id", "1").request()
                .get(Integer.class))
                .isEqualTo(1);
    }

    @Test
    public void absentOptionalsThrowANotFound() throws Exception {
        try {
            target("optional-return").request().get(Integer.class);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(404);
        }
    }

    @Test
    public void valueSetIgnoresDefault() {
        assertThat(target("optional-return/default").queryParam("id", "1").request().get(Integer.class))
            .isEqualTo(target("optional-return/int/default").queryParam("id", "1").request().get(Integer.class))
            .isEqualTo(1);
    }

    @Test
    public void valueNotSetReturnsDefault() {
        assertThat(target("optional-return/default").request().get(Integer.class))
            .isEqualTo(target("optional-return/int/default").request().get(Integer.class))
            .isEqualTo(0);
    }

    @Test
    public void valueEmptyReturnsDefault() {
        assertThat(target("optional-return/default").queryParam("id", "").request().get(Integer.class))
            .isEqualTo(target("optional-return/int/default").queryParam("id", "").request().get(Integer.class))
            .isEqualTo(0);
    }

    @Test
    public void valueInvalidReturns404() {
        assertThatThrownBy(() -> target("optional-return/default").queryParam("id", "invalid").request().get(Integer.class))
            .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> target("optional-return/int/default").queryParam("id", "invalid").request().get(Integer.class))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void verifyInvalidDefaultValueFailsFast() {
        assertThatThrownBy(() -> new OptionalIntParamConverterProvider.OptionalIntParamConverter("invalid").fromString("invalid"))
            .isInstanceOf(NumberFormatException.class);
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
