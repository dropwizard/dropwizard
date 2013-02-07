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

.. _HttpClient: http://hc.apache.org/httpcomponents-client-ga/

To create a :ref:`managed <man-core-managed>`, instrumented ``HttpClient`` instance, your
:ref:`configuration class <man-core-configuration>` needs an ``HttpClientConfiguration`` instance:

.. code-block:: java

    public class ExampleConfiguration extends Configuration {
        @Valid
        @NotNull
        @JsonProperty
        private HttpClientConfiguration httpClient = new HttpClientConfiguration();

        public HttpClientConfiguration getHttpClientConfiguration() {
            return httpClient;
        }
    }

Then, in your service's ``run`` method, create a new ``HttpClientBuilder``:

.. code-block:: java

    @Override
    public void run(ExampleConfiguration config,
                    Environment environment) {
        final HttpClient httpClient = new HttpClientBuilder().using(config.getHttpClientConfiguration())
                                                             .build();
        environment.addResource(new ExternalServiceResource(httpClient));
    }

.. _man-client-apache-config:

Configuration Defaults
----------------------

The default configuration for ``HttpClientConfiguration`` is as follows:

.. code-block:: yaml

    # The socket timeout value. If a read or write to the underlying
    # TCP/IP connection hasn't succeeded after this duration, a
    # timeout exception is thrown.
    timeout: 500ms

    # The connection timeout value. If a TCP/IP connection cannot be
    # established in this time, a timeout exception is thrown.
    connectionTimeout: 500ms

    # The time a TCP/IP connection to the server is allowed to
    # persist before being explicitly closed.
    timeToLive: 1 hour

    # If true, cookies will be persisted in memory for the duration
    # of the client's lifetime. If false, cookies will be ignored
    # entirely.
    cookiesEnabled: false

    # The maximum number of connections to be held in the client's
    # connection pool.
    maxConnections: 1024

    # The maximum number of connections per "route" to be held in
    # the client's connection pool. A route is essentially a
    # combination of hostname, port, configured proxies, etc.
    maxConnectionsPerRoute: 1024

    # The default value for a persistent connection's keep-alive.
    # A value of 0 will result in connections being immediately
    # closed after a response.
    keepAlive: 0s

.. _man-client-apache-metrics:

Metrics
-------

Dropwizard's ``HttpClientBuilder`` actually gives you an instrumented subclass which tracks the
following pieces of data:

``org.apache.http.conn.ClientConnectionManager.connections``
    The number of open connections currently in the connection pool.

``org.apache.http.impl.conn.tsccm.ConnPoolByRoute.new-connections``
    The rate at which new connections are being created.

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

.. _man-client-jersey:

Jersey Client
=============

If HttpClient_ is too low-level for you, Dropwizard also supports Jersey's `Client API`_.
Jersey's ``Client`` allows you to use all of the server-side media type support that your service
uses to, for example, deserialize ``application/json`` request entities as POJOs.

.. _Client API: http://jersey.java.net/nonav/documentation/latest/user-guide.html#client-api

To create a :ref:`managed <man-core-managed>`, instrumented ``JerseyClient`` instance, your
:ref:`configuration class <man-core-configuration>` needs an ``JerseyClientConfiguration`` instance:

.. code-block:: java

    public class ExampleConfiguration extends Configuration {
        @Valid
        @NotNull
        @JsonProperty
        private JerseyClientConfiguration httpClient = new JerseyClientConfiguration();

        public JerseyClientConfiguration getJerseyClientConfiguration() {
            return httpClient;
        }
    }

Then, in your service's ``run`` method, create a new ``JerseyClientBuilder``:

.. code-block:: java

    @Override
    public void run(ExampleConfiguration config,
                    Environment environment) {
        final Client client = new JerseyClientBuilder().using(config.getJerseyClientConfiguration())
                                                       .using(environment)
                                                       .build();
        environment.addResource(new ExternalServiceResource(client));
    }

.. _man-client-jersey-config:

Configuration Defaults
----------------------

In addition to the properties in the :ref:`HttpClient configuration <man-client-apache-config>`,
``JerseyClientConfiguration`` adds the following:

.. code-block:: yaml

    # The minimum number of threads to use for asynchronous calls.
    minThreads: 1

    # The maximum number of threads to use for asynchronous calls.
    maxThreads: 128

    # If true, the client will automatically decode response entities
    # with gzip content encoding.
    gzipEnabled: true

    # If true, the client will encode request entities with gzip
    # content encoding. (Requires gzipEnabled to be true).
    gzipEnabledForRequests: true
