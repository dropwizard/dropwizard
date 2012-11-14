package com.yammer.dropwizard.bundles;

import com.fasterxml.jackson.module.scala.DefaultScalaModule;
import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.scala.inject.ScalaCollectionsQueryParamInjectableProvider;

public class ScalaBundle implements Bundle {
    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        bootstrap.getObjectMapperFactory().registerModule(new DefaultScalaModule());
    }

    @Override
    public void run(Environment environment) {
        environment.addProvider(new ScalaCollectionsQueryParamInjectableProvider());
    }
}
