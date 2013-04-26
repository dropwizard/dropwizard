package com.codahale.dropwizard.views;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ViewTest {
    private final View view = new View("/blah.tmp") {};

    @Test
    public void hasATemplate() throws Exception {
        assertThat(view.getTemplateName())
                .isEqualTo("/blah.tmp");
    }
}
