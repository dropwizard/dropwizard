package com.yammer.dropwizard.bundles;

import com.codahale.jersey.inject.ScalaCollectionsQueryParamInjectableProvider;
import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.ScalaService;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.jersey.JacksonMessageBodyProvider;

public class ScalaBundle implements Bundle {
    private final ScalaService<?> service;

    public ScalaBundle(ScalaService<?> service) {
        this.service = service;
    }

    @Override
    public void initialize(Environment environment) {
        environment.addProvider(new JacksonMessageBodyProvider(service.getJson()));
        environment.addProvider(new ScalaCollectionsQueryParamInjectableProvider());
    }
}
