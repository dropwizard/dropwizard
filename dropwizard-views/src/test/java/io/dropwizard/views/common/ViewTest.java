package io.dropwizard.views.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ViewTest {
    private final View view = new View("/blah.tmp") {};

    @Test
    void hasATemplate() throws Exception {
        assertThat(view.getTemplateName()).isEqualTo("/blah.tmp");
    }
}
