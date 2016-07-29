package io.dropwizard.auth.principal;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contains resource methods which don't authenticate but use principal instance injection and thus might be affected by authentication logic.
 */
@Path("/no-auth-test")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.TEXT_PLAIN)
public class NoAuthPrincipalEntityResource {

    /**
     * Principal instance must be injected even when no authentication is required.
     */
    @POST
    @Path("principal-entity")
    public String principalEntityWithoutAuth(JsonPrincipal principal) {
        assertThat(principal).isNotNull();
        return principal.getName();
    }

    /**
     * Annotated principal instance must be injected even when no authentication is required.
     */
    @POST
    @Path("annotated-principal-entity")
    public String annotatedPrincipalEntityWithoutAuth(@DummyAnnotation JsonPrincipal principal) {
        assertThat(principal).isNotNull();
        return principal.getName();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.PARAMETER })
    public @interface DummyAnnotation {
    }
}
