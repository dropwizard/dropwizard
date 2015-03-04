package io.dropwizard.views;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.Configuration;

public interface ViewConfigurable<T extends Configuration> {
    ImmutableMap<String, ImmutableMap<String, String>> getViewConfiguration(T configuration);
}
