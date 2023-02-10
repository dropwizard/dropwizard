package io.dropwizard.logging.json.layout;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class MapBuilderTest {

    private int size = 4;
    private TimestampFormatter timestampFormatter = new TimestampFormatter("yyyy-MM-dd'T'HH:mm:ss.SSSZ", ZoneId.of("UTC"));
    private MapBuilder mapBuilder = new MapBuilder(timestampFormatter, Collections.emptyMap(), Collections.emptyMap(), size);
    private String message = "Since the dawn of time...";

    @Test
    void testIncludeStringValue() {
        assertThat(mapBuilder.add("message", true, message).build())
            .containsOnly(entry("message", message));
    }

    @Test
    void testDoNotIncludeStringValue() {
        assertThat(mapBuilder.add("message", false, message).build()).isEmpty();
    }

    @Test
    void testDoNotIncludeNullStringValue() {
        String value = null;
        assertThat(mapBuilder.add("message", true, value).build()).isEmpty();
    }

    @Test
    void testIncludeNumberValue() {
        assertThat(mapBuilder.addNumber("status", true, 200)
            .build()).containsOnly(entry("status", 200));
    }

    @Test
    void testIncludeMapValue() {
        assertThat(mapBuilder.add("headers", true, Collections.singletonMap("userAgent", "Lynx/2.8.7"))
            .build()).containsOnly(entry("headers", Collections.singletonMap("userAgent", "Lynx/2.8.7")));
    }

    @Test
    void testDoNotIncludeEmptyMapValue() {
        assertThat(mapBuilder.add("headers", true, Collections.emptyMap()).build()).isEmpty();
    }

    @Test
    void testDoNotIncludeNullNumberValue() {
        Double value = null;
        assertThat(mapBuilder.addNumber("status", true, value).build()).isEmpty();
    }

    @Test
    void testIncludeFormattedTimestamp() {
        assertThat(mapBuilder.addTimestamp("timestamp", true, 1514906361000L).build())
            .containsOnly(entry("timestamp", "2018-01-02T15:19:21.000+0000"));
    }

    @Test
    void testIncludeNotFormattedTimestamp() {
        assertThat(new MapBuilder(new TimestampFormatter(null, ZoneId.of("UTC")), Collections.emptyMap(),
            Collections.emptyMap(), size)
            .addTimestamp("timestamp", true, 1514906361000L)
            .build()).containsOnly(entry("timestamp", 1514906361000L));
    }

    @Test
    void testReplaceStringFieldName() {
        assertThat(new MapBuilder(timestampFormatter, Collections.singletonMap("message", "@message"), Collections.emptyMap(), size)
            .add("message", true, message)
            .build()).containsOnly(entry("@message", message));
    }

    @Test
    void testReplaceNumberFieldName() {
        assertThat(new MapBuilder(timestampFormatter, Collections.singletonMap("status", "@status"), Collections.emptyMap(), size)
            .addNumber("status", true, 200)
            .build()).containsOnly(entry("@status", 200));
    }

    @Test
    void testAddAdditionalField() {
        assertThat(new MapBuilder(timestampFormatter, Collections.emptyMap(), Collections.singletonMap("version", "1.8.3"), size)
            .add("message", true, message).build())
            .containsOnly(entry("message", message), entry("version", "1.8.3"));
    }

    @Test
    void testAddSupplier() {
        assertThat(mapBuilder.add("message", true, () -> message).build())
            .containsOnly(entry("message", message));
    }
    @Test
    void testAddNumberSupplier() {
        assertThat(mapBuilder.addNumber("status", true, () -> 200)
            .build()).containsOnly(entry("status", 200));
    }
    @Test
    void testAddMapSupplier() {
        assertThat(mapBuilder.addMap("headers", true, () -> Collections.singletonMap("userAgent", "Lynx/2.8.7"))
            .build()).containsOnly(entry("headers", Collections.singletonMap("userAgent", "Lynx/2.8.7")));
    }

    @Test
    void testAddSupplierNotInvoked() {
        assertThat(mapBuilder.add("status", false, () -> {throw new RuntimeException();}).build()).isEmpty();
    }
    @Test
    void testAddNumberSupplierNotInvoked() {
        assertThat(mapBuilder.addNumber("status", false, () -> {throw new RuntimeException();}).build()).isEmpty();
    }
    @Test
    void testAddMapSupplierNotInvoked() {
        assertThat(mapBuilder.addMap("status", false, () -> {throw new RuntimeException();}).build()).isEmpty();
    }

    @Test
    void testTimestampIsAlwaysFirst() {
        mapBuilder.add("status", true, "200");
        mapBuilder.addTimestamp("timestamp", true, 1514906361000L);
        mapBuilder.addNumber("code", true, 123);
        mapBuilder.addTimestamp("timestamp2", true, 1514906361000L);

        assertThat(mapBuilder.build().keySet())
            .containsExactly("timestamp", "status", "code", "timestamp2");
    }
    @Test
    void testTimestampIsAlwaysFirstWhenRenamed() {
        final MapBuilder mapBuilder = new MapBuilder(timestampFormatter,
            Collections.singletonMap("timestamp", "renamed-timestamp"), Collections.emptyMap(), size);

        mapBuilder.add("status", true, "200");
        mapBuilder.addNumber("code", true, 123);
        mapBuilder.addTimestamp("timestamp2", true, 1514906361000L);
        mapBuilder.addTimestamp("timestamp", true, 1514906361000L);

        assertThat(mapBuilder.build().keySet())
            .containsExactly("renamed-timestamp", "status", "code", "timestamp2");
    }
}
