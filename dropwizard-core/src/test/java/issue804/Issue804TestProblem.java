package issue804;

import io.dropwizard.setup.Bootstrap;
import org.junit.Test;

public class Issue804TestProblem {
    @Test
    public void testOnProvidedExtendedConfigurationClassShouldReturnExtendedClass() throws Exception {
        BaseApplication<SampleConfiguration> application = new SampleApplication();

        application.initialize(new Bootstrap<>(application));
    }
}
