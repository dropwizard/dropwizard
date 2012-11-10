.. _release-notes:

#############
Release Notes
#############

.. _rel-0.6.0:

v0.6.0-SNAPSHOT: TBD
====================

* Added Hibernate support in ``dropwizard-hibernate``.
* Added Liquibase migrations in ``dropwizard-migrations``.
* Renamed ``http.acceptorThreadCount`` to ``http.acceptorThreads``.
* Dropped ``JerseyClient``. Use Jersey's ``Client`` class instead.
* Moved JDBI support to ``dropwizard-jdbi``.
* Dropped ``Database``. Use JDBI's ``DBI`` class instead.
* Dropped the ``Json`` class. Use ``ObjectMapperFactory`` and ``ObjectMapper`` instead.
* Decoupled JDBI support from tomcat-dbcp.
* Added group support to ``Validator``.
* Moved CLI support to argparse4j.
* Fixed testing support for ``Optional`` resource method parameters.
* Fixed Freemarker support to use its internal encoding map.
* Added property support to ``ResourceTest``.
* Fixed JDBI metrics support for raw SQL queries.
* Dropped Hamcrest matchers in favor of FEST assertions in ``dropwizard-testing``.
* Split ``Environment`` into ``Bootstrap`` and ``Environment``, and broke configuration of each into
  ``Service``'s ``#initialize(Bootstrap)`` and ``#run(Configuration, Environment)``.
* Combined ``AbstractService`` and ``Service``.
* Trimmed down ``ScalaService``, so be sure to add ``ScalaBundle``.
* Added support for using ``JerseyClientFactory`` without an ``Environment``.
* Dropped Jerkson in favor of Jackson's Scala module.
* Added ``Optional`` support for JDBI.
* Fixed bug in stopping ``AsyncRequestLog``.
* Upgraded to Jetty 8.1.7.
* Upgraded to Mockito 1.9.5.
* Upgraded to tomcat-dbcp 7.0.32.
* Upgraded to Mustache 0.8.8.
* Upgraded to Jersey 1.15.
* Upgraded to Apache HttpClient 4.2.2.
* Upgraded to Metrics 3.0.0.
* Upgraded to JDBI 2.41.
* Upgraded to Logback 1.0.7 and SLF4J 1.7.2.
* Upgraded to Guava 13.0.1.
* Upgraded to Jackson 2.1.0.

.. _rel-0.5.1:

v0.5.1: Aug 06 2012
===================

* Fixed logging of managed objects.
* Fixed default file logging configuration.
* Added FEST-Assert as a ``dropwizard-testing`` dependency.
* Added support for Mustache templates (``*.mustache``) to ``dropwizard-views``.
* Added support for arbitrary view renderers.
* Fixed command-line overrides when no configuration file is present.
* Added support for arbitrary ``DnsResolver`` implementations in ``HttpClientFactory``.
* Upgraded to Guava 13.0 final.
* Fixed task path bugs.
* Upgraded to Metrics 2.1.3.
* Added ``JerseyClientConfiguration#compressRequestEntity`` for disabling the compression of request
  entities.
* Added ``Environment#scanPackagesForResourcesAndProviders`` for automatically detecting Jersey
  providers and resources.
* Added ``Environment#setSessionHandler``.

.. _rel-0.5.0:

v0.5.0: Jul 30 2012
===================

* Upgraded to JDBI 2.38.1.
* Upgraded to Jackson 1.9.9.
* Upgraded to Jersey 1.13.
* Upgraded to Guava 13.0-rc2.
* Upgraded to HttpClient 4.2.1.
* Upgraded to tomcat-dbcp 7.0.29.
* Upgraded to Jetty 8.1.5.
* Improved ``AssetServlet``:

  * More accurate ``Last-Modified-At`` timestamps.
  * More general asset specification.
  * Default filename is now configurable.

