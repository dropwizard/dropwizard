package io.dropwizard.jersey.jsr310;

import org.junit.Test;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class InstantParamTest {
    @Test
    public void parsesInstants() throws Exception {
        final InstantParam param = new InstantParam("1488751730055");

        assertThat(param.get())
                .isEqualTo(Instant.ofEpochMilli(1488751730055L));
    }
}
