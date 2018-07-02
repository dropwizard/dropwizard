package io.dropwizard.jersey.params;

import io.dropwizard.jersey.errors.ErrorMessage;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UUIDParamTest {
    private void UuidParamNegativeTest(String input) {
        assertThatThrownBy(() -> new UUIDParam(input))
            .isInstanceOfSatisfying(WebApplicationException.class, e -> {
                assertThat(e.getResponse().getStatus()).isEqualTo(400);
                assertThat(e.getResponse().getEntity()).isEqualTo(
                    new ErrorMessage(400, "Parameter is not a UUID.")
                );
            });
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
        UuidParamNegativeTest("067e61623b6f4ae2a1712470b63dff00");
    }

    @Test
    public void tooLongUUID() {
        UuidParamNegativeTest("067e6162-3b6f-4ae2-a171-2470b63dff000");
    }

    @Test
    public void aNonUUIDThrowsAnException() {
        UuidParamNegativeTest("foo");
    }
}
