.. _man-client:

#################
Dropwizard Client
#################

.. highlight:: text

.. rubric:: The ``dropwizard-client`` module provides you with two different performant,
            instrumented HTTP clients so you can integrate your service with other web
            services: :ref:`man-client-apache` and :ref:`man-client-jersey`.

.. _man-client-apache:

Apache HttpClient, version 4.3
===============================

The underlying library for ``dropwizard-client`` is  Apache's HttpClient_, a full-featured,
well-tested HTTP client library.

.. _HttpClient: http://hc.apache.org/httpcomponents-core-4.3.x/index.html

To create a :ref:`managed <man-core-managed>`, instrumented ``HttpClient`` instance, your
:ref:`configuration class <man-core-configuration>` needs an :ref:`http client configuration <man-configuration-clients-http>` instance:

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

Then, in your application's ``run`` method, create a new ``HttpClientBuilder``:

.. code-block:: java

    @Override
    public void run(ExampleConfiguration config,
                    Environment environment) {
        final HttpClient httpClient = new HttpClientBuilder(environment).using(config.getHttpClientConfiguration())
                                                                        .build();
        environment.addResource(new ExternalServiceResource(httpClient));
    }

.. _man-client-apache-metrics:

Metrics
-------

Dropwizard's ``HttpClientBuilder`` actually gives you an instrumented subclass which tracks the
following pieces of data:

``org.apache.http.conn.ClientConnectionManager.available-connections``
    The number the number idle connections ready to be used to execute requests.

``org.apache.http.conn.ClientConnectionManager.leased-connections``
    The number of persistent connections currently being used to execut requests.

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

Jersey Client, version 1.18
===========================

If HttpClient_ is too low-level for you, Dropwizard also supports Jersey's `Client API`_.
Jersey's ``Client`` allows you to use all of the server-side media type support that your service
uses to, for example, deserialize ``application/json`` request entities as POJOs.

.. _Client API: https://jersey.java.net/documentation/1.18/client-api.html

To create a :ref:`managed <man-core-managed>`, instrumented ``JerseyClient`` instance, your
:ref:`configuration class <man-core-configuration>` needs an :ref:`jersey client configuration <man-configuration-clients-jersey>` instance:

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

        final Client client = new JerseyClientBuilder(environment).using(config.getJerseyClientConfiguration())
                                                                  .build(getName());                                                       
        environment.addResource(new ExternalServiceResource(client));
    }

