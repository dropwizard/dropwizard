.. _release-notes:

#############
Release Notes
#############

.. _rel-1.0.0:

v1.0.0
======

* Using Java 8 as baseline
* ``dropwizard-java8`` bundle merged into mainline `#1365 <https://github.com/dropwizard/dropwizard/issues/1365>`_
* HTTP/2 and server push support `#1349 <https://github.com/dropwizard/dropwizard/issues/1349>`_
* ``dropwizard-spdy`` module is removed in favor of ``dropwizard-http2`` `#1330 <https://github.com/dropwizard/dropwizard/pull/1330>`_
* Switching to ``logback-access`` for HTTP request logging `#1415 <https://github.com/dropwizard/dropwizard/pull/1415>`
* Support for validating return values in JAX-RS resources `#1251 <https://github.com/dropwizard/dropwizard/pull/1251>`_
* Consistent handling null entities in JAX-RS resources `#1251 <https://github.com/dropwizard/dropwizard/pull/1251>`_
* Returning an HTTP 500 error for entities that can't be serialized `#1347 <https://github.com/dropwizard/dropwizard/pull/1347>`_
* Support fallback to the ``toString`` method during deserializing enum values from JSON  `#1340 <https://github.com/dropwizard/dropwizard/pull/1340>`_
* Support for setting default headers in Apache HTTP client `#1354 <https://github.com/dropwizard/dropwizard/pull/1354>`_
* Printing help once on invalid command line arguments `#1376 <https://github.com/dropwizard/dropwizard/pull/1376>`_
* Support for case insensitive and all single letter ``SizeUnit`` suffixes `#1380 <https://github.com/dropwizard/dropwizard/pull/1380>`_
* Added a development profile to the build `#1364 <https://github.com/dropwizard/dropwizard/issues/1364>`_
* All the default exception mappers in ``ResourceTestRule`` are registered by default `#1387 <https://github.com/dropwizard/dropwizard/pull/1387>`_
* Ability to provide own ``RequestLogFactory`` `#1290 <https://github.com/dropwizard/dropwizard/pull/1290>`_
* Support for authentication by polymorphic principals `#1392 <https://github.com/dropwizard/dropwizard/pull/1392>`_
* Support for configuring Jetty's ``inheritedChannel`` option `#1410 <https://github.com/dropwizard/dropwizard/pull/1410>`_
* Support for using ``DropwizardAppRule`` at the suite level `#1411 <https://github.com/dropwizard/dropwizard/pull/1411>`_
* Support for adding multiple ``MigrationBundles`` `#1430 <https://github.com/dropwizard/dropwizard/pull/1430>`_
* Support for obtaining server context paths in the ``Application.run`` method `#1503 <https://github.com/dropwizard/dropwizard/pull/1503>`_
* Upgraded to Apache HTTP Client 4.5.2
* Upgraded to argparse4j 0.7.0
* Upgraded to Guava 19.0
* Upgraded to Hibernate 5.0.7 `#1429 <https://github.com/dropwizard/dropwizard/pull/1429>`_
* Upgraded to Hibernate Validator 5.2.4.Final
* Upgraded to Jadira Usertype Core 5.0.0.GA
* Upgraded to Jackson 2.7.3
* Upgraded to JDBI 2.72 `#1358 <https://github.com/dropwizard/dropwizard/pull/1358>`_
* Upgraded to Jersey 2.22.2
* Upgraded to Jetty 9.3.8.v20160314 `#1330 <https://github.com/dropwizard/dropwizard/pull/1330>`_
* Upgraded to Joda-Time 2.9.2
* Upgraded to Liquibase 3.4.2
* Upgraded to liquibase-slf4j 2.0.0
* Upgraded to Logback 1.1.6
* Upgraded to Mustache 0.9.1
* Upgraded to SLF4J 1.7.18
* Upgraded to tomcat-jdbc 8.0.32
* Upgraded to AssertJ 3.3.0

