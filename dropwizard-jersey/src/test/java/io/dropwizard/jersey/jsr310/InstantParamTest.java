package io.dropwizard.jersey.jsr310;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class InstantParamTest {
    @Test
    void parsesInstants() throws Exception {
        final InstantParam param = new InstantParam("1488751730055");

        assertThat(param.get()).isEqualTo(Instant.ofEpochMilli(1488751730055L));
    }
}
