package io.dropwizard.jackson;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Safe deserializer for `Instant` that rejects big decimal values that take forever to deserialize
 * and can be used in a DoS attack.
 */
class SafeInstantDeserializer<T extends Temporal> extends InstantDeserializer<T> {

    private static final BigDecimal MAX_INSTANT = new BigDecimal(Instant.MAX.getEpochSecond() + 1);
    private static final BigDecimal MIN_INSTANT = new BigDecimal(Instant.MIN.getEpochSecond());

    SafeInstantDeserializer(Class<T> supportedType,
                            DateTimeFormatter formatter,
                            Function<TemporalAccessor, T> parsedToValue,
                            Function<FromIntegerArguments, T> fromMilliseconds,
                            Function<FromDecimalArguments, T> fromNanoseconds,
                            @Nullable BiFunction<T, ZoneId, T> adjust,
                            boolean replaceZeroOffsetAsZ) {
        super(supportedType, formatter, parsedToValue, fromMilliseconds, fromNanoseconds, adjust, replaceZeroOffsetAsZ);
    }

    @Override
    protected T _fromDecimal(DeserializationContext context, BigDecimal value) {
        // new BigDecimal("1e1000000000").longValue() takes forever to complete
        if (value.compareTo(MAX_INSTANT) >= 0 || value.compareTo(MIN_INSTANT) < 0) {
            throw new IllegalArgumentException("Value is out of range of Instant");
        }
        return super._fromDecimal(context, value);
    }
}
