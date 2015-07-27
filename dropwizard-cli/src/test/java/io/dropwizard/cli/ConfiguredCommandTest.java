package io.dropwizard.cli;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.ConfigurationFactoryFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import javax.validation.Validator;

import net.sourceforge.argparse4j.inf.Namespace;

import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;


public class ConfiguredCommandTest {
    private static class TestCommand extends ConfiguredCommand<Configuration> {
        protected TestCommand() {
            super("test", "test");
        }

        @Override
        protected void run(Bootstrap<Configuration> bootstrap, Namespace namespace, Configuration configuration) throws Exception {
            
        }        
    }
    
    private static class MyApplication extends Application<Configuration> {
        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
        }
    }
    
    private final MyApplication application = new MyApplication();
    private final TestCommand command = new TestCommand();
    private final Bootstrap<Configuration> bootstrap = new Bootstrap<>(application);
    private final Namespace namespace = mock(Namespace.class);
    
    @SuppressWarnings("unchecked")
    @Test
    public void canUseCustomConfigurationFactory() throws Exception {

        ConfigurationFactory<Configuration> factory = Mockito.mock(ConfigurationFactory.class);
        when(factory.build()).thenReturn(null);
        

        ConfigurationFactoryFactory<Configuration> factoryFactory = Mockito.mock(ConfigurationFactoryFactory.class);
        when(factoryFactory.create(any(Class.class), any(Validator.class), any(ObjectMapper.class), any(String.class))).thenReturn(factory);       
        bootstrap.setConfigurationFactoryFactory(factoryFactory);
        
        command.run(bootstrap, namespace);
        
        Mockito.verify(factoryFactory).create(any(Class.class), any(Validator.class), any(ObjectMapper.class), any(String.class));
        Mockito.verify(factory).build();
    }
}
