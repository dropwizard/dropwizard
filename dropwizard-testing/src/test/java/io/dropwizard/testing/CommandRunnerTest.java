package io.dropwizard.testing;

import io.dropwizard.testing.DropwizardTestSupportTest.TestConfiguration;
import org.junit.Test;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.is;

public class CommandRunnerTest {
    @Test
    public void commandIsRun() {
        TestCommand cmd = new TestCommand();
        new CommandRunner<>(TestApplication.class, null, cmd).run();
        assertThat(cmd.output, is("success"));
    }

    @Test
    public void commandIsRunByName() {
        CommandRunner<TestConfiguration> runner = new CommandRunner<>(TestApplication.class, null, "Test");
        runner.run();
        TestApplication application = runner.getApplication();
        assertThat(application.testCommand.output, is("success"));
    }
}