* Improved ``JacksonMessageBodyProvider``:

  * Now based on Jackson's JAX-RS support.
  * Doesn't read or write types annotated with ``@JsonIgnoreType``.

* Added ``@MinSize``, ``@MaxSize``, and ``@SizeRange`` validations.
* Added ``@MinDuration``, ``@MaxDuration``, and ``@DurationRange`` validations.
* Fixed race conditions in Logback initialization routines.
* Fixed ``TaskServlet`` problems with custom context paths.
* Added ``jersey-text-framework-core`` as an explicit dependency of ``dropwizard-testing``. This
  helps out some non-Maven build frameworks with bugs in dependency processing.
* Added ``addProvider`` to ``JerseyClientFactory``.
* Fixed ``NullPointerException`` problems with anonymous health check classes.
* Added support for serializing/deserializing ``ByteBuffer`` instances as JSON.
* Added ``supportedProtocols`` to SSL configuration, and disabled SSLv2 by default.
* Added support for ``Optional<Integer>`` query parameters and others.
* Removed ``jersey-freemarker`` dependency from ``dropwizard-views``.
* Fixed missing thread contexts in logging statements.
* Made the configuration file argument for the ``server`` command optional.
* Added support for disabling log rotation.
* Added support for arbitrary KeyStore types.
* Added ``Log.forThisClass()``.
* Made explicit service names optional.

.. _rel-0.4.4:

v0.4.4: Jul 24 2012
===================

* Added support for ``@JsonIgnoreType`` to ``JacksonMessageBodyProvider``.

.. _rel-0.4.3:

v0.4.3: Jun 22 2012
===================

* Re-enable immediate flushing for file and console logging appenders.

.. _rel-0.4.2:

v0.4.2: Jun 20 2012
===================

* Fixed ``JsonProcessingExceptionMapper``. Now returns human-readable error messages for malformed
  or invalid JSON as a ``400 Bad Request``. Also handles problems with JSON generation and object
  mapping in a developer-friendly way.

.. _rel-0.4.1:

v0.4.1: Jun 19 2012
===================

* Fixed type parameter resolution in for subclasses of subclasses of ``ConfiguredCommand``.
* Upgraded to Jackson 1.9.7.
* Upgraded to Logback 1.0.6, with asynchronous logging.
* Upgraded to Hibernate Validator 4.3.0.
* Upgraded to JDBI 2.34.
* Upgraded to Jetty 8.1.4.
* Added ``logging.console.format``, ``logging.file.format``, and ``logging.syslog.format``
  parameters for custom log formats.
* Extended ``ResourceTest`` to allow for enabling/disabling specific Jersey features.
* Made ``Configuration`` serializable as JSON.
* Stopped lumping command-line options in a group in ``Command``.
* Fixed ``java.util.logging`` level changes.
* Upgraded to Apache HttpClient 4.2.
* Improved performance of ``AssetServlet``.
* Added ``withBundle`` to ``ScalaService`` to enable bundle mix-ins.
* Upgraded to SLF4J 1.6.6.
* Enabled configuration-parameterized Jersey containers.
* Upgraded to Jackson Guava 1.9.1, with support for ``Optional``.
* Fixed error message in ``AssetBundle``.
* Fixed ``WebApplicationException``s being thrown by ``JerseyClient``.

.. _rel-0.4.0:

v0.4.0: May 1 2012
==================

* Switched logging from Log4j__ to Logback__.

  * Deprecated ``Log#fatal`` methods.
  * Deprecated Log4j usage.
  * Removed Log4j JSON support.
  * Switched file logging to a time-based rotation system with optional GZIP and ZIP compression.
  * Replaced ``logging.file.filenamePattern`` with ``logging.file.currentLogFilename`` and
    ``logging.file.archivedLogFilenamePattern``.
  * Replaced ``logging.file.retainedFileCount`` with ``logging.file.archivedFileCount``.
  * Moved request logging to use a Logback-backed, time-based rotation system with optional GZIP
    and ZIP compression. ``http.requestLog`` now has ``console``, ``file``, and ``syslog``
    sections.

