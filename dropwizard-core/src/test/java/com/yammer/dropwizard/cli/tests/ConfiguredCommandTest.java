package com.yammer.dropwizard.cli.tests;

import org.junit.Assert;

import com.yammer.dropwizard.AbstractService;
import com.yammer.dropwizard.cli.ConfiguredCommand;
import com.yammer.dropwizard.config.Configuration;

import org.apache.commons.cli.CommandLine;
import org.junit.Test;

public class ConfiguredCommandTest {
    public static class MyConfig extends Configuration { }

    static class DirectCommand extends ConfiguredCommand<MyConfig> {
        protected DirectCommand() { super("test", "foobar"); }
        @Override protected void run(AbstractService<MyConfig> service, MyConfig configuration, CommandLine params) { }
        // needed since super-class method is protected
        public Class<?> getParameterization() { return super.getConfigurationClass(); }
    }

    static class UberCommand extends DirectCommand { }
    
    @Test
    public void canResolveParameterization() {
        // first, simple case with direct sub-class parameterization:
        Assert.assertEquals(new DirectCommand().getParameterization(), MyConfig.class);
        // then indirect one
        Assert.assertEquals(new UberCommand().getParameterization(), MyConfig.class);
    }

}
