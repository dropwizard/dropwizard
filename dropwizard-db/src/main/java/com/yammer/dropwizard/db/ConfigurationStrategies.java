package com.yammer.dropwizard.db;

import com.google.common.collect.ImmutableSet;
import com.yammer.dropwizard.config.Configuration;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Helper functions for working with {@link ConfigurationStrategy} and {@link MultiDbConfigurationStrategy}
 */
public class ConfigurationStrategies {
    public static <T extends Configuration> MultiDbConfigurationStrategy<T> asMultiDbConfigurationStrategy(final ConfigurationStrategy<T> configurationStrategy) {
        return new MultiDbConfigurationStrategy<T>() {
            @Override
            public DatabaseConfiguration getDatabaseConfiguration(String name, T configuration) {
                return getDatabaseConfiguration(configuration);
            }

            @Override
            public Set<String> getDatabaseNames() {
                return Collections.emptySet();
            }

            @Override
            public DatabaseConfiguration getDatabaseConfiguration(T configuration) {
                return configurationStrategy.getDatabaseConfiguration(configuration);
            }

            @Override
            public boolean hasDefault() {
                return true;
            }
        };
    }

    public static <T extends Configuration> MultiDbConfigurationStrategy<T> newMultiDbConfigurationStrategy(final Map<String,ConfigurationStrategy<T>> configurationStrategyMap) {
        return newMultiDbConfigurationStrategy(configurationStrategyMap, null);

    }

    public static <T extends Configuration> MultiDbConfigurationStrategy<T> newMultiDbConfigurationStrategy(final Map<String, ConfigurationStrategy<T>> configurationStrategyMap, final String defaultName) {
        if (defaultName != null && !defaultName.isEmpty() && !configurationStrategyMap.containsKey(defaultName)) {
            throw new IllegalArgumentException(String.format("'%s' specified as default configuration strategy but no such strategy provided.", defaultName));
        }

        return new MultiDbConfigurationStrategy<T>() {
            @Override
            public DatabaseConfiguration getDatabaseConfiguration(String name, T configuration) {
                ConfigurationStrategy<T> strategy = null;
                if (name == null || name.isEmpty()) {
                    if (hasDefault()) {
                        strategy = configurationStrategyMap.get(defaultName);
                    }
                } else {
                    strategy = configurationStrategyMap.get(name);
                }

                return strategy == null ? null : strategy.getDatabaseConfiguration(configuration);
            }

            @Override
            public Set<String> getDatabaseNames() {
                return ImmutableSet.copyOf(configurationStrategyMap.keySet());
            }

            @Override
            public DatabaseConfiguration getDatabaseConfiguration(T configuration) {
                return getDatabaseConfiguration(null, configuration);
            }

            @Override
            public boolean hasDefault() {
                return (defaultName != null && !defaultName.isEmpty());
            }
        };
    }
}
