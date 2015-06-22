.. _man-configuration:

##################################
Dropwizard Configuration Reference
##################################

.. highlight:: text

.. _man-configuration-servers:

Servers
=======

.. code-block:: yaml

    server:
      type: default
      maxThreads: 1024


.. _man-configuration-all:

All
---

=================================== ===============================================  =============================================================================
Name                                Default                                          Description
=================================== ===============================================  =============================================================================
type                                default                                          - default
                                                                                     - simple
maxThreads                          1024                                             The maximum number of threads to use for requests.
minThreads                          8                                                The minimum number of threads to use for requests.
maxQueuedRequests                   1024                                             The maximum number of requests to queue before blocking
                                                                                     the acceptors.
idleThreadTimeout                   1 minute                                         The amount of time a worker thread can be idle before
                                                                                     being stopped.
nofileSoftLimit                     (none)                                           The number of open file descriptors before a soft error is issued.
                                                                                     Requires Jetty's ``libsetuid.so`` on ``java.library.path``.
nofileHardLimit                     (none)                                           The number of open file descriptors before a hard error is issued.
                                                                                     Requires Jetty's ``libsetuid.so`` on ``java.library.path``.
gid                                 (none)                                           The group ID to switch to once the connectors have started.
                                                                                     Requires Jetty's ``libsetuid.so`` on ``java.library.path``.
uid                                 (none)                                           The user ID to switch to once the connectors have started.
                                                                                     Requires Jetty's ``libsetuid.so`` on ``java.library.path``.
user                                (none)                                           The username to switch to once the connectors have started.
                                                                                     Requires Jetty's ``libsetuid.so`` on ``java.library.path``.
group                               (none)                                           The group to switch to once the connectors have started.
                                                                                     Requires Jetty's ``libsetuid.so`` on ``java.library.path``.
umask                               (none)                                           The umask to switch to once the connectors have started.
                                                                                     Requires Jetty's ``libsetuid.so`` on ``java.library.path``.
startsAsRoot                        (none)                                           Whether or not the Dropwizard application is started as a root user.
                                                                                     Requires Jetty's ``libsetuid.so`` on ``java.library.path``.
shutdownGracePeriod                 30 seconds                                       The maximum time to wait for Jetty, and all Managed instances,
                                                                                     to cleanly shutdown before forcibly terminating them.
allowedMethods                      ``GET``, ``POST``, ``PUT``, ``DELETE``,          The set of allowed HTTP methods. Others will be rejected with a
                                    ``HEAD``, ``OPTIONS``, ``PATCH``                 405 Method Not Allowed response.
rootPath                            ``/``                                            The URL pattern relative to ``applicationContextPath`` from which
                                                                                     the JAX-RS resources will be served.
registerDefaultExceptionMappers     true                                             Whether or not the default Jersey ExceptionMappers should be registered.
                                                                                     Set this to false if you want to register your own.
=================================== ===============================================  =============================================================================


.. _man-configuration-gzip:

GZip
....

.. code-block:: yaml

    server:
      gzip: 
        bufferSize: 8KiB


+----------------------+------------+---------------------------------------------------------------------------------------------------+ 
|     Name             | Default    | Description                                                                                       | 
+======================+============+===================================================================================================+ 
| enabled              | true       | If true, all requests with gzip in their Accept-Content-Encoding                                  | 
|                      |            | headers will have their response entities encoded with gzip.                                      |
+----------------------+------------+---------------------------------------------------------------------------------------------------+
| minimumEntitySize    | 256 bytes  | All response entities under this size are not compressed.                                         |
+----------------------+------------+---------------------------------------------------------------------------------------------------+
| bufferSize           | 8KiB       | The size of the buffer to use when compressing.                                                   |
+----------------------+------------+---------------------------------------------------------------------------------------------------+
| excludedUserAgents   | []         | The set of user agents to exclude from compression.                                               |
+----------------------+------------+---------------------------------------------------------------------------------------------------+
| compressedMimeTypes  | []         | If specified, the set of mime types to compress.                                                  |
+----------------------+------------+---------------------------------------------------------------------------------------------------+


.. _man-configuration-requestLog:

Request Log
...........

.. code-block:: yaml

    server:
      requestLog: 
        timeZone: UTC


====================== ================ ===========
Name                   Default          Description
====================== ================ ===========
timeZone               UTC              The time zone to which request timestamps will be converted.
appenders              console appender The set of AppenderFactory appenders to which requests will be logged.
                                        *TODO* See logging/appender refs for more info
====================== ================ ===========


.. _man-configuration-simple:

Simple
------

