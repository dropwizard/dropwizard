package io.dropwizard.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.fasterxml.jackson.module.paramnames.PackageVersion;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Module that provides safe deserializers for Instant and Duration that reject big decimal values
 * outside of their range which are extremely CPU-heavy to parse.
 */
class SafeJavaTimeModule extends SimpleModule {

    private static final InstantDeserializer<Instant> INSTANT = new SafeInstantDeserializer<>(
        Instant.class, DateTimeFormatter.ISO_INSTANT,
        Instant::from,
        a -> Instant.ofEpochMilli(a.value),
        a -> Instant.ofEpochSecond(a.integer, a.fraction),
        null,
        true
    );

    private static final InstantDeserializer<OffsetDateTime> OFFSET_DATE_TIME = new SafeInstantDeserializer<>(
        OffsetDateTime.class, DateTimeFormatter.ISO_OFFSET_DATE_TIME,
        OffsetDateTime::from,
        a -> OffsetDateTime.ofInstant(Instant.ofEpochMilli(a.value), a.zoneId),
        a -> OffsetDateTime.ofInstant(Instant.ofEpochSecond(a.integer, a.fraction), a.zoneId),
        (d, z) -> d.withOffsetSameInstant(z.getRules().getOffset(d.toLocalDateTime())),
        true
    );

    private static final InstantDeserializer<ZonedDateTime> ZONED_DATE_TIME = new SafeInstantDeserializer<>(
        ZonedDateTime.class, DateTimeFormatter.ISO_ZONED_DATE_TIME,
        ZonedDateTime::from,
        a -> ZonedDateTime.ofInstant(Instant.ofEpochMilli(a.value), a.zoneId),
        a -> ZonedDateTime.ofInstant(Instant.ofEpochSecond(a.integer, a.fraction), a.zoneId),
        ZonedDateTime::withZoneSameInstant,
        false
    );

    SafeJavaTimeModule() {
        super(PackageVersion.VERSION);
        addDeserializer(Instant.class, INSTANT);
        addDeserializer(OffsetDateTime.class, OFFSET_DATE_TIME);
        addDeserializer(ZonedDateTime.class, ZONED_DATE_TIME);
        addDeserializer(Duration.class, new SafeDurationDeserializer());
    }
}
