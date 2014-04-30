package io.dropwizard.client;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.config.ClientConfig;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import io.dropwizard.setup.Environment;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.validation.Validation;
import javax.validation.Validator;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.SchemeRegistry;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.GZIPContentEncodingFilter;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.ApacheHttpClient4Handler;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;

/**
 * A convenience class for building {@link Client} instances.
 * <p/>
 * Among other things,
 *
 * <ul>
 *     <li>Backed by Apache HttpClient</li>
 *     <li>Disables stale connection checks</li>
 *     <li>Disables Nagle's algorithm</li>
 *     <li>Disables cookie management by default</li>
 * </ul>
 *
 * @see HttpClientBuilder
 */
public class JerseyClientBuilder {
    private final HttpClientBuilder builder;
    private final List<Object> singletons = Lists.newArrayList();
    private final List<Class<?>> providers = Lists.newArrayList();
    private final Map<String, Boolean> features = Maps.newLinkedHashMap();
    private final Map<String, Object> properties = Maps.newLinkedHashMap();

    private JerseyClientConfiguration configuration = new JerseyClientConfiguration();
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private Environment environment;
    private ObjectMapper objectMapper;
    private ExecutorService executorService;

    public JerseyClientBuilder(Environment environment) {
        this.builder = new HttpClientBuilder(environment);
        this.environment = environment;
    }

    public JerseyClientBuilder(MetricRegistry metricRegistry) {
        this.builder = new HttpClientBuilder(metricRegistry);
    }

    /**
     * Adds the given object as a Jersey provider.
     *
     * @param provider a Jersey provider
     * @return {@code this}
     */
    public JerseyClientBuilder withProvider(Object provider) {
        singletons.add(checkNotNull(provider));
        return this;
    }

    /**
     * Adds the given class as a Jersey provider. <p/><b>N.B.:</b> This class must either have a
     * no-args constructor or use Jersey's built-in dependency injection.
     *
     * @param klass a Jersey provider class
     * @return {@code this}
     */
    public JerseyClientBuilder withProvider(Class<?> klass) {
        providers.add(checkNotNull(klass));
        return this;
    }

    /**
     * Sets the state of the given Jersey feature.
     *
     * @param featureName  the name of the Jersey feature
     * @param featureState the state of the Jersey feature
     * @return {@code this}
     */
    @SuppressWarnings("UnusedDeclaration") // basically impossible to test
    public JerseyClientBuilder withFeature(String featureName, boolean featureState) {
        features.put(featureName, featureState);
        return this;
    }

    /**
     * Sets the state of the given Jersey property.
     *
     * @param propertyName  the name of the Jersey property
     * @param propertyValue the state of the Jersey property
     * @return {@code this}
     */
    public JerseyClientBuilder withProperty(String propertyName, Object propertyValue) {
        properties.put(propertyName, propertyValue);
        return this;
    }
    
    /**
     * Uses the {@link HttpRequestRetryHandler} for handling request retries.
     *
     * @param httpRequestRetryHandler an httpRequestRetryHandler
     * @return {@code this}
     */
    public JerseyClientBuilder using(HttpRequestRetryHandler httpRequestRetryHandler) {
        builder.using(httpRequestRetryHandler);
        return this;
    }

    /**
     * Uses the given {@link JerseyClientConfiguration}.
     *
     * @param configuration a configuration object
     * @return {@code this}
     */
    public JerseyClientBuilder using(JerseyClientConfiguration configuration) {
        this.configuration = configuration;
        builder.using(configuration);
        return this;
    }

    /**
     * Uses the given {@link Environment}.
     *
     * @param environment a Dropwizard {@link Environment}
     * @return {@code this}
     * @see #using(java.util.concurrent.ExecutorService, com.fasterxml.jackson.databind.ObjectMapper)
     */
    public JerseyClientBuilder using(Environment environment) {
        this.environment = environment;
        return this;
    }

    /**
     * Use the given {@link DnsResolver} instance.
     *
     * @param resolver a {@link DnsResolver} instance
     * @return {@code this}
     */
    public JerseyClientBuilder using(DnsResolver resolver) {
        builder.using(resolver);
        return this;
    }

    /**
     * Use the given {@link SchemeRegistry} instance.
     *
     * @param registry a {@link SchemeRegistry} instance
     * @return {@code this}
     */
    public JerseyClientBuilder using(SchemeRegistry registry) {
        builder.using(registry);
        return this;
    }

    /**
     * Use the given {@link Validator} instance.
     *
     * @param validator a {@link Validator} instance
     * @return {@code this}
     */
    public JerseyClientBuilder using(Validator validator) {
        this.validator = validator;
        return this;
    }

    /**
     * Uses the given {@link ExecutorService} and {@link ObjectMapper}.
     *
     * @param executorService a thread pool
     * @param objectMapper    an object mapper
     * @return {@code this}
     * @see #using(io.dropwizard.setup.Environment)
     */
    public JerseyClientBuilder using(ExecutorService executorService, ObjectMapper objectMapper) {
        this.executorService = executorService;
        this.objectMapper = objectMapper;
        return this;
    }

    /**
     * Builds the {@link Client} instance.
     *
     * @return a fully-configured {@link Client}
     */
    public Client build(String name) {
        if ((environment == null) && (executorService == null) && (objectMapper == null)) {
            throw new IllegalStateException("Must have either an environment or both " +
                                                    "an executor service and an object mapper");
        }

        if (environment == null) {
            return build(executorService, objectMapper, validator, name);
        }

        return build(environment.lifecycle()
                                .executorService("jersey-client-" + name + "-%d")
                                .minThreads(configuration.getMinThreads())
                                .maxThreads(configuration.getMaxThreads())
                                .build(),
                     environment.getObjectMapper(),
                     environment.getValidator(),
                     name);
    }