Extends the attributes that are available to :ref:`all servers <man-configuration-all>`

.. code-block:: yaml

    server:
      type: simple
      applicationContextPath: /application
      adminContextPath: /admin
      connector:
        type: http
        port: 8080



========================  ===============   =====================================================================
Name                      Default           Description
========================  ===============   =====================================================================
connector                 http connector    HttpConnectorFactory HTTP connector listening on port 8080.
                                            The ConnectorFactory connector which will handle both application
                                            and admin requests. TODO link to connector below.
applicationContextPath    /application      The context path of the application servlets, including Jersey.
adminContextPath          /admin            The context path of the admin servlets, including metrics and tasks.
========================  ===============   =====================================================================


.. _man-configuration-default:

Default
-------

Extends the attributes that are available to :ref:`all servers <man-configuration-all>`

.. code-block:: yaml

    server:
      adminMinThreads: 1
      adminMaxThreads: 64
      adminContextPath: /
      applicationContextPath: /
      applicationConnectors:
        - type: http
          port: 8080
        - type: https
          port: 8443
          keyStorePath: example.keystore
          keyStorePassword: example
          validateCerts: false
      adminConnectors:
        - type: http
          port: 8081
        - type: https
          port: 8444
          keyStorePath: example.keystore
          keyStorePassword: example
          validateCerts: false


========================  =======================   =====================================================================
Name                      Default                   Description
========================  =======================   =====================================================================
applicationConnectors     An `HTTP connector`_      A set of :ref:`connectors <man-configuration-connectors>` which will
                          listening on port 8080.   handle application requests.
adminConnectors           An `HTTP connector`_      An `HTTP connector`_ listening on port 8081.
                          listening on port 8081.   A set of :ref:`connectors <man-configuration-connectors>` which will 
                                                    handle admin requests.
adminMinThreads           1                         The minimum number of threads to use for admin requests.
adminMaxThreads           64                        The maximum number of threads to use for admin requests.
adminContextPath          /                         The context path of the admin servlets, including metrics and tasks.
applicationContextPath    /                         The context path of the application servlets, including Jersey.
========================  =======================   =====================================================================

.. _`HTTP connector`:  https://github.com/dropwizard/dropwizard/blob/master/dropwizard-jetty/src/main/java/io/dropwizard/jetty/HttpConnectorFactory.java

.. _man-configuration-connectors:

Connectors
==========


.. _man-configuration-http:

HTTP
----

.. code-block:: yaml
    
    # Extending from the default server configuration
    server:
      applicationConnectors:
        - type: http
          port: 8080
          bindHost: 127.0.0.1 # only bind to loopback
          headerCacheSize: 512 bytes
          outputBufferSize: 32KiB
          maxRequestHeaderSize: 8KiB
          maxResponseHeaderSize: 8KiB
          inputBufferSize: 8KiB
          idleTimeout: 30 seconds
          minBufferPoolSize: 64 bytes
          bufferPoolIncrement: 1KiB
          maxBufferPoolSize: 64KiB
          acceptorThreads: 1
          selectorThreads: 2
          acceptQueueSize: 1024
          reuseAddress: true
          soLingerTime: 345s
          useServerHeader: false
          useDateHeader: true
          useForwardedHeaders: true


======================== ==================  ======================================================================================
Name                     Default             Description
======================== ==================  ======================================================================================
port                     8080                The TCP/IP port on which to listen for incoming connections.
bindHost                 (none)              The hostname to bind to.
headerCacheSize          512 bytes           The size of the header field cache.
outputBufferSize         32KiB               The size of the buffer into which response content is aggregated before being sent to
                                             the client. A larger buffer can improve performance by allowing a content producer
                                             to run without blocking, however larger buffers consume more memory and may induce
                                             some latency before a client starts processing the content.
maxRequestHeaderSize     8KiB                The maximum size of a request header. Larger headers will allow for more and/or
                                             larger cookies plus larger form content encoded  in a URL. However, larger headers
                                             consume more memory and can make a server more vulnerable to denial of service
                                             attacks.
maxResponseHeaderSize    8KiB                The maximum size of a response header. Larger headers will allow for more and/or
                                             larger cookies and longer HTTP headers (eg for redirection).  However, larger headers
                                             will also consume more memory.
inputBufferSize          8KiB                The size of the per-connection input buffer.
idleTimeout              30 seconds          The maximum idle time for a connection, which roughly translates to the
                                             `java.net.Socket#setSoTimeout(int)`_ call, although with NIO implementations
                                             other mechanisms may be used to implement the timeout.
                                             The max idle time is applied when waiting for a new message to be received on a connection
                                             or when waiting for a new message to be sent on a connection.
                                             This value is interpreted as the maximum time between some progress being made on the
                                             connection. So if a single byte is read or written, then the timeout is reset.