* Fixed validation errors for logging configuration.
* Added ``ResourceTest#addProvider(Class<?>)``.
* Added ``ETag`` and ``Last-Modified`` support to ``AssetServlet``.
* Fixed ``off`` logging levels conflicting with YAML's helpfulness.
* Improved ``Optional`` support for some JDBC drivers.
* Added ``ResourceTest#getJson()``.
* Upgraded to Jackson 1.9.6.
* Improved syslog logging.
* Fixed template paths for views.
* Upgraded to Guava 12.0.
* Added support for deserializing ``CacheBuilderSpec`` instances from JSON/YAML.
* Switched ``AssetsBundle`` and servlet to using cache builder specs.
* Switched ``CachingAuthenticator`` to using cache builder specs.
* Malformed JSON request entities now produce a ``400 Bad Request`` instead of a
  ``500 Server Error`` response.
* Added ``connectionTimeout``, ``maxConnectionsPerRoute``, and ``keepAlive`` to
  ``HttpClientConfiguration``.
* Added support for using Guava's ``HostAndPort`` in configuration properties.
* Upgraded to tomcat-dbcp 7.0.27.
* Upgraded to JDBI 2.33.2.
* Upgraded to HttpClient 4.1.3.
* Upgraded to Metrics 2.1.2.
* Upgraded to Jetty 8.1.3.
* Added SSL support.

.. __: http://logging.apache.org/log4j/1.2/
.. __: http://logback.qos.ch/


.. _rel-0.3.1:

v0.3.1: Mar 15 2012
===================

* Fixed debug logging levels for ``Log``.

.. _rel-0.3.0:

v0.3.0: Mar 13 2012
===================

* Upgraded to JDBI 2.31.3.
* Upgraded to Jackson 1.9.5.
* Upgraded to Jetty 8.1.2. (Jetty 9 is now the experimental branch. Jetty 8 is just Jetty 7 with
  Servlet 3.0 support.)
* Dropped ``dropwizard-templates`` and added ``dropwizard-views`` instead.
* Added ``AbstractParam#getMediaType()``.
* Fixed potential encoding bug in parsing YAML files.
* Fixed a ``NullPointerException`` when getting logging levels via JMX.
* Dropped support for ``@BearerToken`` and added ``dropwizard-auth`` instead.
* Added ``@CacheControl`` for resource methods.
* Added ``AbstractService#getJson()`` for full Jackson customization.
* Fixed formatting of configuration file parsing errors.
* ``ThreadNameFilter`` is now added by default. The thread names Jetty worker threads are set to the
  method and URI of the HTTP request they are currently processing.
* Added command-line overriding of configuration parameters via system properties. For example,
  ``-Ddw.http.port=8090`` will override the configuration file to set ``http.port`` to ``8090``.
* Removed ``ManagedCommand``. It was rarely used and confusing.
* If ``http.adminPort`` is the same as ``http.port``, the admin servlet will be hosted under
  ``/admin``. This allows Dropwizard applications to be deployed to environments like Heroku, which
  require applications to open a single port.
* Added ``http.adminUsername`` and ``http.adminPassword`` to allow for Basic HTTP Authentication
  for the admin servlet.
* Upgraded to `Metrics 2.1.1 <http://metrics.codahale.com/about/release-notes/#v2-1-1-mar-13-2012>`_.

.. _rel-0.2.1:

v0.2.1: Feb 24 2012
===================

* Added ``logging.console.timeZone`` and ``logging.file.timeZone`` to control the time zone of
  the timestamps in the logs. Defaults to UTC.
* Upgraded to Jetty 7.6.1.
* Upgraded to Jersey 1.12.
* Upgraded to Guava 11.0.2.
* Upgraded to SnakeYAML 1.10.
* Upgraded to tomcat-dbcp 7.0.26.
* Upgraded to Metrics 2.0.3.

