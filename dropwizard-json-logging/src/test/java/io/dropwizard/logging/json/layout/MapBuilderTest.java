package io.dropwizard.logging.json.layout;

import org.junit.Test;

import java.time.ZoneId;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class MapBuilderTest {

    private int size = 4;
    private TimestampFormatter timestampFormatter = new TimestampFormatter("yyyy-MM-dd'T'HH:mm:ss.SSSZ", ZoneId.of("UTC"));
    private MapBuilder mapBuilder = new MapBuilder(timestampFormatter, Collections.emptyMap(), Collections.emptyMap(), size);
    private String message = "Since the dawn of time...";

    @Test
    public void testIncludeStringValue() {
        assertThat(mapBuilder.add("message", true, message).build())
            .containsOnly(entry("message", message));
    }

    @Test
    public void testDoNotIncludeStringValue() {
        assertThat(mapBuilder.add("message", false, message).build()).isEmpty();
    }

    @Test
    public void testDoNotIncludeNullStringValue() {
        String value = null;
        assertThat(mapBuilder.add("message", true, value).build()).isEmpty();
    }

    @Test
    public void testIncludeNumberValue() {
        assertThat(mapBuilder.add("status", true, 200)
            .build()).containsOnly(entry("status", 200));
    }

    @Test
    public void testIncludeMapValue() {
        assertThat(mapBuilder.add("headers", true, Collections.singletonMap("userAgent", "Lynx/2.8.7"))
            .build()).containsOnly(entry("headers", Collections.singletonMap("userAgent", "Lynx/2.8.7")));
    }

    @Test
    public void testDoNotIncludeEmptyMapValue() {
        assertThat(mapBuilder.add("headers", true, Collections.emptyMap()).build()).isEmpty();
    }

    @Test
    public void testDoNotIncludeNullNumberValue() {
        Double value = null;
        assertThat(mapBuilder.add("status", true, value).build()).isEmpty();
    }

    @Test
    public void testIncludeFormattedTimestamp() {
        assertThat(mapBuilder.addTimestamp("timestamp", true, 1514906361000L).build())
            .containsOnly(entry("timestamp", "2018-01-02T15:19:21.000+0000"));
    }

    @Test
    public void testIncludeNotFormattedTimestamp() {
        assertThat(new MapBuilder(new TimestampFormatter(null, ZoneId.of("UTC")), Collections.emptyMap(),
            Collections.emptyMap(), size)
            .addTimestamp("timestamp", true, 1514906361000L)
            .build()).containsOnly(entry("timestamp", 1514906361000L));
    }

    @Test
    public void testReplaceStringFieldName() {
        assertThat(new MapBuilder(timestampFormatter, Collections.singletonMap("message", "@message"), Collections.emptyMap(), size)
            .add("message", true, message)
            .build()).containsOnly(entry("@message", message));
    }

    @Test
    public void testReplaceNumberFieldName() {
        assertThat(new MapBuilder(timestampFormatter, Collections.singletonMap("status", "@status"), Collections.emptyMap(), size)
            .add("status", true, 200)
            .build()).containsOnly(entry("@status", 200));
    }

    @Test
    public void testAddAdditionalField() {
        assertThat(new MapBuilder(timestampFormatter, Collections.emptyMap(), Collections.singletonMap("version", "1.8.3"), size)
            .add("message", true, message).build())
            .containsOnly(entry("message", message), entry("version", "1.8.3"));
    }
}
