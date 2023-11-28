package io.dropwizard.testing;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import io.dropwizard.core.Configuration;

import java.util.ArrayList;
import java.util.List;

public class DropwizardTestSupportUtils<C extends Configuration> {
    protected static List<DropwizardTestSupport.ServiceListener<C>> listeners = new ArrayList<>();

    @Nullable
    protected C configuration;
    static void stopIfRequired(DropwizardTestSupport<?> support) {
        if (support.jettyServer != null) {
            for (DropwizardTestSupport.ServiceListener<C> listener : listeners) {
                try {
                    listener.onStop(this);
                } catch (Exception ignored) {
                }
            }
            try {
                support.jettyServer.stop();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                support.jettyServer = null;
            }
        }

        // Don't leak logging appenders into other test cases
        if (configuration != null) {
            configuration.getLoggingFactory().reset();
        } else {
            LoggingUtil.getLoggerContext().getLogger(Logger.ROOT_LOGGER_NAME).detachAndStopAllAppenders();
        }
    }
}
