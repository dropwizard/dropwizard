package io.dropwizard.jersey.params;

import io.dropwizard.jersey.errors.ErrorMessage;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UUIDParamTest {
    private void badUUID(WebApplicationException e) {
        final Response response = e.getResponse();

        assertThat(response.getStatus())
            .isEqualTo(400);

        ErrorMessage entity = (ErrorMessage) response.getEntity();
        assertThat(entity.getCode()).isEqualTo(400);
        assertThat(entity.getMessage())
            .isEqualTo("Parameter is not a UUID.");
    }

    @Test
    public void aUUIDStringReturnsAUUIDObject() {
        final String uuidString = "067e6162-3b6f-4ae2-a171-2470b63dff00";
        final UUID uuid = UUID.fromString(uuidString);

        final UUIDParam param = new UUIDParam(uuidString);
        assertThat(param.get())
                .isEqualTo(uuid);
    }

    @Test
    public void noSpaceUUID() {
        assertThatThrownBy(() -> new UUIDParam("067e61623b6f4ae2a1712470b63dff00"))
            .isInstanceOfSatisfying(WebApplicationException.class, this::badUUID);
    }

    @Test
    public void tooLongUUID() {
        assertThatThrownBy(() -> new UUIDParam("067e6162-3b6f-4ae2-a171-2470b63dff000"))
            .isInstanceOfSatisfying(WebApplicationException.class, this::badUUID);
    }

    @Test
    public void aNonUUIDThrowsAnException() {
        assertThatThrownBy(() -> new UUIDParam("foo"))
            .isInstanceOfSatisfying(WebApplicationException.class, this::badUUID);
    }
}
