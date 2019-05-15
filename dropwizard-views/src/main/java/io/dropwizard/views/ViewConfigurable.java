package io.dropwizard.views;

import java.util.Map;

public interface ViewConfigurable<T> {
    Map<String, Map<String, String>> getViewConfiguration(T configuration);
}
