package io.dropwizard.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MapsTest {

    @Test
    void of2KeyValuePairs() {
        final Map<Integer,Integer> map = Maps.of(1, 60, 2, 61);
        final HashMap<Integer,Integer> hashMap = new HashMap<>();
        hashMap.put(1, 60);
        hashMap.put(2, 61);

        assertThat(map).isEqualTo(hashMap);
    }

    @Test
    void of3KeyValuePairs() {
        final Map<Integer,Integer> map = Maps.of(1, 60, 2, 61, 3, 62);
        final HashMap<Integer,Integer> hashMap = new HashMap<>();
        hashMap.put(1, 60);
        hashMap.put(2, 61);
        hashMap.put(3, 62);

        assertThat(map).isEqualTo(hashMap);
    }

    @Test
    void of4KeyValuePairs() {
        final Map<Integer,Integer> map = Maps.of(1, 60, 2, 61, 3, 62, 4, 63);
        final HashMap<Integer,Integer> hashMap = new HashMap<>();
        hashMap.put(1, 60);
        hashMap.put(2, 61);
        hashMap.put(3, 62);
        hashMap.put(4, 63);

        assertThat(map).isEqualTo(hashMap);
    }

    @Test
    void of5KeyValuePairs() {
        final Map<Integer,Integer> map = Maps.of(1, 60, 2, 61, 3, 62, 4, 63, 5, 64);
        final HashMap<Integer,Integer> hashMap = new HashMap<>();
        hashMap.put(1, 60);
        hashMap.put(2, 61);
        hashMap.put(3, 62);
        hashMap.put(4, 63);
        hashMap.put(5, 64);

        assertThat(map).isEqualTo(hashMap);
    }
}
