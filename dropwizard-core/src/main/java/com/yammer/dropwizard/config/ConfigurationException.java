package com.yammer.dropwizard.config;

import javax.validation.ConstraintViolation;
import java.io.File;
import java.util.Set;

public class ConfigurationException extends Exception {
    public <T> ConfigurationException(File file, Set<ConstraintViolation<T>> violations) {
        super(formatMessage(file, violations));
    }

    private static <T> String formatMessage(File file, Set<ConstraintViolation<T>> violations) {
        final StringBuilder msg = new StringBuilder(file.toString()).append(
                " has the following errors:\n");
        for (ConstraintViolation<?> v : violations) {
            msg.append("  * ").append(v.getPropertyPath()).append(" ").append(v.getMessage());
        }
        return msg.toString();
    }
}