minBufferPoolSize        64 bytes            The minimum size of the buffer pool. 
bufferPoolIncrement      1KiB                The increment by which the buffer pool should be increased.
maxBufferPoolSize        64KiB               The maximum size of the buffer pool.
acceptorThreads          # of CPUs/2         The number of worker threads dedicated to accepting connections.
selectorThreads          # of CPUs           The number of worker threads dedicated to sending and receiving data.
acceptQueueSize          (OS default)        The size of the TCP/IP accept queue for the listening socket.
reuseAddress             true                Whether or not ``SO_REUSEADDR`` is enabled on the listening socket.
soLingerTime             (disabled)          Enable/disable ``SO_LINGER`` with the specified linger time.
useServerHeader          false               Whether or not to add the ``Server`` header to each response.
useDateHeader            true                Whether or not to add the ``Date`` header to each response.
useForwardedHeaders      true                Whether or not to look at ``X-Forwarded-*`` headers added by proxies. See
                                             `ForwardedRequestCustomizer`_ for details.
======================== ==================  ======================================================================================

.. _`java.net.Socket#setSoTimeout(int)`: http://docs.oracle.com/javase/7/docs/api/java/net/Socket.html#setSoTimeout(int)
.. _`ForwardedRequestCustomizer`: http://download.eclipse.org/jetty/stable-9/apidocs/org/eclipse/jetty/server/ForwardedRequestCustomizer.html

.. _man-configuration-https:

HTTPS
-----

Extends the attributes that are available to the :ref:`HTTP connector <man-configuration-http>`

.. code-block:: yaml
    
    # Extending from the default server configuration
    server:
      applicationConnectors:
        - type: https
          port: 8443
          ....
          keyStorePath: /path/to/file
          keyStorePassword: changeit
          keyStoreType: JKS
          keyStoreProvider: 
          trustStorePath: /path/to/file
          trustStorePassword: changeit
          trustStoreType: JKS
          trustStoreProvider: 
          keyManagerPassword: changeit
          needClientAuth: false
          wantClientAuth: 
          certAlias: <alias>
          crlPath: /path/to/file
          enableCRLDP: false
          enableOCSP: false
          maxCertPathLength: (unlimited)
          ocspResponderUrl: (none)
          jceProvider: (none)
          validateCerts: true
          validatePeers: true
          supportedProtocols: SSLv3
          excludedProtocols: (none)
          supportedCipherSuites: TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256
          excludedCipherSuites: (none)
          allowRenegotiation: true
          endpointIdentificationAlgorithm: (none)

================================ ==================  ======================================================================================
Name                             Default             Description
================================ ==================  ======================================================================================
keyStorePath                     REQUIRED            The path to the Java key store which contains the host certificate and private key.
keyStorePassword                 REQUIRED            The password used to access the key store.
keyStoreType                     JKS                 The type of key store (usually ``JKS``, ``PKCS12``, JCEKS``,
                                                     ``Windows-MY``}, or ``Windows-ROOT``).
keyStoreProvider                 (none)              The JCE provider to use to access the key store.
trustStorePath                   (none)              The path to the Java key store which contains the CA certificates used to establish
                                                     trust.
trustStorePassword               (none)              The password used to access the trust store.
trustStoreType                   JKS                 The type of trust store (usually ``JKS``, ``PKCS12``, ``JCEKS``,
                                                     ``Windows-MY``, or ``Windows-ROOT``).
trustStoreProvider               (none)              The JCE provider to use to access the trust store.
keyManagerPassword               (none)              The password, if any, for the key manager.
needClientAuth                   (none)              Whether or not client authentication is required.
wantClientAuth                   (none)              Whether or not client authentication is requested.
certAlias                        (none)              The alias of the certificate to use.
crlPath                          (none)              The path to the file which contains the Certificate Revocation List.
enableCRLDP                      false               Whether or not CRL Distribution Points (CRLDP) support is enabled.
enableOCSP                       false               Whether or not On-Line Certificate Status Protocol (OCSP) support is enabled.
maxCertPathLength                (unlimited)         The maximum certification path length.
ocspResponderUrl                 (none)              The location of the OCSP responder.
jceProvider                      (none)              The name of the JCE provider to use for cryptographic support.
validateCerts                    true                Whether or not to validate TLS certificates before starting. If enabled, Dropwizard
                                                     will refuse to start with expired or otherwise invalid certificates.
