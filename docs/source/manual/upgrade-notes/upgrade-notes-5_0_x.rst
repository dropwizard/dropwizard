.. _upgrade-notes-dropwizard-5_0_x:

##################################
Upgrade Notes for Dropwizard 5.0.x
##################################

Java version changes
====================
Jetty 12 drops support for Java versions older than Java 17.
To avoid version conflicts, we've also adapted their Java version change and adjusted our Java baseline to Java 17.

**You must upgrade to Java 17 or higher to use Dropwizard 5.0.x.**

Dependency Upgrades
===================

Dropwizard 5.0.x includes major dependency upgrades across the board. Here's a summary of the key version changes:

=================== ===================== =====================
Library             4.0.x Version         5.0.x Version
=================== ===================== =====================
argparse4j          0.8.x                 0.9.x
Caffeine            2.8.x                 3.2.x
Guava               31.x                  33.4.x-jre
Hibernate           6.1.x                 6.6.x
Hibernate Validator 7.x                   8.0.x
Jackson             2.15.x                2.20.x
Jersey              3.0.x                 3.1.x
Jetty               11.x                  12.1.x
JUnit               5.9.x                 5.13.x
Liquibase           4.20.x                4.33.x
Logback             1.4.x                 1.5.x
SLF4J               2.0.x                 2.0.17
All Jakarta EE APIs 9.x                   10.x
=================== ===================== =====================

Jakarta component updates
=========================
Dropwizard 5.0.x will be compatible with Jakarta EE 10.
Therefore all Jakarta EE dependencies have been upgraded to their new versions in the Jakarta EE 10 baseline.
The API spec versions for the Jakarta EE 10 baseline can be found in the `Jakarta EE 10 product requirements <https://jakarta.ee/specifications/platform/10/jakarta-platform-spec-10.0#a3252>`_.

Notable changes
===============

Virtual thread update
---------------------
In Dropwizard 3.x and 4.x basic support for virtual threads was added.
The implementation used virtual threads for Jetty's internal thread pools, which is an anti-pattern.
Dropwizard 5.x corrects this behavior.
Now platform threads are used for the thread pools and a virtual thread executor is provided to Jetty's ``AdaptiveExecutionStrategy``, so that virtual threads can be utilized for task execution.

Jackson 2.20.x upgrade
----------------------
Jackson has been upgraded from 2.15.x to 2.20.x. This is a significant upgrade that includes:

* Performance improvements and optimizations
* Enhanced support for Java 17+ features
* Updated databind, core, and annotations modules
* See the `Jackson release notes <https://github.com/FasterXML/jackson/wiki>`_ for detailed changes

Most applications should not require code changes, but custom serializers or deserializers may need review.

JUnit 5.13.x upgrade
--------------------
JUnit has been upgraded from 5.9.x to 5.13.x. Key improvements include:

* Better integration with modern Java versions
* Enhanced parallel test execution
* Improved Jupiter API features
* Updated test engines and extensions

If your tests use JUnit 4 APIs, ensure you've migrated to JUnit 5 or include appropriate compatibility dependencies.

Hibernate 6.6.x upgrade
-----------------------
Hibernate has been upgraded from 6.1.x to 6.6.x. Notable changes include:

* Continued improvements to the type system
* Performance enhancements
* Bug fixes and stability improvements
* Enhanced Jakarta Persistence 3.1 support

For applications using Dropwizard's Hibernate integration, most changes should be transparent. However, if you use Hibernate APIs directly, review the `Hibernate migration guide <https://docs.jboss.org/hibernate/orm/6.6/migration-guide/migration-guide.html>`_.

Liquibase 4.33.x upgrade
------------------------
Liquibase has been upgraded from 4.20.x to 4.33.x. This includes:

* Enhanced change log parsing and validation
* Improved database support and compatibility
* Performance optimizations
* Bug fixes and stability improvements

Review your Liquibase change logs for any deprecated features. See the `Liquibase changelog <https://docs.liquibase.com/start/release-notes/home.html>`_ for detailed information.

Jetty 12.x update
-----------------
The implementation of Jetty 12 made huge changes to the Jetty core.
From now on, the servlet components aren't part of the Jetty core anymore, but can be pulled in by separate modules.
This provides the possibility to use different servlet API versions with the same Jetty version.
Dropwizard manages the current compatible EE components.
For Dropwizard 5.0.x this is done by managing the ``jetty-ee10-bom`` which particularly manages the ``jetty-ee10-servlet`` artifact.

The most important change of Jetty 12 is the signature update of the ``Handler#handle(...)`` method from

.. code-block:: java

    void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException

to

.. code-block:: java

    boolean handle(Request request, Response response, Callback callback) throws Exception

The *handled* state isn't set via ``request.setHandled(boolean)`` anymore but via the return value of the ``handle`` method.
The other notable change is the ``Callback`` object, which should be completed or aborted if and only if the request is handled by this handler.

GZIP status codes
^^^^^^^^^^^^^^^^^
If invalid GZIP bytes are provided to Jetty, an HTTP response with a status code 500 is returned to the user.
Dropwizard catches these errors and returns a response with a status code 400 to indicate a client error rather than a server error.

Since GZIP handling is done in a Jetty handler and not on servlet level, the implementation has to be updated to work with servlet and non-servlet use cases.
If you previously manually used the ``ZipExceptionHandlingGzipHandler``, you may now also want to register the ``ZipExceptionHandlingServletFilter`` to re-enable the status code rewriting in servlet environments.

Unix domain socket connector
^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Dropwizard now supports a new connector of the type ``unix-socket``. For usage information refer to the :ref:`Configuration guide on unix sockets <man-configuration-unix-socket>`.

Removal of the server push functionality
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Jetty previously deprecated the ``PushCacheFilter`` because it was used for a deprecated HTTP feature.
Jetty 12 finally removed the server push functionality.
Therefore Dropwizard 5.0.x removes the configuration classes for this feature.

Removal of ``server.maxQueuedRequests``
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The configuration setting ``server.maxQueuedRequests`` has been removed without replacement.

See `Jetty Threading Architecture <https://jetty.org/docs/jetty/12/programming-guide/arch/threads.html#thread-pool>`_ for details.

Jersey 3.1.x update
-------------------
Jersey now treats HK2 binders as providers. This changes the semantics of binders in Jersey and HK2.

Request logging
---------------
The request logging through ``logback-access`` had its quirks for some time, so Dropwizard provided a workaround for it.
The new ``logback-access`` implementation for Jetty 12 provides a request wrapper that builds a ``HttpServletRequest`` from a Jetty ``Request`` only for some 'relevant' methods.
Dropwizard provides a custom workaround which resolves the servlet context and uses the current active ``HttpServletRequest`` for request logging.
This allows the use of all methods of the ``HttpServletRequest`` and should be more stable for the next servlet API updates.
