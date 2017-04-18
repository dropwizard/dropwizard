package io.dropwizard.views.trimou;

import io.dropwizard.views.View;

public class ComplexView extends View {

    private final String name;

    public ComplexView(String name) {
        super("/complex.trimou");
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
