package io.dropwizard.validation;

import javax.inject.Inject;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.util.function.Consumer;

public class GetResourceContextFeature implements Feature {

    private final Consumer<ResourceContext> action;

    @Inject
    private ResourceContext resourceContext;

    public GetResourceContextFeature(Consumer<ResourceContext> action) {
        this.action = action;
    }

    @Override
    public boolean configure(FeatureContext context) {
        action.accept(resourceContext);
        return true;
    }
}
