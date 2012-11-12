package com.yammer.dropwizard.views.tests;

import com.yammer.dropwizard.views.MyView;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ViewTest {
    private final MyView view = new MyView("Wonk");

    @Test
    public void hasATemplate() throws Exception {
        assertThat(view.getTemplateName())
                .isEqualTo("/example.ftl");
    }
}
