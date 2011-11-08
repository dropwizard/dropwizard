package com.yammer.flopwizard.example;

import com.google.common.base.Optional;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.ConfigurationException;
import com.yammer.dropwizard.config.Environment;

public class Example extends Service<ExampleConfiguration> {
    public static void main(String[] args) throws Exception {
        new Example().run(args);
    }

    private Example() {
        super(ExampleConfiguration.class, "example", new SayCommand(), new SplodyCommand());
    }

    @Override
    public Optional<String> getBanner() {
        return Optional.of(
                "                                               dP           \n" +
                "                                               88           \n" +
                ".d8888b. dP.  .dP .d8888b. 88d8b.d8b. 88d888b. 88 .d8888b.  \n" +
                "88ooood8  `8bd8'  88'  `88 88'`88'`88 88'  `88 88 88ooood8  \n" +
                "88.  ...  .d88b.  88.  .88 88  88  88 88.  .88 88 88.  ...  \n" +
                "`88888P' dP'  `dP `88888P8 dP  dP  dP 88Y888P' dP `88888P'  \n" +
                "                                      88                    \n" +
                "                                      dP                   "
        );
    }

    @Override
    public void configure(ExampleConfiguration configuration,
                          Environment environment) throws ConfigurationException {
        final String saying = configuration.getSaying();
        environment.addResource(new HelloWorldResource(saying));
        environment.addResource(new UploadResource());
//        environment.addResource(new ProtectedResource());
//        environment.addResource(new SplodyResource());
        environment.addHealthCheck(new DumbHealthCheck());
        environment.manage(new StartableObject());
    }
}
