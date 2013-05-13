package com.yammer.dropwizard.migrations;

import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.db.MultiDbConfigurationStrategy;
import com.yammer.dropwizard.util.Generics;

/**
 * Liquibase Migrations bundle that supports run-time selection from amongst
 * multiple configured databases.
 *
 * Example usage:
 * <pre>
 * &#64;Override
 * public void initialize(Bootstrap&lt;MyConfiguration&gt; bootstrap)
 * {
 *     bootstrap.addBundle(new MultiDbMigrationsBundle&lt;MyConfiguration&gt;(
 *               ConfigurationStrategies.newMultiDbConfigurationStrategy(
 *                       ImmutableMap.&lt;String,ConfigurationStrategy&lt;MyConfiguration&gt;&gt; builder()
 *                               .put("this_db", new ConfigurationStrategy&lt;MyConfiguration&gt;() {
 *                                   &#64;Override
 *                                   public DatabaseConfiguration getDatabaseConfiguration (MyConfiguration configuration) {
 *                                       return configuration.getThisConfiguration().getDatabaseConfiguration();
 *                                   }
 *                               })
 *                               .put("that_db", new ConfigurationStrategy&lt;MyConfiguration&gt;() {
 *                                   &#64;Override
 *                                   public DatabaseConfiguration getDatabaseConfiguration (MyConfiguration configuration) {
 *                                       return configuration.getThatConfiguration().getDatabaseConfiguration();
 *                                   }
 *                               })
 *                               .build()
 *              )
 *     ){});
 * }
 * </pre>
 *
 * Note: MultiDbMigrationsBundle is intentionally declared abstract, which forces a class body,
 * as shown above. This is needed for the time being because Generics.getTypeParameter will
 * otherwise fail to determine the type bound of the MultiDbMigrationsBundle instance.
 *
 * @param <T>
 */
public abstract class MultiDbMigrationsBundle<T extends Configuration> implements Bundle {
    private final MultiDbConfigurationStrategy configurationStrategy;

    public MultiDbMigrationsBundle(MultiDbConfigurationStrategy configurationStrategy) {
        this.configurationStrategy = configurationStrategy;
    }

    @Override
    public final void initialize(Bootstrap<?> bootstrap) {
        final Class<T> klass = Generics.getTypeParameter(getClass(), Configuration.class);
        bootstrap.addCommand(new DbCommand<T>(configurationStrategy, klass));
    }

    @Override
    public final void run(Environment environment) {
        // nothing doing
    }
}

