package io.dropwizard.views.freemarker;

import io.dropwizard.views.common.View;

public class AbsoluteView extends View {
    private final String name;

    public AbsoluteView(String name) {
        super("/example.ftlh");
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
