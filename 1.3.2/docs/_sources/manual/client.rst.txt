.. _man-client:

#################
Dropwizard Client
#################

.. highlight:: text

.. rubric:: The ``dropwizard-client`` module provides you with two different performant,
            instrumented HTTP clients so you can integrate your service with other web
            services: :ref:`man-client-apache` and :ref:`man-client-jersey`.

.. _man-client-apache:

Apache HttpClient
=================

The underlying library for ``dropwizard-client`` is  Apache's HttpClient_, a full-featured,
well-tested HTTP client library.

.. _HttpClient: http://hc.apache.org/httpcomponents-core-4.3.x/index.html

To create a :ref:`managed <man-core-managed>`, instrumented ``HttpClient`` instance, your
:ref:`configuration class <man-core-configuration>` needs an :ref:`http client configuration <man-configuration-clients-http>` instance:

.. code-block:: java

    public class ExampleConfiguration extends Configuration {
        @Valid
        @NotNull
        private HttpClientConfiguration httpClient = new HttpClientConfiguration();

        @JsonProperty("httpClient")
        public HttpClientConfiguration getHttpClientConfiguration() {
            return httpClient;
        }

        @JsonProperty("httpClient")
        public void setHttpClientConfiguration(HttpClientConfiguration httpClient) {
            this.httpClient = httpClient;
        }
    }

Then, in your application's ``run`` method, create a new ``HttpClientBuilder``:

.. code-block:: java

    @Override
    public void run(ExampleConfiguration config,
                    Environment environment) {
        final HttpClient httpClient = new HttpClientBuilder(environment).using(config.getHttpClientConfiguration())
                                                                        .build(getName());
        environment.jersey().register(new ExternalServiceResource(httpClient));
    }

.. _man-client-apache-metrics:

Metrics
-------

Dropwizard's ``HttpClientBuilder`` actually gives you an instrumented subclass which tracks the
following pieces of data:

``org.apache.http.conn.ClientConnectionManager.available-connections``
    The number the number idle connections ready to be used to execute requests.

``org.apache.http.conn.ClientConnectionManager.leased-connections``
    The number of persistent connections currently being used to execute requests.

``org.apache.http.conn.ClientConnectionManager.max-connections``
    The maximum number of allowed connections.

``org.apache.http.conn.ClientConnectionManager.pending-connections``
    The number of connection requests being blocked awaiting a free connection

``org.apache.http.client.HttpClient.get-requests``
    The rate at which ``GET`` requests are being sent.

``org.apache.http.client.HttpClient.post-requests``
    The rate at which ``POST`` requests are being sent.

``org.apache.http.client.HttpClient.head-requests``
    The rate at which ``HEAD`` requests are being sent.

``org.apache.http.client.HttpClient.put-requests``
    The rate at which ``PUT`` requests are being sent.

``org.apache.http.client.HttpClient.delete-requests``
    The rate at which ``DELETE`` requests are being sent.

``org.apache.http.client.HttpClient.options-requests``
    The rate at which ``OPTIONS`` requests are being sent.

``org.apache.http.client.HttpClient.trace-requests``
    The rate at which ``TRACE`` requests are being sent.

``org.apache.http.client.HttpClient.connect-requests``
    The rate at which ``CONNECT`` requests are being sent.

``org.apache.http.client.HttpClient.move-requests``
    The rate at which ``MOVE`` requests are being sent.

``org.apache.http.client.HttpClient.patch-requests``
    The rate at which ``PATCH`` requests are being sent.

``org.apache.http.client.HttpClient.other-requests``
    The rate at which requests with none of the above methods are being sent.

.. note::

    The naming strategy for the metrics associated requests is configurable.
    Specifically, the last part e.g. get-requests.
    What is displayed is ``HttpClientMetricNameStrategies.METHOD_ONLY``, you can
    also include the host via ``HttpClientMetricNameStrategies.HOST_AND_METHOD``
    or a url without query string via ``HttpClientMetricNameStrategies.QUERYLESS_URL_AND_METHOD``


.. _man-client-jersey:

Jersey Client
=============

If HttpClient_ is too low-level for you, Dropwizard also supports Jersey's `Client API`_.
Jersey's ``Client`` allows you to use all of the server-side media type support that your service
uses to, for example, deserialize ``application/json`` request entities as POJOs.

.. _Client API: https://jersey.github.io/documentation/2.24/client.html

