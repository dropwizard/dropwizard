package io.dropwizard.documentation;

import io.dropwizard.jersey.errors.ErrorMessage;
import io.dropwizard.views.common.View;

public class ErrorView extends View {
    private final ErrorMessage errorMessage;

    public ErrorView(ErrorMessage errorMessage) {
        super("error.ftl");
        this.errorMessage = errorMessage;
    }

    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }
}
