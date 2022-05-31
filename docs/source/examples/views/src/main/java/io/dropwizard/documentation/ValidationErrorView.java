package io.dropwizard.documentation;

import io.dropwizard.jersey.validation.ValidationErrorMessage;
import io.dropwizard.views.common.View;

public class ValidationErrorView extends View {
    private final ValidationErrorMessage message;

    public ValidationErrorView(ValidationErrorMessage message) {
        super("validation-error.ftl");
        this.message = message;
    }

    public ValidationErrorMessage getMessage() {
        return message;
    }
}
