package com.yammer.dropwizard.cli.tests;

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
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

@Ignore("DW lifecycle doesn't play well with Surefire")
public class ConfiguredCommandTest {

    public static class MyConfig extends Configuration {

        @NotEmpty
        private String type;

        public String getType() {
            return type;
        }
    }

    static class MyService extends Service<MyConfig> {
        public MyService() {
            super("myservice");
        }

        @Override
        protected void initialize(MyConfig configuration, Environment environment) throws Exception {
        }
    }

    static class DirectCommand extends ConfiguredCommand<MyConfig> {
        private String type, tag;
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
        public Class<?> getParameterization() {
            return super.getConfigurationClass();
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


    static class UberCommand extends DirectCommand {
    }

    @Test
    public void canResolveDirectParameterization() {
        assertEquals(new DirectCommand().getParameterization(), MyConfig.class);
    }

    @Test
    public void canResolveIndirectParameterization() {
        assertEquals(new UberCommand().getParameterization(), MyConfig.class);
    }

    @Test
    public void parseYmlConfigFile() throws Exception {
        String ymlConfigFile = createTemporaryFile(".yml", "type: yml\n");

        DirectCommand command = new DirectCommand();
        command.run(new MyService(), new String[]{ymlConfigFile});

        assertThat(command.getType(), is("yml"));
        assertNull(command.getTag());
        assertFalse(command.isVerbose());
    }

    @Test
    public void parseJsonConfigFile() throws Exception {
        String jsonConfigFile = createTemporaryFile(".json", "{\"type\": \"json\"}");

        DirectCommand command = new DirectCommand();
        command.run(new MyService(), new String[]{jsonConfigFile});

        assertThat(command.getType(), is("json"));
    }

    @Test
    public void argumentsAreParsedAsExpected() throws Exception {
        String jsonConfigFile = createTemporaryFile(".json", "{\"type\": \"json2\"}");

        DirectCommand command = new DirectCommand();
        command.run(new MyService(), new String[]{jsonConfigFile, "-v", "--tag", "tag1"});

        assertThat(command.getType(), is("json2"));
        assertThat(command.getTag(), is("tag1"));
        assertTrue(command.isVerbose());
    }

    /**
     * @return absolute path to temporary file
     */
    private String createTemporaryFile(String extension, String content) throws IOException {
        File file = File.createTempFile("dropwizard", extension);
        file.deleteOnExit();

        Files.write(content, file, Charsets.UTF_8);
        return file.getAbsolutePath();
    }

}
