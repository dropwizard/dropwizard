package com.yammer.dropwizard.bundles;

import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.scala.inject.ScalaCollectionsQueryParamInjectableProvider;

public class ScalaBundle extends Bundle {
    @Override
    public void run(Environment environment) {
        environment.addProvider(new ScalaCollectionsQueryParamInjectableProvider());
    }
}
