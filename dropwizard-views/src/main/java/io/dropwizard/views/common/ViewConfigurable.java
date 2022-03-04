package io.dropwizard.views.common;

import java.util.Map;

public interface ViewConfigurable<T> {
    Map<String, Map<String, String>> getViewConfiguration(T configuration);
}
