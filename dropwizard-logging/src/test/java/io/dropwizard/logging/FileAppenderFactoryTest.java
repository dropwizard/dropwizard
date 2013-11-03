package io.dropwizard.logging;

import java.lang.reflect.Method;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.rolling.RollingFileAppender;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class FileAppenderFactoryTest {
    @Test
    public void isDiscoverable() throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(FileAppenderFactory.class);
    }

    @Test
    public void isRolling() throws Exception {
        FileAppenderFactory fileAppenderFactory = new FileAppenderFactory ();
        fileAppenderFactory.setCurrentLogFilename("logfile.log");
        fileAppenderFactory.setArchive(true);
        fileAppenderFactory.setArchivedLogFilenamePattern("example-%d.log.gz");
        Method method = FileAppenderFactory.class.getDeclaredMethod("buildAppender", LoggerContext.class);
        method.setAccessible (true);
        assertThat (RollingFileAppender.class.isAssignableFrom (method.invoke (fileAppenderFactory, new LoggerContext ()).getClass ()));
    }
}