validatePeers                    true                Whether or not to validate TLS peer certificates.
supportedProtocols               (none)              A list of protocols (e.g., ``SSLv3``, ``TLSv1``) which are supported. All
                                                     other protocols will be refused.
excludedProtocols                (none)              A list of protocols (e.g., ``SSLv3``, ``TLSv1``) which are excluded. These
                                                     protocols will be refused.
supportedCipherSuites            (none)              A list of cipher suites (e.g., ``TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256``) which
                                                     are supported. All other cipher suites will be refused
excludedCipherSuites             (none)              A list of cipher suites (e.g., ``TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256``) which
                                                     are excluded. These cipher suites will be refused and exclusion takes higher 
                                                     precedence than inclusion, such that if a cipher suite is listed in 
                                                     ``supportedCipherSuites`` and ``excludedCipherSuites``, the cipher suite will be
                                                     excluded. To verify that the proper cipher suites are being whitelisted and
                                                     blacklisted, it is recommended to use the tool `sslyze`_.
allowRenegotiation               true                Whether or not TLS renegotiation is allowed.
endpointIdentificationAlgorithm  (none)              Which endpoint identification algorithm, if any, to use during the TLS handshake.
================================ ==================  ======================================================================================

.. _sslyze: https://github.com/iSECPartners/sslyze

.. _man-configuration-spdy:

SPDY
----

Extends the attributes that are available to the :ref:`HTTPS connector <man-configuration-https>`

For this connector to work with ALPN protocol you need to provide alpn-boot library to JVM's bootpath.
The correct library version depends on the JVM version. Consult Jetty ALPN guide__ for the reference.

.. __: http://www.eclipse.org/jetty/documentation/current/alpn-chapter.html

.. code-block:: yaml

    server:
      applicationConnectors:
        - type: spdy3
          port: 8445
          keyStorePath: example.keystore
          keyStorePassword: example
          validateCerts: false


====================== ===========  ===========
Name                   Default      Description
====================== ===========  ===========
pushStrategy           (none)       The `push strategy`_ to use for server-initiated SPDY pushes.
====================== ===========  ===========

.. _`push strategy`: https://github.com/dropwizard/dropwizard/blob/master/dropwizard-spdy/src/main/java/io/dropwizard/spdy/PushStrategyFactory.java


.. _man-configuration-logging:

Logging
=======

.. code-block:: yaml

    logging:
      level: INFO
      loggers:
        io.dropwizard: INFO
      appenders:
        - type: console


====================== ===========  ===========
Name                   Default      Description
====================== ===========  ===========
level                  Level.INFO   Logback logging level
loggers                (none)       
appenders              (none)       one of console, file or syslog
====================== ===========  ===========


.. _man-configuration-logging-console:

Console
-------

.. code-block:: yaml

    logging:
      level: INFO
      appenders:
        - type: console
          threshold: ALL
          timeZone: UTC
          target: stdout
          logFormat: # TODO


====================== ===========  ===========
Name                   Default      Description
====================== ===========  ===========
type                   REQUIRED     The appender type. Must be ``console``.
threshold              ALL          The lowest level of events to print to the console.
timeZone               UTC          The time zone to which event timestamps will be converted.
target                 stdout       The name of the standard stream to which events will be written.
                                    Can be ``stdout`` or ``stderr``.
logFormat              default      The Logback pattern with which events will be formatted. See
                                    the Logback_ documentation for details.
====================== ===========  ===========

.. _Logback: http://logback.qos.ch/manual/layouts.html#conversionWord


.. _man-configuration-logging-file:

File
----

.. code-block:: yaml

    logging:
      level: INFO
      appenders:
        - type: file
          currentLogFilename: /var/log/myapplication.log
          threshold: ALL
          archive: true
          archivedLogFilenamePattern: /var/log/myapplication-%d.log
          archivedFileCount: 5
          timeZone: UTC
          logFormat: # TODO


============================ ===========  ==================================================================================================
Name                         Default      Description
============================ ===========  ==================================================================================================
type                         REQUIRED     The appender type. Must be ``file``.
currentLogFilename           REQUIRED     The filename where current events are logged.
threshold                    ALL          The lowest level of events to write to the file.
archive                      true         Whether or not to archive old events in separate files.
archivedLogFilenamePattern   (none)       Required if ``archive`` is ``true``.
                                          The filename pattern for archived files. ``%d`` is replaced with the date in ``yyyy-MM-dd`` form,
                                          and the fact that it ends with ``.gz`` indicates the file will be gzipped as it's archived.                                
                                          Likewise, filename patterns which end in ``.zip`` will be filled as they are archived.