To create a :ref:`managed <man-core-managed>`, instrumented ``JerseyClient`` instance, your
:ref:`configuration class <man-core-configuration>` needs an :ref:`jersey client configuration <man-configuration-clients-jersey>` instance:

.. code-block:: java

    public class ExampleConfiguration extends Configuration {
        @Valid
        @NotNull
        private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

        @JsonProperty("jerseyClient")
        public JerseyClientConfiguration getJerseyClientConfiguration() {
            return jerseyClient;
        }
        
        @JsonProperty("jerseyClient")
        public void setJerseyClientConfiguration(JerseyClientConfiguration jerseyClient) {
            this.jerseyClient = jerseyClient;
        }
    }

Then, in your service's ``run`` method, create a new ``JerseyClientBuilder``:

.. code-block:: java

    @Override
    public void run(ExampleConfiguration config,
                    Environment environment) {

        final Client client = new JerseyClientBuilder(environment).using(config.getJerseyClientConfiguration())
                                                                  .build(getName());
        environment.jersey().register(new ExternalServiceResource(client));
    }

Configuration
-------------

The Client that Dropwizard creates deviates from the `Jersey Client Configuration` defaults. The
default, in Jersey, is for a client to never timeout reading or connecting in a request, while in
Dropwizard, the default is 500 milliseconds.

There are a couple of ways to change this behavior. The recommended way is to modify the
:ref:`YAML configuration <man-configuration-clients-http>`. Alternatively, set the properties on
the ``JerseyClientConfiguration``, which will take effect for all built clients. On a per client
basis, the configuration can be changed by utilizing the ``property`` method and, in this case,
the `Jersey Client Properties`_ can be used.

.. warning::

    Do not try to change Jersey properties using `Jersey Client Properties`_ through the

    ``withProperty(String propertyName, Object propertyValue)``

    method on the ``JerseyClientBuilder``, because by default it's configured by Dropwizard's
    ``HttpClientBuilder``, so the Jersey properties are ignored.

.. _Jersey Client Configuration: https://jersey.github.io/documentation/latest/appendix-properties.html#appendix-properties-client
.. _Jersey Client Properties: https://jersey.github.io/apidocs/2.24/jersey/org/glassfish/jersey/client/ClientProperties.html

.. _man-client-jersey-rx-usage:

Rx Usage
--------

To increase the ergonomics of asynchronous client requests, Jersey allows creation of `rx-clients`_.
You can instruct Dropwizard to create such a client:

.. code-block:: java

    @Override
    public void run(ExampleConfiguration config,
                    Environment environment) {

        final RxClient<RxCompletionStageInvoker> client =
            new JerseyClientBuilder(environment)
                .using(config.getJerseyClientConfiguration())
                .buildRx(getName(), RxCompletionStageInvoker.class);
        environment.jersey().register(new ExternalServiceResource(client));
    }

``RxCompletionStageInvoker.class`` is the Java 8 implementation and can be added to the pom:

.. code-block:: xml

    <dependency>
        <groupId>org.glassfish.jersey.ext.rx</groupId>
        <artifactId>jersey-rx-client-java8</artifactId>
    </dependency>

Alternatively, there are RxJava, Guava, and JSR-166e implementations.

By allowing Dropwizard to create the rx-client, the same thread pool that is utilized by traditional
synchronous and asynchronous requests, is used for rx requests.

.. _rx-clients: https://jersey.github.io/documentation/2.24/rx-client.html

Proxy Authentication
--------------------

The client can utilise a forward proxy, supporting both Basic and NTLM authentication schemes. 
Basic Auth against a proxy is simple:

.. code-block:: yaml
 
     proxy:
          host: '192.168.52.11'
          port: 8080
          scheme : 'https'
          auth:
            username: 'secret'
            password: 'stuff'
          nonProxyHosts:
            - 'localhost'
            - '192.168.52.*'
            - '*.example.com'   

NTLM Auth is configured by setting the the relevant windows properties. 

.. code-block:: yaml

     proxy:
          host: '192.168.52.11'
          port: 8080
          scheme : 'https'
          auth:
            username: 'secret'
            password: 'stuff'
            authScheme: 'NTLM'
            realm: 'realm'                    # optional, defaults to ANY_REALM
            hostname: 'workstation'           # optional, defaults to null but may be required depending on your AD environment
            domain: 'HYPERCOMPUGLOBALMEGANET' # optional, defaults to null but may be required depending on your AD environment
            credentialType: 'NT'
          nonProxyHosts:
            - 'localhost'
            - '192.168.52.*'
            - '*.example.com'   


