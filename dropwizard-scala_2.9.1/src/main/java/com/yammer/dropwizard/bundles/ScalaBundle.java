package com.yammer.dropwizard.bundles;

import com.codahale.jersey.inject.ScalaCollectionsQueryParamInjectableProvider;
import com.codahale.jersey.providers.JerksonProvider;
import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.providers.OauthTokenProvider;

public class ScalaBundle implements Bundle {
    @Override
    public void initialize(Environment environment) {
        environment.addProvider(new JerksonProvider<Object>());
        environment.addProvider(new OauthTokenProvider());
        environment.addProvider(new ScalaCollectionsQueryParamInjectableProvider());
    }
}
