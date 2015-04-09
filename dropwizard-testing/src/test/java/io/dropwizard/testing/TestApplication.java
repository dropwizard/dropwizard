package io.dropwizard.testing;

import io.dropwizard.Application;
import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.DropwizardTestSupportTest.HelloTask;
import io.dropwizard.testing.DropwizardTestSupportTest.TestConfiguration;
import io.dropwizard.testing.DropwizardTestSupportTest.TestResource;

public class TestApplication extends Application<TestConfiguration> {
    public TestCommand testCommand = new TestCommand();

    @Override
    public void initialize(Bootstrap<TestConfiguration> bootstrap) {
        bootstrap.addCommand(testCommand);
    }

    @Override
    public void run(TestConfiguration configuration, Environment environment) throws Exception {
        environment.jersey().register(new TestResource(configuration.getMessage()));
        environment.admin().addTask(new HelloTask());
    }
}