.. _rel-0.9.2:

v0.9.2: Jan 20 2016
===================

* Support `@UnitOfWork` annotation outside of Jersey resources `#1361 <https://github.com/dropwizard/dropwizard/issues/1361>`_

.. _rel-0.9.1:

v0.9.1: Nov 3 2015
==================

* Add ``ConfigurationSourceProvider`` for reading resources from classpath `#1314 <https://github.com/dropwizard/dropwizard/issues/1314>`_
* Add ``@UnwrapValidatedValue`` annotation to `BaseReporterFactory.frequency` `#1308 <https://github.com/dropwizard/dropwizard/issues/1308>`_, `#1309 <https://github.com/dropwizard/dropwizard/issues/1309>`_
* Fix serialization of default configuration for ``DataSourceFactory`` by deprecating ``PooledDataSourceFactory#getHealthCheckValidationQuery()`` and ``PooledDataSourceFactory#getHealthCheckValidationTimeout()`` `#1321 <https://github.com/dropwizard/dropwizard/issues/1321>`_, `#1322 <https://github.com/dropwizard/dropwizard/pull/1322>`_
* Treat ``null`` values in JAX-RS resource method parameters of type ``Optional<T>`` as absent value after conversion `#1323 <https://github.com/dropwizard/dropwizard/pull/1323>`_

.. _rel-0.9.0:

v0.9.0: Oct 28 2015
===================

