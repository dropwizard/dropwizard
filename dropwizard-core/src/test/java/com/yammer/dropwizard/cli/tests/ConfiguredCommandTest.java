package com.yammer.dropwizard.cli.tests;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.yammer.dropwizard.AbstractService;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.cli.ConfiguredCommand;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.hibernate.validator.constraints.NotEmpty;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

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

    private static class DirectCommand extends ConfiguredCommand<MyConfig> {
        private String type;
        private String tag;
        private boolean verbose = false;

        protected DirectCommand() {
            super("test", "foobar");
        }

        @Override
        public Options getOptions() {
            return super.getOptions()
                        .addOption("t", "tag", true, "Arbitrary tag")
                        .addOption("v", "verbose", false, "Verbose flag");
        }

        @Override
        protected void run(AbstractService<MyConfig> service,
                           MyConfig configuration, CommandLine params) {
            this.type = configuration.getType();
            this.tag = params.getOptionValue("tag");
            this.verbose = params.hasOption("verbose");
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
        final String ymlConfigFile = createTemporaryFile(".yml", "type: yml\n");

        final DirectCommand command = new DirectCommand();
        command.run(new MyService(), new String[]{ ymlConfigFile });

        assertThat(command.getType())
                .isEqualTo("yml");

        assertThat(command.getTag())
                .isNull();
        assertThat(command.isVerbose())
                .isFalse();
    }

    @Test
    public void parseJsonConfigFile() throws Exception {
        final String jsonConfigFile = createTemporaryFile(".json", "{\"type\": \"json\"}");

        final DirectCommand command = new DirectCommand();
        command.run(new MyService(), new String[]{ jsonConfigFile });

        assertThat(command.getType())
                .isEqualTo("json");
    }

    @Test
    public void argumentsAreParsedAsExpected() throws Exception {
        final String jsonConfigFile = createTemporaryFile(".json", "{\"type\": \"json2\"}");

        final DirectCommand command = new DirectCommand();
        command.run(new MyService(), new String[]{ jsonConfigFile, "-v", "--tag", "tag1" });

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
