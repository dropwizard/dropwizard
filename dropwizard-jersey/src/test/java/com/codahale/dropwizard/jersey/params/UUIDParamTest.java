package com.codahale.dropwizard.jersey.params;

import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

public class UUIDParamTest {

    @Test
    public void aUUIDStringReturnsAUUIDObject() throws Exception {
        final String uuidString = "067e6162-3b6f-4ae2-a171-2470b63dff00";
        final UUID uuid = UUID.fromString(uuidString);

        final UUIDParam param = new UUIDParam(uuidString);
        assertThat(param.get())
                .isEqualTo(uuid);
    }

    @Test
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public void aNonUUIDThrowsAnException() throws Exception {
        try {
            new UUIDParam("foo");
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            final Response response = e.getResponse();

            assertThat(response.getStatus())
                    .isEqualTo(400);

            assertThat((String) response.getEntity())
                    .isEqualTo("\"foo\" is not a UUID.");
        }
    }
}