archivedFileCount            5            The number of archived files to keep. Must be between ``1`` and ``50``.
timeZone                     UTC          The time zone to which event timestamps will be converted.
logFormat                    default      The Logback pattern with which events will be formatted. See
                                          the Logback_ documentation for details.
============================ ===========  ==================================================================================================


.. _man-configuration-logging-syslog:

Syslog
------

.. code-block:: yaml

    logging:
      level: INFO
      appenders:
        - type: syslog
          host: localhost
          port: 514
          facility: local0
          threshold: ALL
          stackTracePrefix: \t
          logFormat: # TODO


============================ ===========  ==================================================================================================
Name                         Default      Description
============================ ===========  ==================================================================================================
host                         localhost    The hostname of the syslog server.
port                         514          The port on which the syslog server is listening.
facility                     local0       The syslog facility to use. Can be either ``auth``, ``authpriv``,
                                          ``daemon``, ``cron``, ``ftp``, ``lpr``, ``kern``, ``mail``,
                                          ``news``, ``syslog``, ``user``, ``uucp``, ``local0``,
                                          ``local1``, ``local2``, ``local3``, ``local4``, ``local5``,
                                          ``local6``, or ``local7``.
threshold                    ALL          The lowest level of events to write to the file.
logFormat                    default      The Logback pattern with which events will be formatted. See
                                          the Logback_ documentation for details.
stackTracePrefix             \t           The prefix to use when writing stack trace lines (these are sent
                                          to the syslog server separately from the main message)
============================ ===========  ==================================================================================================


.. _man-configuration-metrics:

Metrics
=======

The metrics configuration has two fields; frequency and reporters.

.. code-block:: yaml

    metrics:
      frequency: 1 minute
      reporters:
        - type: <type>


====================== ===========  ===========
Name                   Default      Description
====================== ===========  ===========
frequency              1 minute     The frequency to report metrics. Overridable per-reporter.
reporters              (none)       A list of reporters to report metrics.
====================== ===========  ===========


.. _man-configuration-metrics-all:

All Reporters
-------------

The following options are available for all metrics reporters.

.. code-block:: yaml

    metrics:
      reporters:
        - type: <type>
          durationUnit: milliseconds
          rateUnit: seconds
          excludes: (none)
          includes: (all)
          useRegexFilters: false
          frequency: 1 minute


====================== =============  ===========
Name                   Default        Description
====================== =============  ===========
durationUnit           milliseconds   The unit to report durations as. Overrides per-metric duration units.
rateUnit               seconds        The unit to report rates as. Overrides per-metric rate units.
excludes               (none)         Metrics to exclude from reports, by name. When defined, matching metrics will not be reported.
includes               (all)          Metrics to include in reports, by name. When defined, only these metrics will be reported.
useRegexFilters        false          Indicates whether the values of the 'includes' and 'excludes' fields should be treated as regular expressions or not.
frequency              (none)         The frequency to report metrics. Overrides the default.
====================== =============  ===========

The inclusion and exclusion rules are defined as:

* If **includes** is empty, then all metrics are included;
* If **includes** is not empty, only metrics from this list are included;
* If **excludes** is empty, no metrics are excluded;
* If **excludes** is not empty, then exclusion rules take precedence over inclusion rules. Thus if a name matches
the exclusion rules it will not be included in reports even if it also matches the inclusion rules.

.. _man-configuration-metrics-formatted:

Formatted Reporters
...................

These options are available only to "formatted" reporters and extend the options available to :ref:`all reporters <man-configuration-metrics-all>`

.. code-block:: yaml

    metrics:
      reporters:
        - type: <type>
          locale: <system default>


====================== ===============  ===========
Name                   Default          Description
====================== ===============  ===========
locale                 System default   The Locale_ for formatting numbers, dates and times.
====================== ===============  ===========

.. _Locale: http://docs.oracle.com/javase/7/docs/api/java/util/Locale.html

.. _man-configuration-metrics-console:

Console Reporter
----------------

Reports metrics periodically to the console.

Extends the attributes that are available to :ref:`formatted reporters <man-configuration-metrics-formatted>`

.. code-block:: yaml

    metrics:
      reporters:
        - type: console
          timeZone: UTC
          output: stdout


====================== ===============  ===========
Name                   Default          Description
====================== ===============  ===========
timeZone               UTC              The timezone to display dates/times for.
output                 stdout           The stream to write to. One of ``stdout`` or ``stderr``.
====================== ===============  ===========


.. _man-configuration-metrics-csv:

CSV Reporter
------------

