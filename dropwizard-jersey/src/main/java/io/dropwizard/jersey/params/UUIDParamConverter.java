package io.dropwizard.jersey.params;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ext.ParamConverter;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * Converts request parameters to {@link UUID} values.
 *
 * <p>Jersey's default converters map an empty parameter value (for example {@code ?id=}) to
 * {@code null} when {@link UUID#fromString(String)} fails. For multi-valued parameters that yields
 * a collection containing a single {@code null} element. This converter rejects empty and
 * non-parsable values with {@code 400 Bad Request} instead.</p>
 *
 * @see <a href="https://github.com/dropwizard/dropwizard/issues/3435">dropwizard/dropwizard#3435</a>
 * @since 5.0.3
 */
public class UUIDParamConverter implements ParamConverter<UUID> {
    private final String parameterName;

    public UUIDParamConverter(String parameterName) {
        this.parameterName = parameterName;
    }

    @Override
    @Nullable
    public UUID fromString(@Nullable String value) {
        // Missing parameters are represented as null; leave them as null so optional single
        // values and absent multi-valued parameters keep working as before.
        if (value == null) {
            return null;
        }

        // Empty string is not a valid UUID. Jersey would otherwise swallow the parse failure and
        // insert null into List/Set/SortedSet parameters (see #3435).
        if (value.isEmpty()) {
            throw invalidUuid();
        }

        // Match UUIDParam: require RFC 4122 length (36) so extra hex is not silently accepted.
        if (value.length() != 36) {
            throw invalidUuid();
        }

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw invalidUuid();
        }
    }

    @Override
    public String toString(UUID value) {
        return value.toString();
    }

    private BadRequestException invalidUuid() {
        return new BadRequestException(parameterName + " is not a UUID.");
    }
}
