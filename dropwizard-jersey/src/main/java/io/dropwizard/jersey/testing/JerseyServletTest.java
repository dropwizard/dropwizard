package io.dropwizard.jersey.testing;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jersey.repackaged.com.google.common.base.Joiner;

import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jetty.servlet.JettyWebContainerFactory;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

/**
 * A convenience class that can be sub-classed to run Jersey tests that need a servlet.
 *
 * <p/>
 * This is mostly for internal Dropwizard use to overcome the limitations of all the available
 * jersey test framework connectors which don't expose servlet based containers. Another way 
 * would be to write our own jersey test framework connector but that is more effort. For 
 * testing your own Dropwizard application resources, there is more comprehensive support in the 
 * dropwizard-testing module.
 * 
 * This class overrides the default implementation of the the getTestContainerFactory
 * method to return a Jetty based container.
 *
 */
public abstract class JerseyServletTest extends JerseyTest {
    protected final String resourceConfigClassName;
    protected final List<String> providerClassNames;

    /**
     * Causes the Jetty servlet that will be constructed to load the indicated classname
     * via its nullary constructor as the ResourceConfig of the Jersey test application.
     * The Jersey test application will also register the indicated array of provider
     * classnames as Jersey providers. Note that this facility is not suitable for registering
     * Jersey singletons. If you need those, the ResourceConfig class you're specifying needs
     * to do that in its nullary constructor
     *
     * @param packages array of package names
     */
    public JerseyServletTest(final String resourceConfigClassName,
                            final List<String> providerClassNames) throws TestContainerException {
        this.resourceConfigClassName = resourceConfigClassName;
        this.providerClassNames = providerClassNames;
    }

    @Override
    protected TestContainerFactory getTestContainerFactory()
            throws TestContainerException {
      
        return new TestContainerFactory() {
            @Override
            public TestContainer create(final URI baseUri, final ApplicationHandler application) throws IllegalArgumentException {
                return new TestContainer() {
                    private Server server;

                    @Override
                    public ClientConfig getClientConfig() {
                        return null;
                    }

                    @Override
                    public URI getBaseUri() {
                        return baseUri;
                    }

                    @Override
                    public void start() {
                        try {
                            Map<String,String> propertyMap = new HashMap<String,String>();
                            propertyMap.put(ServletProperties.JAXRS_APPLICATION_CLASS, 
                                            resourceConfigClassName);
                            final String providerClassNamesStr =
                                    Joiner.on(",").join(providerClassNames);
                            if (providerClassNamesStr.length() > 0)
                                propertyMap.put(ServerProperties.PROVIDER_CLASSNAMES, 
                                        providerClassNamesStr);
                            this.server = JettyWebContainerFactory.create(baseUri, propertyMap);
                        } catch (Exception e) {
                            throw new TestContainerException(e);
                        }
                    }

                    @Override
                    public void stop() {
                        try {
                            this.server.stop();
                        } catch (Exception e) {
                            throw new TestContainerException(e);
                        } 
                    }
                };

            }
        };
    }

}