Reports metrics periodically to a CSV file.

Extends the attributes that are available to :ref:`formatted reporters <man-configuration-metrics-formatted>`

.. code-block:: yaml

    metrics:
      reporters:
        - type: csv
          file: /path/to/file


====================== ===============  ===========
Name                   Default          Description
====================== ===============  ===========
file                   No default       The CSV file to write metrics to.
====================== ===============  ===========


.. _man-configuration-metrics-ganglia:

Ganglia Reporter
----------------

Reports metrics periodically to Ganglia.

Extends the attributes that are available to :ref:`all reporters <man-configuration-metrics-all>`

.. note::

    You will need to add ``dropwizard-metrics-ganglia`` to your POM.

.. code-block:: yaml

    metrics:
      reporters:
        - type: ganglia
          host: localhost
          port: 8649
          mode: unicast
          ttl: 1
          uuid: (none)
          spoof: localhost:8649
          tmax: 60
          dmax: 0


====================== ===============  ====================================================================================================
Name                   Default          Description
====================== ===============  ====================================================================================================
host                   localhost        The hostname (or group) of the Ganglia server(s) to report to.
port                   8649             The port of the Ganglia server(s) to report to.
mode                   unicast          The UDP addressing mode to announce the metrics with. One of ``unicast`` 
                                        or ``multicast``.
ttl                    1                The time-to-live of the UDP packets for the announced metrics.
uuid                   (none)           The UUID to tag announced metrics with.
spoof                  (none)           The hostname and port to use instead of this nodes for the announced metrics. 
                                        In the format ``hostname:port``.
tmax                   60               The tmax value to announce metrics with.
dmax                   0                The dmax value to announce metrics with.
====================== ===============  ====================================================================================================


.. _man-configuration-metrics-graphite:

Graphite Reporter
-----------------

Reports metrics periodically to Graphite.

Extends the attributes that are available to :ref:`all reporters <man-configuration-metrics-all>`

.. note::

    You will need to add ``dropwizard-metrics-graphite`` to your POM.

.. code-block:: yaml

    metrics:
      reporters:
        - type: graphite
          host: localhost
          port: 8080
          prefix: <prefix>


====================== ===============  ====================================================================================================
Name                   Default          Description
====================== ===============  ====================================================================================================
host                   localhost        The hostname of the Graphite server to report to.
port                   8080             The port of the Graphite server to report to.
prefix                 (none)           The prefix for Metric key names to report to Graphite.
====================== ===============  ====================================================================================================


.. _man-configuration-metrics-slf4j:

SLF4J
-----

Reports metrics periodically by logging via SLF4J.

Extends the attributes that are available to :ref:`all reporters <man-configuration-metrics-all>`

See BaseReporterFactory_  and BaseFormattedReporterFactory_ for more options.

.. _BaseReporterFactory:  https://github.com/dropwizard/dropwizard/blob/master/dropwizard-metrics/src/main/java/io/dropwizard/metrics/BaseReporterFactory.java
.. _BaseFormattedReporterFactory: https://github.com/dropwizard/dropwizard/blob/master/dropwizard-metrics/src/main/java/io/dropwizard/metrics/BaseFormattedReporterFactory.java


.. code-block:: yaml

    metrics:
      reporters:
        - type: log
          logger: metrics
          markerName: <marker name>


====================== ===============  ====================================================================================================
Name                   Default          Description
====================== ===============  ====================================================================================================
logger                 metrics          The name of the logger to write metrics to.
markerName             (none)           The name of the marker to mark logged metrics with.
====================== ===============  ====================================================================================================


.. _man-configuration-clients:

Clients
=======

.. _man-configuration-clients-http:

HttpClient
----------

See HttpClientConfiguration_  for more options.

.. _HttpClientConfiguration:  https://github.com/dropwizard/dropwizard/blob/master/dropwizard-client/src/main/java/io/dropwizard/client/HttpClientConfiguration.java

.. code-block:: yaml

    httpClient:
      timeout: 500ms
      connectionTimeout: 500ms
      timeToLive: 1h
      cookiesEnabled: false
      maxConnections: 1024
      maxConnectionsPerRoute: 1024
      keepAlive: 0ms
      retries: 0
      userAgent: <application name> (<client name>)


========================= ======================================  =============================================================================
Name                      Default                                 Description
========================= ======================================  =============================================================================
timeout                   500 milliseconds                        The maximum idle time for a connection, once established.
connectionTimeout         500 milliseconds                        The maximum time to wait for a connection to open.
connectionRequestTimeout  500 milliseconds                        The maximum time to wait for a connection to be returned from the connection pool.
timeToLive                1 hour                                  The maximum time a pooled connection can stay idle (not leased to any thread)
                                                                  before it is shut down.
