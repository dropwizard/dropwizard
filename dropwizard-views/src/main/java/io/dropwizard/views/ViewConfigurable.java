package io.dropwizard.views;

import io.dropwizard.Configuration;

import java.util.Map;

public interface ViewConfigurable<T extends Configuration> {
    Map<String, Map<String, String>> getViewConfiguration(T configuration);
}