* Various documentation fixes and improvements
* New filter-based authorization & authentication `#952 <https://github.com/dropwizard/dropwizard/pull/952>`_, `#1023 <https://github.com/dropwizard/dropwizard/pull/1023>`_, `#1114 <https://github.com/dropwizard/dropwizard/pull/1114>`_, `#1162 <https://github.com/dropwizard/dropwizard/pull/1162>`_, `#1241 <https://github.com/dropwizard/dropwizard/pull/1241>`_
* Fixed a security bug in ``CachingAuthenticator`` with caching results of failed authentication attempts `#1082 <https://github.com/dropwizard/dropwizard/pull/1082>`_
* Correct handling misconfigured context paths in ``ServerFactory`` `#785 <https://github.com/dropwizard/dropwizard/pull/785>`_
* Logging context paths during application startup `#994 <https://github.com/dropwizard/dropwizard/pull/994>`_, `#1072 <https://github.com/dropwizard/dropwizard/pull/1072>`_
* Support for `Jersey Bean Validation <https://jersey.java.net/documentation/latest/bean-validation.html>`_ `#842 <https://github.com/dropwizard/dropwizard/pull/842>`_
* Returning descriptive constraint violation messages `#1039 <https://github.com/dropwizard/dropwizard/pull/1039>`_,
* Trace logging of failed constraint violations `#992 <https://github.com/dropwizard/dropwizard/pull/992>`_
* Returning correct HTTP status codes for constraint violations `#993 <https://github.com/dropwizard/dropwizard/pull/993>`_
* Fixed possible XSS in constraint violations `#892 <https://github.com/dropwizard/dropwizard/issues/892>`_
* Support for including caller data in appenders `#995 <https://github.com/dropwizard/dropwizard/pull/995>`_
* Support for defining custom logging factories (e.g. native Logback) `#996 <https://github.com/dropwizard/dropwizard/pull/996>`_
* Support for defining the maximum log file size in ``FileAppenderFactory``. `#1000 <https://github.com/dropwizard/dropwizard/pull/1000>`_
* Support for fixed window rolling policy in ``FileAppenderFactory`` `#1218 <https://github.com/dropwizard/dropwizard/pull/1218>`_
* Support for individual logger appenders `#1092 <https://github.com/dropwizard/dropwizard/pull/1092>`_
* Support for disabling logger additivity `#1215 <https://github.com/dropwizard/dropwizard/pull/1215>`_
* Sorting endpoints in the application startup log `#1002 <https://github.com/dropwizard/dropwizard/pull/1002>`_
* Dynamic DNS resolution in the Graphite metric reporter `#1004 <https://github.com/dropwizard/dropwizard/pull/1004>`_
* Support for defining a custom ``MetricRegistry`` during bootstrap (e.g. with HdrHistogram) `#1015 <https://github.com/dropwizard/dropwizard/pull/1015>`_
* Support for defining a custom ``ObjectMapper`` during bootstrap. `#1112 <https://github.com/dropwizard/dropwizard/pull/1112>`_
* Added facility to plug-in custom DB connection pools (e.g. HikariCP) `#1030 <https://github.com/dropwizard/dropwizard/pull/1030>`_
* Support for setting a custom DB pool connection validator `#1113 <https://github.com/dropwizard/dropwizard/pull/1113>`_
* Support for enabling of removing abandoned DB pool connections `#1264 <https://github.com/dropwizard/dropwizard/pull/1264>`_
* Support for credentials in a DB data source URL `#1260 <https://github.com/dropwizard/dropwizard/pull/1260>`_
* Support for simultaneous work of several Hibernate bundles `#1276 <https://github.com/dropwizard/dropwizard/pull/1276>`_
* HTTP(S) proxy support for Dropwizard HTTP client `#657 <https://github.com/dropwizard/dropwizard/pull/657>`_
* Support external configuration of TLS properties for Dropwizard HTTP client `#1224 <https://github.com/dropwizard/dropwizard/pull/1224>`_
* Support for not accepting GZIP-compressed responses in HTTP clients `#1270 <https://github.com/dropwizard/dropwizard/pull/1270>`_
* Support for setting a custom redirect strategy in HTTP clients `#1281 <https://github.com/dropwizard/dropwizard/pull/1281>`_
* Apache and Jersey clients are now managed by the application environment `#1061 <https://github.com/dropwizard/dropwizard/pull/1061>`_
* Support for request-scoped configuration for Jersey client  `#939 <https://github.com/dropwizard/dropwizard/pull/939>`_
* Respecting Jackson feature for deserializing enums using ``toString`` `#1104 <https://github.com/dropwizard/dropwizard/pull/1104>`_
* Support for passing explicit ``Configuration`` via test rules `#1131 <https://github.com/dropwizard/dropwizard/pull/1131>`_
* On view template error, return a generic error page instead of template not found `#1178 <https://github.com/dropwizard/dropwizard/pull/1178>`_
* In some cases an instance of Jersey HTTP client could be abruptly closed during the application lifetime `#1232 <https://github.com/dropwizard/dropwizard/pull/1232>`_
* Improved build time build by running tests in parallel `#1032 <https://github.com/dropwizard/dropwizard/pull/1032>`_
* Added JMH benchmarks  `#990 <https://github.com/dropwizard/dropwizard/pull/990>`_
* Allow customization of Hibernate ``SessionFactory`` `#1182 <https://github.com/dropwizard/dropwizard/issue/1182>`_
* Removed javax.el-2.x in favour of javax.el-3.0
* Upgraded to argparse4j 0.6.0
* Upgrade to AssertJ 2.2.0
* Upgraded to JDBI 2.63.1
* Upgraded to Apache HTTP Client 4.5.1
* Upgraded to Dropwizard Metrics 3.1.2
* Upgraded to Freemarker 2.3.23
* Upgraded to H2 1.4.190
* Upgraded to Hibernate 4.3.11.Final
* Upgraded to Jackson 2.6.3
* Upgraded to Jadira Usertype Core 4.0.0.GA
* Upgraded to Jersey 2.22.1
* Upgraded to Jetty 9.2.13.v20150730
* Upgraded to Joda-Time 2.9
* Upgraded to JSR305 annotations 3.0.1
* Upgraded to Hibernate Validator 5.2.2.Final
* Upgraded to Jetty ALPN boot 7.1.3.v20150130
* Upgraded to Jetty SetUID support 1.0.3
* Upgraded to Liquibase 3.4.1
* Upgraded to Logback 1.1.3
* Upgraded to Metrics 3.1.2
* Upgraded to Mockito 1.10.19
* Upgraded to SLF4J 1.7.12
* Upgraded to commons-lang3 3.4
* Upgraded to tomcat-jdbc 8.0.28

