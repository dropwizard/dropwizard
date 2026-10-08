package io.dropwizard.jersey.params;

import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class UUIDParamConverterTest {
    private final UUIDParamConverter converter = new UUIDParamConverter("id");

    @Test
    void parsesValidUuid() {
        final String uuidString = "067e6162-3b6f-4ae2-a171-2470b63dff00";
        assertThat(converter.fromString(uuidString)).isEqualTo(UUID.fromString(uuidString));
    }

    @Test
    void nullReturnsNull() {
        assertThat(converter.fromString(null)).isNull();
    }

    @Test
    void emptyStringIsRejected() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> converter.fromString(""))
                .withMessage("id is not a UUID.");
    }

    @Test
    void invalidUuidIsRejected() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> converter.fromString("not-a-uuid"))
                .withMessage("id is not a UUID.");
    }

    @Test
    void wrongLengthIsRejected() {
        assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> converter.fromString("067e61623b6f4ae2a1712470b63dff00"))
                .withMessage("id is not a UUID.");
    }

    @Test
    void toStringReturnsCanonicalForm() {
        final UUID uuid = UUID.fromString("067e6162-3b6f-4ae2-a171-2470b63dff00");
        assertThat(converter.toString(uuid)).isEqualTo(uuid.toString());
    }
}
