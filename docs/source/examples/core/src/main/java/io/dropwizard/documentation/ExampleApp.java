package io.dropwizard.documentation;

import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.documentation.config.ExampleConfiguration;
import io.dropwizard.documentation.mq.MessageQueueClient;

public class ExampleApp extends Application<ExampleConfiguration> {
    @Override
    // core: ExampleApp#initialize
    public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
        // Enable variable substitution with environment variables
        EnvironmentVariableSubstitutor substitutor = new EnvironmentVariableSubstitutor(false);
        SubstitutingSourceProvider provider =
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(), substitutor);
        bootstrap.setConfigurationSourceProvider(provider);
    }
    // core: ExampleApp#initialize

    @Override
    // core: ExampleApp#run
    public void run(ExampleConfiguration configuration, Environment environment) {
        MessageQueueClient messageQueue = configuration.getMessageQueueFactory().build(environment);
    }
    // core: ExampleApp#run
}
