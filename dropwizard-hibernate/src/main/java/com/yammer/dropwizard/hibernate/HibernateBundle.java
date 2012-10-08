package com.yammer.dropwizard.hibernate;

import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.config.Bootstrap;

public class HibernateBundle extends Bundle {
    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        final Hibernate4Module module = new Hibernate4Module();
        module.enable(Hibernate4Module.Feature.FORCE_LAZY_LOADING);
        bootstrap.getObjectMapperFactory().registerModule(module);
    }
}
