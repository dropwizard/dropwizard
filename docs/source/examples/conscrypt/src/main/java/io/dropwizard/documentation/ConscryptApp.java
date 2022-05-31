package io.dropwizard.documentation;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Environment;
import org.conscrypt.OpenSSLProvider;

import java.security.Security;

public class ConscryptApp extends Application<Configuration> {
    // conscrypt: ConscryptApp->OpenSSLProvider
    static {
        Security.insertProviderAt(new OpenSSLProvider(), 1);
    }
    // conscrypt: ConscryptApp->OpenSSLProvider

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
    }
}
