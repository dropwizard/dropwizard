package io.dropwizard.views.freemarker;

import io.dropwizard.views.common.View;

public class ErrorView extends View {
    protected ErrorView() {
        super("/example-error.ftlx");
    }
}
