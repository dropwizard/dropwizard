package io.dropwizard.jersey.params;

import io.dropwizard.jersey.errors.ErrorMessage;
import org.junit.jupiter.api.Test;

import javax.ws.rs.WebApplicationException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class UUIDParamTest {
    private void UuidParamNegativeTest(String input) {
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> new UUIDParam(input))
            .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(400))
            .satisfies(e -> assertThat(e.getResponse().getEntity()).isEqualTo(
                new ErrorMessage(400, "Parameter is not a UUID.")
            ));
    }

    @Test
    void aUUIDStringReturnsAUUIDObject() {
        final String uuidString = "067e6162-3b6f-4ae2-a171-2470b63dff00";
        final UUID uuid = UUID.fromString(uuidString);

        assertThat(new UUIDParam(uuidString).get()).isEqualTo(uuid);
    }

    @Test
    void noSpaceUUID() {
        UuidParamNegativeTest("067e61623b6f4ae2a1712470b63dff00");
    }

    @Test
    void tooLongUUID() {
        UuidParamNegativeTest("067e6162-3b6f-4ae2-a171-2470b63dff000");
    }

    @Test
    void aNonUUIDThrowsAnException() {
        UuidParamNegativeTest("foo");
    }
}
