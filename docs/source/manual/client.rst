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

Your service's configuration file will then look like this:

.. code-block:: yaml

    httpClient:
      # timeout after 1s while connecting, reading, or writing
      timeout: 1s

      # keep connections open for 10 minutes
      timeToLive: 10m

      # don't track cookies
      cookiesEnabled: false

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

JerseyClient
============

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

Your service's configuration file will then look like this:

.. code-block:: yaml

    httpClient:
      timeout: 1s # timeout after 1s while connecting, reading, or writing
      timeToLive: 10m # keep connections open for 10 minutes
      cookiesEnabled: false # don't track cookies
      gzipEnabled: true # allow for gzipped request and response entities
      minThreads: 1
      maxThreads: 128 # thread pool for JerseyClient's async requests

