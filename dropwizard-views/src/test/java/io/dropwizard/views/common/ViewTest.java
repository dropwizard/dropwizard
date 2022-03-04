package io.dropwizard.views.common;

import io.dropwizard.views.common.View;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ViewTest {
    private final View view = new View("/blah.tmp") {
    };

    @Test
    void hasATemplate() throws Exception {
        assertThat(view.getTemplateName())
                .isEqualTo("/blah.tmp");
    }
}
