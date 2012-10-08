package com.yammer.dropwizard.migrations;

import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.db.DatabaseConfiguration;

public interface ConfigurationStrategy<T extends Configuration> {
    DatabaseConfiguration getDatabaseConfiguration(T configuration);
}
