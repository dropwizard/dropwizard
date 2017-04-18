package io.dropwizard.views.trimou;

import io.dropwizard.views.View;

public class HelloView extends View {

    private final String name;

    public HelloView(String name) {
        super("/hello.trimou");
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isValid() {
        return true;
    }
}