cookiesEnabled            false                                   Whether or not to enable cookies.
maxConnections            1024                                    The maximum number of concurrent open connections.
maxConnectionsPerRoute    1024                                    The maximum number of concurrent open connections per route.
keepAlive                 0 milliseconds                          The maximum time a connection will be kept alive before it is reconnected. If set
                                                                  to 0, connections will be immediately closed after every request/response.
retries                   0                                       The number of times to retry failed requests. Requests are only
                                                                  retried if they throw an exception other than ``InterruptedIOException``,
                                                                  ``UnknownHostException``, ``ConnectException``, or ``SSLException``.
userAgent                 ``applicationName`` (``clientName``)    The User-Agent to send with requests.
========================= ======================================  =============================================================================


.. _man-configuration-clients-http-proxy:

Proxy
.....

.. code-block:: yaml

    httpClient:
      proxy:
        host: 192.168.52.11
        port: 8080
        scheme : http
        auth:
          username: secret
          password: stuff
        nonProxyHosts:
          - localhost
          - '192.168.52.*'
          - '*.example.com'


=============   =================  ======================================================================
Name            Default            Description
=============   =================  ======================================================================
host            REQUIRED           The proxy server host name or ip address.
port            (scheme default)   The proxy server port.
                                   If the port is not set then the scheme default port is used.
scheme          http               The proxy server URI scheme. HTTP and HTTPS schemas are permitted.
                                   By default HTTP scheme is used.
auth            (none)             The proxy server BASIC authentication credentials.
                                   If they are not set then no credentials will be passed to the server.
username        REQUIRED           The username used to connect to the server.
password        REQUIRED           The password used to connect to the server.

nonProxyHosts   (none)             List of patterns of hosts that should be reached without proxy.
                                   The patterns may contain symbol '*' as a wildcard.
                                   If a host matches one of the patterns it will be reached through a direct connection.
=============   =================  ======================================================================


.. _man-configuration-clients-jersey:

JerseyClient
------------

Extends the attributes that are available to :ref:`http clients <man-configuration-clients-http>`

See JerseyClientConfiguration_ and HttpClientConfiguration_ for more options.

.. _JerseyClientConfiguration:  https://github.com/dropwizard/dropwizard/blob/master/dropwizard-client/src/main/java/io/dropwizard/client/JerseyClientConfiguration.java

.. code-block:: yaml

    jerseyClient:
      minThreads: 1
      maxThreads: 128
      workQueueSize: 8
      gzipEnabled: true
      gzipEnabledForRequests: true
      chunkedEncodingEnabled: true


======================= ==================  ===================================================================================================
Name                    Default             Description
======================= ==================  ===================================================================================================
minThreads              1                   The minimum number of threads in the pool used for asynchronous requests.
maxThreads              128                 The maximum number of threads in the pool used for asynchronous requests.
workQueueSize           8                   The size of the work queue of the pool used for asynchronous requests.
                                            Additional threads will be spawn only if the queue is reached its maximum size.
gzipEnabled             true                Adds an Accept-Encoding: gzip header to all requests, and enables automatic gzip decoding of responses.
gzipEnabledForRequests  true                Adds a Content-Encoding: gzip header to all requests, and enables automatic gzip encoding of requests.
chunkedEncodingEnabled  true                Enables the use of chunked encoding for requests.
======================= ==================  ===================================================================================================


.. _man-configuration-database:

Database
========

.. code-block:: yaml

    database:
      driverClass : org.postgresql.Driver
      url: 'jdbc:postgresql://db.example.com/db-prod'
      user: pg-user
      password: iAMs00perSecrEET


============================    =====================    ===============================================================
Name                            Default                  Description
============================    =====================    ===============================================================
driverClass                     REQUIRED                 The full name of the JDBC driver class.

url                             REQUIRED                 The URL of the server.

user                            REQUIRED                 The username used to connect to the server.

password                        none                     The password used to connect to the server.

abandonWhenPercentageFull       0                        Connections that have been abandoned (timed out) won't get
                                                         closed and reported up unless the number of connections in use
                                                         are above the percentage defined by abandonWhenPercentageFull.
                                                         The value should be between 0-100.

alternateUsernamesAllowed       false                    Set to true if the call getConnection(username,password) is
                                                         allowed. This is used for when the pool is used by an
                                                         application  accessing multiple schemas. There is a
                                                         performance impact turning this option on, even when not used.

