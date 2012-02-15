.. _release-notes:

#############
Release Notes
#############

.. _rel-0.2.0:

v0.2.0: TBD
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
* Added support for ``ServletContextListener`` instances.
* Added ``Log#setLevel(Level)``.

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
