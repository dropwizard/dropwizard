package io.dropwizard.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.DurationDeserializer;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;

/**
 * Safe deserializer for `Instant` that rejects big decimal values out of the range of Long.
 * They take forever to deserialize and can be used in a DoS attack.
 */
class SafeDurationDeserializer extends StdScalarDeserializer<Duration> {

    private static final BigDecimal MAX_DURATION = new BigDecimal(Long.MAX_VALUE);
    private static final BigDecimal MIN_DURATION = new BigDecimal(Long.MIN_VALUE);

    SafeDurationDeserializer() {
        super(Duration.class);
    }

    @Override
    @Nullable
    public Duration deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (parser.getCurrentTokenId() == JsonTokenId.ID_NUMBER_FLOAT) {
            BigDecimal value = parser.getDecimalValue();
            // new BigDecimal("1e1000000000").longValue() takes forever to complete
            if (value.compareTo(MAX_DURATION) > 0 || value.compareTo(MIN_DURATION) < 0) {
                throw new IllegalArgumentException("Value is out of range of Duration");
            }
        }
        return DurationDeserializer.INSTANCE.deserialize(parser, context);
    }
}
