package io.dropwizard.jersey.jsr310;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class InstantSecondParamTest {
    @Test
    void parsesInstants() throws Exception {
        final InstantSecondParam param = new InstantSecondParam("1488752017");

        assertThat(param.get())
                .isEqualTo(Instant.ofEpochSecond(1488752017L));
    }
}
