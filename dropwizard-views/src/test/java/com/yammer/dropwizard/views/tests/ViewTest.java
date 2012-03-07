package com.yammer.dropwizard.views.tests;

import com.yammer.dropwizard.views.MyView;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ViewTest {
    private final MyView view = new MyView("Wonk");

    @Test
    public void hasATemplate() throws Exception {
        assertThat(view.getTemplateName(),
                   is("/example.ftl"));
    }
}
