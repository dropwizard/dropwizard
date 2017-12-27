package io.dropwizard.cli;

import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.cli.EnvironmentCommand;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;

public class ConsoleCommand<C extends Configuration> extends EnvironmentCommand<C> {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleCommand.class);

    private final Application<C> application;
    private final Class<C> configurationClass;
    protected ConsoleCommand(Application<C> application) {
        super(application, "console", "Runs a console application");
        this.application = application;
        this.configurationClass  = application.getConfigurationClass();
    }

    @Override
    protected Class<C> getConfigurationClass() {
        return configurationClass;
    }

    @Override
    protected void run(Environment environment, Namespace namespace, C configuration) throws Exception {
        for (LifeCycle lifeCycle : environment.lifecycle().getManagedObjects()) {
            try {
              lifeCycle.start();
            } catch(Exception e) {
                logger.error("start failed: " + lifeCycle, e);
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                for (LifeCycle lifeCycle : environment.lifecycle().getManagedObjects()) {
                    if (lifeCycle.isRunning()) {
                        try {
                          lifeCycle.stop();
                        } catch(Exception e) {
                            logger.error("stop failed: " + lifeCycle, e);
                        }
                    }
                }
            }
        });

        while(true) {
            for (LifeCycle lifeCycle : environment.lifecycle().getManagedObjects()) {
                if (lifeCycle.isRunning()) {
                    logger.info("lifecycle is still running");
                    Thread.sleep(100);
                    continue;
                }
            }
            break;
        }
    }
}

