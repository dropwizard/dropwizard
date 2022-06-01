package io.dropwizard.jersey.jsr310;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class ZoneIdParamTest {
    @Test
    void parsesDateTimes() throws Exception {
        final ZoneIdParam param = new ZoneIdParam("Europe/Berlin");

        assertThat(param.get()).isEqualTo(ZoneId.of("Europe/Berlin"));
    }
}
