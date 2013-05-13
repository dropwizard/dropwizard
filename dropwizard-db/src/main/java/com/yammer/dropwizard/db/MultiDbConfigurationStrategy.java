package com.yammer.dropwizard.db;

import com.yammer.dropwizard.config.Configuration;

import java.util.Set;

/**
 * Allows configuration of objects that can make use of multiple databases at once.
 */
public interface MultiDbConfigurationStrategy<T extends Configuration> extends ConfigurationStrategy<T>{
    DatabaseConfiguration getDatabaseConfiguration(String name, T configuration);
    Set<String> getDatabaseNames();
    boolean hasDefault();
}
