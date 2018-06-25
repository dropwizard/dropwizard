package io.dropwizard.util;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;


public class ByteStreamsTest {

    @Test
    public void testToByteArray() throws IOException {
        byte[] input = new byte[4010];
        for (int i = 0; i < 4010; i++) {
            input[i] = 0x1A;
        }
        assertThat(ByteStreams.toByteArray(new ByteArrayInputStream(input)))
            .isEqualTo(input);
    }
}
