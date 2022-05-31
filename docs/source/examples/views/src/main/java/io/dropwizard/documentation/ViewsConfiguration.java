package io.dropwizard.documentation;

import io.dropwizard.core.Configuration;

import java.util.Collections;
import java.util.Map;

public class ViewsConfiguration extends Configuration {
    public Map<String, Map<String, String>> getViewRendererConfiguration() {
        return Collections.emptyMap();
    }
}