.. _rel-0.2.0:

v0.2.0: Feb 15 2012
===================

* Switched to using ``jackson-datatype-guava`` for JSON serialization/deserialization of Guava
  types.
* Use ``InstrumentedQueuedThreadPool`` from ``metrics-jetty``.
* Upgraded to Jackson 1.9.4.
* Upgraded to Jetty 7.6.0 final.
* Upgraded to tomcat-dbcp 7.0.25.
* Improved fool-proofing for ``Service`` vs. ``ScalaService``.
* Switched to using Jackson for configuration file parsing. SnakeYAML is used to parse YAML
  configuration files to a JSON intermediary form, then Jackson is used to map that to your
  ``Configuration`` subclass and its fields. Configuration files which don't end in ``.yaml`` or
  ``.yml`` are treated as JSON.
* Rewrote ``Json`` to no longer be a singleton.
* Converted ``JsonHelpers`` in ``dropwizard-testing`` to use normalized JSON strings to compare
  JSON.
* Collapsed ``DatabaseConfiguration``. It's no longer a map of connection names to configuration
  objects.
* Changed ``Database`` to use the validation query in ``DatabaseConfiguration`` for its ``#ping()``
  method.
* Changed many ``HttpConfiguration`` defaults to match Jetty's defaults.
* Upgraded to JDBI 2.31.2.
* Fixed JAR locations in the CLI usage screens.
* Upgraded to Metrics 2.0.2.
* Added support for all servlet listener types.
* Added ``Log#setLevel(Level)``.
* Added ``Service#getJerseyContainer``, which allows services to fully customize the Jersey
  container instance.
* Added the ``http.contextParameters`` configuration parameter.

.. _rel-0.1.3:

v0.1.3: Jan 19 2012
===================

* Upgraded to Guava 11.0.1.
* Fixed logging in ``ServerCommand``. For the last time.
* Switched to using the instrumented connectors from ``metrics-jetty``. This allows for much
  lower-level metrics about your service, including whether or not your thread pools are overloaded.
* Added FindBugs to the build process.
* Added ``ResourceTest`` to ``dropwizard-testing``, which uses the Jersey Test Framework to provide
  full testing of resources.
* Upgraded to Jetty 7.6.0.RC4.
* Decoupled URIs and resource paths in ``AssetServlet`` and ``AssetsBundle``.
* Added ``rootPath`` to ``Configuration``. It allows you to serve Jersey assets off a specific path
  (e.g., ``/resources/*`` vs ``/*``).
* ``AssetServlet`` now looks for ``index.htm`` when handling requests for the root URI.
* Upgraded to Metrics 2.0.0-RC0.

.. _rel-0.1.2:

v0.1.2: Jan 07 2012
===================

* All Jersey resource methods annotated with ``@Timed``, ``@Metered``, or ``@ExceptionMetered`` are
  now instrumented via ``metrics-jersey``.
* Now licensed under Apache License 2.0.
* Upgraded to Jetty 7.6.0.RC3.
* Upgraded to Metrics 2.0.0-BETA19.
* Fixed logging in ``ServerCommand``.
* Made ``ServerCommand#run()`` non-``final``.


.. _rel-0.1.1:

v0.1.1: Dec 28 2011
===================

* Fixed ``ManagedCommand`` to provide access to the ``Environment``, among other things.
* Made ``JerseyClient``'s thread pool managed.
* Improved ease of use for ``Duration`` and ``Size`` configuration parameters.
* Upgraded to Mockito 1.9.0.
* Upgraded to Jetty 7.6.0.RC2.
* Removed single-arg constructors for ``ConfiguredCommand``.
* Added ``Log``, a simple front-end for logging.

.. _rel-0.1.0:


v0.1.0: Dec 21 2011
===================

* Initial release
