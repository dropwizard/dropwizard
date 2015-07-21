package io.dropwizard.views.mustache;

import io.dropwizard.views.View;

public class ErrorView extends View {
    protected ErrorView() {
        super("/example-error.mustache");
    }
}
