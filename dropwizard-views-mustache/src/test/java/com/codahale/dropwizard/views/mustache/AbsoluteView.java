package com.codahale.dropwizard.views.mustache;

import com.codahale.dropwizard.views.View;

public class AbsoluteView extends View {
    private final String name;

    public AbsoluteView(String name) {
        super("/example.mustache");
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
