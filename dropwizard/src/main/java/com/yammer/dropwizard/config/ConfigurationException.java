package com.yammer.dropwizard.config;

import java.io.File;

public class ConfigurationException extends Exception {
    public ConfigurationException(File file, Iterable<String> errors) {
        super(formatMessage(file, errors));
    }

    public ConfigurationException(Exception e) {
        super(e);
    }

    private static String formatMessage(File file, Iterable<String> errors) {
        final StringBuilder msg = new StringBuilder(file.toString())
                .append(" has the following errors:\n");
        for (String error : errors) {
            msg.append("  * ").append(error);
        }
        return msg.toString();
    }
}