.. _rel-0.8.5:

v0.8.5: Nov 3 2015
==================

* Treat ``null`` values in JAX-RS resource method parameters of type ``Optional<T>`` as absent value after conversion `#1323 <https://github.com/dropwizard/dropwizard/pull/1323>`_

.. _rel-0.8.4:

v0.8.4: Aug 26 2015
===================

* Upgrade to Apache HTTP Client 4.5
* Upgrade to Jersey 2.21
* Fixed user-agent shadowing in Jersey HTTP Client `#1198 <https://github.com/dropwizard/dropwizard/pull/1198>`_

.. _rel-0.8.3:

v0.8.3: Aug 24 2015
===================
* Fixed an issue with closing the HTTP client connection pool after a full GC `#1160 <https://github.com/dropwizard/dropwizard/pull/1160>`_

.. _rel-0.8.2:

v0.8.2: Jul 6 2015
==================

* Support for request-scoped configuration for Jersey client `#1137 <https://github.com/dropwizard/dropwizard/pull/1137>`_
* Upgraded to Jersey 2.19 `#1143 <https://github.com/dropwizard/dropwizard/pull/1143>`_

.. _rel-0.8.1:

v0.8.1: Apr 7 2015
==================

* Fixed transaction committing lifecycle for ``@UnitOfWork``  (#850, #915)
* Fixed noisy Logback messages on startup (#902)
* Ability to use providers in TestRule, allows testing of auth & views (#513, #922)
* Custom ExceptionMapper not invoked when Hibernate rollback (#949)
* Support for setting a time bound on DBI and Hibernate health checks
* Default configuration for views
* Ensure that JerseyRequest scoped ClientConfig gets propagated to HttpUriRequest
* More example tests
* Fixed security issue where info is leaked during validation of unauthenticated resources(#768)

.. _rel-0.8.0:

v0.8.0: Mar 5 2015
==================

* Migrated ``dropwizard-spdy`` from NPN to ALPN
* Dropped support for deprecated SPDY/2 in ``dropwizard-spdy``
* Upgrade to argparse4j 0.4.4
* Upgrade to commons-lang3 3.3.2
* Upgrade to Guava 18.0
* Upgrade to H2 1.4.185
* Upgrade to Hibernate 4.3.5.Final
* Upgrade to Hibernate Validator 5.1.3.Final
* Upgrade to Jackson 2.5.1
* Upgrade to JDBI 2.59
* Upgrade to Jersey 2.16
* Upgrade to Jetty 9.2.9.v20150224
* Upgrade to Joda-Time 2.7
* Upgrade to Liquibase 3.3.2
* Upgrade to Mustache 0.8.16
* Upgrade to SLF4J 1.7.10
* Upgrade to tomcat-jdbc 8.0.18
* Upgrade to JSR305 annotations 3.0.0
* Upgrade to Junit 4.12
* Upgrade to AssertJ 1.7.1
* Upgrade to Mockito 1.10.17
* Support for range headers
* Ability to use Apache client configuration for Jersey client
* Warning when maximum pool size and unbounded queues are combined
* Fixed connection leak in CloseableLiquibase
* Support ScheduledExecutorService with daemon thread
* Improved DropwizardAppRule
* Better connection pool metrics
* Removed final modifier from Application#run
* Fixed gzip encoding to support Jersey 2.x
* Configuration to toggle regex [in/ex]clusion for Metrics
* Configuration to disable default exception mappers
* Configuration support for disabling chunked encoding
* Documentation fixes and upgrades


.. _rel-0.7.1:

v0.7.1: Jun 18 2014
===================

* Added instrumentation to ``Task``, using metrics annotations.
* Added ability to blacklist SSL cipher suites.
* Added ``@PATCH`` annotation for Jersey resource methods to indicate use of the HTTP ``PATCH`` method.
* Added support for configurable request retry behavior for ``HttpClientBuilder`` and ``JerseyClientBuilder``.
* Added facility to get the admin HTTP port in ``DropwizardAppTestRule``.
* Added ``ScanningHibernateBundle``, which scans packages for entities, instead of requiring you to add them individually.
* Added facility to invalidate credentials from the ``CachingAuthenticator`` that match a specified ``Predicate``.
* Added a CI build profile for JDK 8 to ensure that Dropwizard builds against the latest version of the JDK.
* Added ``--catalog`` and ``--schema`` options to Liquibase.
* Added ``stackTracePrefix`` configuration option to ``SyslogAppenderFactory`` to configure the pattern prepended to each line in the stack-trace sent to syslog. Defaults to the TAB character, "\t". Note: this is different from the bang prepended to text logs (such as "console", and "file"), as syslog has different conventions for multi-line messages.
* Added ability to validate ``Optional`` values using validation annotations. Such values require the ``@UnwrapValidatedValue`` annotation, in addition to the validations you wish to use.
* Added facility to configure the ``User-Agent`` for ``HttpClient``. Configurable via the ``userAgent`` configuration option.
* Added configurable ``AllowedMethodsFilter``. Configure allowed HTTP methods for both the application and admin connectors with ``allowedMethods``.
* Added support for specifying a ``CredentialProvider`` for HTTP clients.
* Fixed silently overriding Servlets or ServletFilters; registering a duplicate will now emit a warning.
* Fixed ``SyslogAppenderFactory`` failing when the application name contains a PCRE reserved character (e.g. ``/`` or ``$``).
* Fixed regression causing JMX reporting of metrics to not be enabled by default.
* Fixed transitive dependencies on log4j and extraneous sl4j backends bleeding in to projects. Dropwizard will now enforce that only Logback and slf4j-logback are used everywhere.
* Fixed clients disconnecting before the request has been fully received causing a "500 Internal Server Error" to be generated for the request log. Such situations will now correctly generate a "400 Bad Request", as the request is malformed. Clients will never see these responses, but they matter for logging and metrics that were previously considering this situation as a server error.
* Fixed ``DiscoverableSubtypeResolver`` using the system ``ClassLoader``, instead of the local one.
* Fixed regression causing Liquibase ``--dump`` to fail to dump the database.
* Fixed the CSV metrics reporter failing when the output directory doesn't exist. It will now attempt to create the directory on startup.
* Fixed global frequency for metrics reporters being permanently overridden by the default frequency for individual reporters.
* Fixed tests failing on Windows due to platform-specific line separators.
* Changed ``DropwizardAppTestRule`` so that it no longer requires a configuration path to operate. When no path is specified, it will now use the applications' default configuration.
* Changed ``Bootstrap`` so that ``getMetricsFactory()`` may now be overridden to provide a custom instance to the framework to use.
* Upgraded to Guava 17.0
  Note: this addresses a bug with BloomFilters that is incompatible with pre-17.0 BloomFilters.
* Upgraded to Jackson 2.3.3
* Upgraded to Apache HttpClient 4.3.4
* Upgraded to Metrics 3.0.2
* Upgraded to Logback 1.1.2
* Upgraded to h2 1.4.178
* Upgraded to JDBI 2.55
* Upgraded to Hibernate 4.3.5 Final
* Upgraded to Hibernate Validator 5.1.1 Final
* Upgraded to Mustache 0.8.15

.. _rel-0.7.0:

v0.7.0: Apr 04 2014
===================

* Upgraded to Java 7.
* Moved to the ``io.dropwizard`` group ID and namespace.
* Extracted out a number of reusable libraries: ``dropwizard-configuration``,
  ``dropwizard-jackson``, ``dropwizard-jersey``, ``dropwizard-jetty``, ``dropwizard-lifecycle``,
  ``dropwizard-logging``, ``dropwizard-servlets``, ``dropwizard-util``, ``dropwizard-validation``.
* Extracted out various elements of ``Environment`` to separate classes: ``JerseyEnvironment``,
  ``LifecycleEnvironment``, etc.
* Extracted out ``dropwizard-views-freemarker`` and ``dropwizard-views-mustache``.
  ``dropwizard-views`` just provides infrastructure now.
* Renamed ``Service`` to ``Application``.
* Added ``dropwizard-forms``, which provides support for multipart MIME entities.
* Added ``dropwizard-spdy``.
* Added ``AppenderFactory``, allowing for arbitrary logging appenders for application and request
  logs.
* Added ``ConnectorFactory``, allowing for arbitrary Jetty connectors.
* Added ``ServerFactory``, with multi- and single-connector implementations.
* Added ``ReporterFactory``, for metrics reporters, with Graphite and Ganglia implementations.
* Added ``ConfigurationSourceProvider`` to allow loading configuration files from sources other than
  the filesystem.
* Added setuid support. Configure the user/group to run as and soft/hard open file limits in the
  ``ServerFactory``. To bind to privileged ports (e.g. 80), enable ``startsAsRoot`` and set ``user``
  and ``group``, then start your application as the root user.
* Added builders for managed executors.
* Added a default ``check`` command, which loads and validates the service configuration.
* Added support for the Jersey HTTP client to ``dropwizard-client``.
* Added Jackson Afterburner support.
* Added support for ``deflate``-encoded requests and responses.
* Added support for HTTP Sessions. Add the annotated parameter to your resource method:
  ``@Session HttpSession session`` to have the session context injected.
* Added support for a "flash" message to be propagated across requests. Add the annotated parameter
  to your resource method: ``@Session Flash message`` to have any existing flash message injected.
* Added support for deserializing Java ``enums`` with fuzzy matching rules (i.e., whitespace
  stripping, ``-``/``_`` equivalence, case insensitivity, etc.).
* Added ``HibernateBundle#configure(Configuration)`` for customization of Hibernate configuration.
* Added support for Joda Time ``DateTime`` arguments and results when using JDBI.
* Added configuration option to include Exception stack-traces when logging to syslog. Stack traces
  are now excluded by default.
* Added the application name and PID (if detectable) to the beginning of syslog messages, as is the
  convention.
* Added ``--migrations`` command-line option to ``migrate`` command to supply the migrations
  file explicitly.
* Validation errors are now returned as ``application/json`` responses.
* Simplified ``AsyncRequestLog``; now standardized on Jetty 9 NCSA format.
* Renamed ``DatabaseConfiguration`` to ``DataSourceFactory``, and ``ConfigurationStrategy`` to
  ``DatabaseConfiguration``.
* Changed logging to be asynchronous. Messages are now buffered and batched in-memory before being
  delivered to the configured appender(s).
* Changed handling of runtime configuration errors. Will no longer display an Exception stack-trace
  and will present a more useful description of the problem, including suggestions when appropriate.
* Changed error handling to depend more heavily on Jersey exception mapping.
* Changed ``dropwizard-db`` to use ``tomcat-jdbc`` instead of ``tomcat-dbcp``.
* Changed default formatting when logging nested Exceptions to display the root-cause first.
* Replaced ``ResourceTest`` with ``ResourceTestRule``, a JUnit ``TestRule``.
* Dropped Scala support.
* Dropped ``ManagedSessionFactory``.
* Dropped ``ObjectMapperFactory``; use ``ObjectMapper`` instead.
* Dropped ``Validator``; use ``javax.validation.Validator`` instead.
* Fixed a shutdown bug in ``dropwizard-migrations``.
* Fixed formatting of "Caused by" lines not being prefixed when logging nested Exceptions.
* Fixed not all available Jersey endpoints were being logged at startup.
* Upgraded to argparse4j 0.4.3.
* Upgraded to Guava 16.0.1.
* Upgraded to Hibernate Validator 5.0.2.
* Upgraded to Jackson 2.3.1.
* Upgraded to JDBI 2.53.
* Upgraded to Jetty 9.0.7.
* Upgraded to Liquibase 3.1.1.
* Upgraded to Logback 1.1.1.
* Upgraded to Metrics 3.0.1.
* Upgraded to Mustache 0.8.14.
* Upgraded to SLF4J 1.7.6.
* Upgraded to Jersey 1.18.
* Upgraded to Apache HttpClient 4.3.2.
* Upgraded to tomcat-jdbc 7.0.50.
* Upgraded to Hibernate 4.3.1.Final.

.. _rel-0.6.2:

v0.6.2: Mar 18 2013
===================

* Added support for non-UTF8 views.
* Fixed an NPE for services in the root package.
* Fixed exception handling in ``TaskServlet``.
* Upgraded to Slf4j 1.7.4.
* Upgraded to Jetty 8.1.10.
* Upgraded to Jersey 1.17.1.
* Upgraded to Jackson 2.1.4.
* Upgraded to Logback 1.0.10.
* Upgraded to Hibernate 4.1.9.
* Upgraded to Hibernate Validator 4.3.1.
* Upgraded to tomcat-dbcp 7.0.37.
* Upgraded to Mustache.java 0.8.10.
* Upgraded to Apache HttpClient 4.2.3.
* Upgraded to Jackson 2.1.3.
* Upgraded to argparse4j 0.4.0.
* Upgraded to Guava 14.0.1.
* Upgraded to Joda Time 2.2.
* Added ``retries`` to ``HttpClientConfiguration``.
* Fixed log formatting for extended stack traces, also now using extended stack traces as the
  default.
* Upgraded to FEST Assert 2.0M10.

.. _rel-0.6.1:

v0.6.1: Nov 28 2012
===================

* Fixed incorrect latencies in request logs on Linux.
* Added ability to register multiple ``ServerLifecycleListener`` instances.

.. _rel-0.6.0:

v0.6.0: Nov 26 2012
===================

* Added Hibernate support in ``dropwizard-hibernate``.
* Added Liquibase migrations in ``dropwizard-migrations``.
* Renamed ``http.acceptorThreadCount`` to ``http.acceptorThreads``.
* Renamed ``ssl.keyStorePath`` to ``ssl.keyStore``.
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
* Added ``UUIDParam``.
* Upgraded to Metrics 2.2.0.
* Upgraded to Jetty 8.1.8.
* Upgraded to Mockito 1.9.5.
* Upgraded to tomcat-dbcp 7.0.33.
* Upgraded to Mustache 0.8.8.
* Upgraded to Jersey 1.15.
* Upgraded to Apache HttpClient 4.2.2.
* Upgraded to JDBI 2.41.
* Upgraded to Logback 1.0.7 and SLF4J 1.7.2.
* Upgraded to Guava 13.0.1.
* Upgraded to Jackson 2.1.1.
* Added support for Joda Time.

.. note:: Upgrading to 0.6.0 will require changing your code. First, your ``Service`` subclass will
          need to implement both ``#initialize(Bootstrap<T>)`` **and**
          ``#run(T, Environment)``. What used to be in ``initialize`` should be moved to ``run``.
          Second, your representation classes need to be migrated to Jackson 2. For the most part,
          this is just changing imports to ``com.fasterxml.jackson.annotation.*``, but there are
          `some subtler changes in functionality <http://wiki.fasterxml.com/JacksonUpgradeFrom19To20>`_.
          Finally, references to 0.5.x's ``Json``, ``JerseyClient``, or ``JDBI`` classes should be
          changed to Jackon's ``ObjectMapper``, Jersey's ``Client``, and JDBI's ``DBI``
          respectively.

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