commitOnReturn                  false                    Set to true if you want the connection pool to commit any
                                                         pending transaction when a connection is returned.

autoCommitByDefault             JDBC driver's default    The default auto-commit state of the connections.

readOnlyByDefault               JDBC driver's default    The default read-only state of the connections.

properties                      none                     Any additional JDBC driver parameters.

defaultCatalog                  none                     The default catalog to use for the connections.

defaultTransactionIsolation     JDBC driver's default    The default transaction isolation to use for the connections.
                                                         Can be one of none, default, read-uncommitted, read-committed,
                                                         repeatable-read, or serializable.

useFairQueue                    true                     If true, calls to getConnection are handled in a FIFO manner.

initialSize                     10                       The initial size of the connection pool.

minSize                         10                       The minimum size of the connection pool.

maxSize                         100                      The maximum size of the connection pool.

initializationQuery             none                     A custom query to be run when a connection is first created.

logAbandonedConnections         false                    If true, logs stack traces of abandoned connections.

logValidationErrors             false                    If true, logs errors when connections fail validation.

maxConnectionAge                none                     If set, connections which have been open for longer than
                                                         maxConnectionAge are closed when returned.

maxWaitForConnection            30 seconds               If a request for a connection is blocked for longer than this
                                                         period, an exception will be thrown.

minIdleTime                     1 minute                 The minimum amount of time an connection must sit idle in the
                                                         pool before it is eligible for eviction.

validationQuery                 SELECT 1                 The SQL query that will be used to validate connections from
                                                         this pool before returning them to the caller or pool.
                                                         If specified, this query does not have to return any data, it
                                                         just can't throw a SQLException.

validationQueryTimeout          none                     The timeout before a connection validation queries fail.

checkConnectionWhileIdle        true                     Set to true if query validation should take place while the
                                                         connection is idle.

checkConnectionOnBorrow         false                    Whether or not connections will be validated before being
                                                         borrowed from the pool. If the connection fails to validate,
                                                         it will be dropped from the pool, and another will be
                                                         borrowed.

checkConnectionOnConnect        false                    Whether or not connections will be validated before being
                                                         added to the pool. If the connection fails to validate,
                                                         it won't be added to the pool.

checkConnectionOnReturn         false                    Whether or not connections will be validated after being
                                                         returned to the pool. If the connection fails to validate, it
                                                         will be dropped from the pool.

autoCommentsEnabled             true                     Whether or not ORMs should automatically add comments.

evictionInterval                5 seconds                The amount of time to sleep between runs of the idle
                                                         connection validation, abandoned cleaner and idle pool
                                                         resizing.

validationInterval              30 seconds               To avoid excess validation, only run validation once every
                                                         interval.

validatorClassName              none                     Name of a class of a custom validator implementation, which
                                                         will be used for validating connections.
============================    =====================    ===============================================================

.. _man-configuration-polymorphic:

Polymorphic configuration
========

.. rubric:: The ``dropwizard-configuration`` module provides you with a polymorphic configuration
            mechanism, meaning that a particular section of your configuration file can be implemented
            using one or more configuration classes.

To use this capability for your own configuration classes, create a top-level configuration interface or class that
implements ``Discoverable`` and add the name of that class to ``META-INF/services/io.dropwizard.jackson.Discoverable``.
Make sure to use `Jackson polymorphic deserialization`_ annotations appropriately.

.. _Jackson polymorphic deserialization: http://wiki.fasterxml.com/JacksonPolymorphicDeserialization

.. code-block:: java

    @JsonTypeInfo(use = Id.NAME, include = As.PROPERTY, property = "type")
    interface WidgetFactory extends Discoverable {
        Widget createWidget();
    }

Then create subtypes of the top-level type corresponding to each alternative, and add their names to
``META-INF/services/WidgetFactory``.

.. code-block:: java

    @JsonTypeName("hammer")
    public class HammerFactory implements WidgetFactory {
        @JsonProperty
        private int weight = 10;

        @Override
        public Hammer createWidget() {
            return new Hammer(weight);
        }
    }

    @JsonTypeName("chisel")
    public class ChiselFactory implements WidgetFactory {
        @JsonProperty
        private float radius = 1;

        @Override
        public Chisel createWidget() {
            return new Chisel(radius);
        }
    }

Now you can use ``WidgetFactory`` objects in your application's configuration.

.. code-block:: java

    public class MyConfiguration extends Configuration {
        @JsonProperty
        @NotNull
        @Valid
        private List<WidgetFactory> widgets;
    }

.. code-block:: yaml

    widgets:
      - type: hammer
        weight: 20
      - type: chisel
        radius: 0.4
