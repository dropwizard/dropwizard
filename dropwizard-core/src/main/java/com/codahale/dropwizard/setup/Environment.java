package com.codahale.dropwizard.setup;

import com.codahale.dropwizard.lifecycle.setup.LifecycleEnvironment;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.validation.Validator;

import static com.google.common.base.Preconditions.checkNotNull;

// TODO: 5/15/13 <coda> -- add tests for Environment

/**
 * A Dropwizard application's environment.
 */
public class Environment {
    private final String name;
    private final MetricRegistry metricRegistry;
    private final HealthCheckRegistry healthCheckRegistry;

    private final ObjectMapper objectMapper;
    private Validator validator;

    private final LifecycleEnvironment lifecycleEnvironment;

    /**
     * Creates a new environment.
     *
     * @param name                the name of the application
     * @param objectMapper the {@link ObjectMapper} for the application
     */
    public Environment(String name,
                       ObjectMapper objectMapper,
                       Validator validator,
                       MetricRegistry metricRegistry,
                       ClassLoader classLoader) {
        this.name = name;
        this.objectMapper = objectMapper;
        this.metricRegistry = metricRegistry;
        this.healthCheckRegistry = new HealthCheckRegistry();
        this.validator = validator;

        this.lifecycleEnvironment = new LifecycleEnvironment();
    }


    public LifecycleEnvironment lifecycle() {
        return lifecycleEnvironment;
    }
    
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public String getName() {
        return name;
    }

    public Validator getValidator() {
        return validator;
    }

    public void setValidator(Validator validator) {
        this.validator = checkNotNull(validator);
    }

    public MetricRegistry metrics() {
        return metricRegistry;
    }

    public HealthCheckRegistry healthChecks() {
        return healthCheckRegistry;
    }
}