    private Client build(ExecutorService threadPool,
                         ObjectMapper objectMapper,
                         Validator validator,
                         String name) {
        final Client client = new ApacheHttpClient4(buildHandler(name), buildConfig(objectMapper));
        client.setExecutorService(threadPool);

        if (configuration.isGzipEnabled()) {
            client.addFilter(new GZIPContentEncodingFilter(configuration.isGzipEnabledForRequests()));
        }

        return client;
    }

    //CHANGED
    private ApacheHttpClient4Handler buildHandler(String name) {
//        return new ApacheHttpClient4Handler(builder.build(name), null, true);
        return createDefaultClientHandler(buildConfig(objectMapper), name);
    }

    private ApacheHttpClient4Config buildConfig(ObjectMapper objectMapper) {
        final ApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
        config.getSingletons().addAll(singletons);
        config.getSingletons().add(new JacksonMessageBodyProvider(objectMapper, validator));
        config.getClasses().addAll(providers);
        config.getFeatures().putAll(features);
        config.getProperties().putAll(properties);
        return config;
    }

    //ADDED (from ApacheHttpClient4)
    private ApacheHttpClient4Handler createDefaultClientHandler(ClientConfig cc, String name) {
        //these warnings are changed slightly from the original; we warn on any value instead of checking class
        if(cc != null) {
            Object connectionManager = cc.getProperties().get(ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER);
            if(connectionManager != null) {
                Logger.getLogger(ApacheHttpClient4.class.getName()).log(
                        Level.WARNING,
                        "Ignoring value of property " + ApacheHttpClient4Config.PROPERTY_CONNECTION_MANAGER +
                                " (" + connectionManager.getClass().getName() +
                                ") - client only uses InstrumentedConnectionManager."
                );
            }

            Object httpParams = cc.getProperties().get(ApacheHttpClient4Config.PROPERTY_HTTP_PARAMS);
            if(httpParams != null) {
                Logger.getLogger(ApacheHttpClient4.class.getName()).log(
                        Level.WARNING,
                        "Ignoring value of property " + ApacheHttpClient4Config.PROPERTY_HTTP_PARAMS +
                                " (" + httpParams.getClass().getName() +
                                ") - params are built by HttpClientBuilder from client config."
                );
            }
        }

        //TODO: may want to allow builder to take a different InstrumentedConnectionManager or custom HttpParams
        final DefaultHttpClient client = (DefaultHttpClient)builder.build(name);

        CookieStore cookieStore = null;
        boolean preemptiveBasicAuth = false;

        if(cc != null) {
            for(Map.Entry<String, Object> entry : cc.getProperties().entrySet())
                client.getParams().setParameter(entry.getKey(), entry.getValue());

            if (cc.getPropertyAsFeature(ApacheHttpClient4Config.PROPERTY_DISABLE_COOKIES))
                client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES);

            Object credentialsProvider = cc.getProperty(ApacheHttpClient4Config.PROPERTY_CREDENTIALS_PROVIDER);
            if(credentialsProvider != null && (credentialsProvider instanceof CredentialsProvider)) {
                client.setCredentialsProvider((CredentialsProvider)credentialsProvider);
            }

            final Object proxyUri = cc.getProperties().get(ApacheHttpClient4Config.PROPERTY_PROXY_URI);
            if(proxyUri != null) {
                final URI u = getProxyUri(proxyUri);
                final HttpHost proxy = new HttpHost(u.getHost(), u.getPort(), u.getScheme());

                if(cc.getProperties().containsKey(ApacheHttpClient4Config.PROPERTY_PROXY_USERNAME) &&
                        cc.getProperties().containsKey(ApacheHttpClient4Config.PROPERTY_PROXY_PASSWORD)) {

                    client.getCredentialsProvider().setCredentials(
                            new AuthScope(u.getHost(), u.getPort()),
                            new UsernamePasswordCredentials(
                                    cc.getProperty(ApacheHttpClient4Config.PROPERTY_PROXY_USERNAME).toString(),
                                    cc.getProperty(ApacheHttpClient4Config.PROPERTY_PROXY_PASSWORD).toString())
                    );

                }
                client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            }

            preemptiveBasicAuth = cc.getPropertyAsFeature(ApacheHttpClient4Config.PROPERTY_PREEMPTIVE_BASIC_AUTHENTICATION);
        }

        if(client.getParams().getParameter(ClientPNames.COOKIE_POLICY) == null || !client.getParams().getParameter(ClientPNames.COOKIE_POLICY).equals(CookiePolicy.IGNORE_COOKIES)) {
            cookieStore = new BasicCookieStore();
            client.setCookieStore(cookieStore);
        }

        return new ApacheHttpClient4Handler(client, cookieStore, preemptiveBasicAuth);
    }

    //ADDED (from ApacheHttpClient4)
    private static URI getProxyUri(final Object proxy) {
        if (proxy instanceof URI) {
            return (URI) proxy;
        } else if (proxy instanceof String) {
            return URI.create((String) proxy);
        } else {
            throw new ClientHandlerException("The proxy URI (" + ApacheHttpClient4Config.PROPERTY_PROXY_URI +
                    ") property MUST be an instance of String or URI");
        }
    }
}
