package com.yammer.dropwizard.cli.tests;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.cli.Cli;
import com.yammer.dropwizard.cli.Command;
import com.yammer.dropwizard.cli.ConfiguredCommand;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import org.hibernate.validator.constraints.NotEmpty;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import static org.fest.assertions.api.Assertions.assertThat;

public class ConfiguredCommandTest {
    public static class MyConfig extends Configuration {
        @NotEmpty
        @JsonProperty
        private String type;

        public String getType() {
            return type;
        }
    }

    private static class MyService extends Service<MyConfig> {
        MyService() {
            super("myservice");
        }

        @Override
        protected void initialize(MyConfig configuration, Environment environment) throws Exception {
        }
    }

    @Parameters(commandNames = {"test"}, commandDescription = "foobar")
    private static class DirectCommand extends ConfiguredCommand<MyConfig> {
        private String type;

        @Parameter(names = {"-t", "--tag"}, description = "Arbitrary tag")
        private String tag;

        @SuppressWarnings("FieldMayBeFinal")
        @Parameter(names = { "-v", "--verbose" }, description = "Verbose flag")
        private boolean verbose = false;

        @Override
        protected void run(Service<MyConfig> service,
                           MyConfig configuration) {
            this.type = configuration.getType();
        }

        // needed since super-class method is protected
        public Class<MyConfig> getParameterization() {
            return getConfigurationClass();
        }

        public String getType() {
            return type;
        }

        public String getTag() {
            return tag;
        }

        public boolean isVerbose() {
            return verbose;
        }
    }


    private static class UberCommand extends DirectCommand {
    }

    @Test
    public void canResolveDirectParameterization() {
        assertThat(new DirectCommand().getParameterization())
                .isSameAs(MyConfig.class);
    }

    @Test
    public void canResolveIndirectParameterization() {
        assertThat(new UberCommand().getParameterization())
                .isSameAs(MyConfig.class);
    }

    @Test
    public void parseYmlConfigFile() throws Exception {
        final MyService service = new MyService();
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final String ymlConfigFile = createTemporaryFile(".yml", "type: yml\n");

        final DirectCommand command = new DirectCommand();
        final Cli cli = new Cli(service,
                                ImmutableList.<Command>of(command),
                                new PrintStream(output));
        cli.run(new String[]{ "test", ymlConfigFile });

        assertThat(command.getType())
                .isEqualTo("yml");

        assertThat(command.getTag())
                .isNull();
        assertThat(command.isVerbose())
                .isFalse();
    }

    @Test
    public void parseJsonConfigFile() throws Exception {
        final MyService service = new MyService();
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final String jsonConfigFile = createTemporaryFile(".json", "{\"type\": \"json\"}");

        final DirectCommand command = new DirectCommand();
        final Cli cli = new Cli(service,
                                ImmutableList.<Command>of(command),
                                new PrintStream(output));
        cli.run(new String[]{ "test", jsonConfigFile });

        assertThat(command.getType())
                .isEqualTo("json");
    }

    @Test
    public void argumentsAreParsedAsExpected() throws Exception {
        final MyService service = new MyService();
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final String jsonConfigFile = createTemporaryFile(".json", "{\"type\": \"json2\"}");

        final DirectCommand command = new DirectCommand();
        final Cli cli = new Cli(service,
                                ImmutableList.<Command>of(command),
                                new PrintStream(output));
        cli.run(new String[]{ "test", jsonConfigFile, "-v", "--tag", "tag1" });

        assertThat(command.getType())
                .isEqualTo("json2");
        assertThat(command.getTag())
                .isEqualTo("tag1");
        assertThat(command.isVerbose())
                .isTrue();
    }

    /**
     * @return absolute path to temporary file
     */
    private String createTemporaryFile(String extension, String content) throws IOException {
        final File file = File.createTempFile("dropwizard", extension);
        file.deleteOnExit();

        Files.write(content, file, Charsets.UTF_8);
        return file.getAbsolutePath();
    }

}
