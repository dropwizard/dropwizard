.. _upgrade-notes-dropwizard-2_1_x:

##################################
Upgrade Notes for Dropwizard 2.1.x
##################################

Hibernate Validator Expression Language overhaul
================================================

The Expression Language is now disabled by default for custom violations.

The ``@SelfValidating`` feature has been a source of various security vulnerabilities in Dropwizard:

* `CVE-2020-5245 <https://github.com/dropwizard/dropwizard/security/advisories/GHSA-3mcp-9wr4-cjqf>`_
* `CVE-2020-11002 <https://github.com/dropwizard/dropwizard/security/advisories/GHSA-8jpx-m2wh-2v34>`_

This originally lead to the introduction of `SelfValidating#escapeExpressions() <https://javadoc.io/static/io.dropwizard/dropwizard-project/2.0.3/io/dropwizard/validation/selfvalidating/SelfValidating.html#escapeExpressions-->`_.

Due to some changes regarding the interpolation of messages in custom violations in Hibernate Validator 6.2.0.Final and later, this flag is not required anymore and has been removed in Dropwizard 2.1.0.

While strongly discouraged, you can enable EL interpolation in custom violations with Hibernate Validator, by customizing the ``HibernateValidatorConfiguration`` created by ``Validators#newConfiguration()`` and use the Hibernate Validator instance created from it in your application via ``Environment#setValidator(Validator)``.

Details about these changes in Hibernate Validator can be found at:

* `Hibernate Validator 6.2.0.Final - 12.9. Enabling Expression Language features <https://docs.jboss.org/hibernate/validator/6.2/reference/en-US/html_single/#el-features>`_
* `HV-1816: Disable Expression Language by default for custom constraint violations <https://hibernate.atlassian.net/browse/HV-1816>`_
* `hibernate/hibernate-validator#1138 <https://github.com/hibernate/hibernate-validator/pull/1138>`_


.. _upgrade-notes-dropwizard-2_1_x-health:

Migrating from dropwizard-health
================================

Migrating from the HealthBundle
-------------------------------
`dropwizard/dropwizard-health module <https://github.com/dropwizard/dropwizard-health>`_
Starting in Dropwizard 2.1, the functionality from the external dropwizard/dropwizard-health module has been integrated into the main dropwizard/dropwizard project.

Perform the following steps to migrate your application:

* Remove the HealthBundle from your Application's initialize() method.
* Remove the reference to HealthConfiguration from your application's Configuration class.
* Migrate YAML configuration previously associated with your HealthConfiguration field to instead be nested under a top-level health field (at the same level as server, logging, and metrics).
* Review the new and changed configuration options mentioned below.
* Ensure that you don't already have a custom configuration class member using the health name or rename it to something else or nest it under another field in order not to conflict with the new top-level field.
* Remove the dependency on the io.dropwizard.modules:dropwizard-health library from your application's pom.xml file (or other build dependency definition file).

New Configuration Options
-------------------------
* ``enabled`` flag added to turn on/off health functionality (defaults to ``true``).
* ``name`` configuration added, used in metric naming, thread naming, and log messages.
* ``responder`` factory added, to control how health check requests are responded to. For more info, see :ref:`the config reference <man-configuration-health-responder>`.
* ``responseProvider`` factory added, to control the body of health check responses, separate from the mechanics of responding. For more info, see :ref:`the config reference <man-configuration-health-responseprovider>`.

Changed Configuration Options
-----------------------------
* ``delayedShutdownHandlerEnabled`` default value changed to ``false``.
* ``servlet`` removed in favor of the new ``responder`` and ``responseProvider`` factories.

.. _upgrade-notes-dropwizard-2_1_x-breaking-changes:

Changes in versioning
=====================
Although Dropwizard tries to postpone big changes to major releases, some breaking changes had to be introduced into Dropwizard 2.1. This change is necessary due to new versioning of Dropwizard releases.
The Dropwizard 2.x releases will stay on a Java 8 baseline and the ``javax`` namespace.
Dropwizard 3.x will stay on the ``javax`` namespace too, but will drop support for Java 8 and upgrade to Java 11 instead.
Dropwizard 4.x will eventually mirror the 3.x versions on the ``jakarta`` namespace and maybe introduce some more changes.

Therefore major updates for the Java 8 baseline have to be brought to minor releases on the 2.x branch. The major changes introduced in 2.1 are the following:

.. include:: <isonum.txt>

=================== ====================
Library             Version change
=================== ====================
argparse            0.8.x |rarr| 0.9.x

Hibernate Validator 6.1.x |rarr| 6.2.x

Jackson             2.10.x |rarr| 2.13.x

Jersey              2.33 |rarr| 2.35

Liquibase           3.10.x |rarr| 4.9.x

Dropwizard Metrics  4.1.x |rarr| 4.2.x
=================== ====================

Upgrade to Liquibase 4.x
------------------------
Most of the updates come with low migration cost, but Liquibase gets a major version upgrade and needs some attention.

Liquibase 4.x changes the way it finds files. This means previously recognized migration files could be reported as missing.
Liquibase lets users of the library specify paths, where it should search for files.
Dropwizard therefore adds the file system specific roots to these ``root paths``, as well as the code location (of the current JAR).

This essentially means migration files now must be specified with absolute paths or be located under ``src/main/resources`` and specified relative to that path.

Upgrade to Jersey 2.35
----------------------
The upgrade of Jersey from version 2.33 to 2.35 introduces a behavior change in the handling of ``Optional<T>`` parameters.
If such a parameter is invalid, now a status code ``404`` is returned instead of the former ``400`` status code.

Jackson Blackbird as default
============================

Dropwizard is now registering the `Jackson Blackbird`_ module.
This is the recommended setup for Java 9 and later.

If the `Jackson Afterburner`_ module is on the class path, it will be preferred over the `Jackson Blackbird`_ module.

.. code-block:: xml

    <dependency>
      <groupId>com.fasterxml.jackson.module</groupId>
      <artifactId>jackson-module-afterburner</artifactId>
      <!-- Unnecessary when using dropwizard-dependencies -->
      <version>${jackson.version}</version>
    </dependency>

.. _Jackson Blackbird: https://github.com/FasterXML/jackson-modules-base/tree/jackson-modules-base-2.13.3/blackbird#readme
.. _Jackson Afterburner: https://github.com/FasterXML/jackson-modules-base/tree/jackson-modules-base-2.13.3/afterburner#readme

Modification of the client in ``DropwizardAppExtension``
========================================================

The ``DropwizardAppExtension`` previously obtained a Jersey client configured with the default ``HttpUrlConnectorProvider``.
To support HTTP methods, which are not implemented in the ``HttpURLConnection`` class, workarounds are enabled.
These workarounds modify the JDK classes to support the requested method.
Starting from Java 16, it is no longer possible to modify JDK classes. Therefore workarounds can cause problems on Java 16+.

Dropwizard 2.1.1 changes the Jersey client returned by the ``DropwizardAppExtension#client()`` method.
Now the ``GrizzlyConnectorProvider`` is registered by default to use the Grizzly Async Http Client instead of the ``HttpURLConnection``.
Using the Grizzly client should cause no problems when migrating to Dropwizard 2.1.1.

However, if you are experiencing issues with the client modifications, you can easily instantiate a client without the new connector:

.. code-block:: java

    new JerseyClientBuilder()
        .register(new JacksonFeature(getObjectMapper()))
        .property(ClientProperties.CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT_MS)
        .property(ClientProperties.READ_TIMEOUT, DEFAULT_READ_TIMEOUT_MS)
        .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
        .build();

The property ``HttpUrlConnectorProvider.SET_METHOD_WORKAROUND`` is optional and should be avoided on Java versions after Java 15.

Allow parameter conversion exceptions to be mapped to responses by exception mappers
====================================================================================

Some parameter validation logic has been fixed to allow for custom remapping of error messages. As a result, the API responses may be a little different than before in certain scenarios.

The responses may be different than before this change if the default exception mappers are disabled, or custom exception mappers are registered:

* If the default exception mappers provided by Dropwizard are disabled (``registerDefaultExceptionMappers`` option is off) and no other exception mappers are configured, then the response will have the same status code as before this change, but with no entity or media type.
* If an exception mapper is registered which takes precedence over ``LoggingExceptionMapper`` for mapping ``WebApplicationException`` instances, then the response will be generated by that exception mapper. The ``WebApplicationException`` instance received by the exception mapper will have the same status code and message as the response had before this change.

In addition, the ``AbstractParam.mediaType()`` method has been deprecated, and the responses may not use the media type returned by that method.

There are two types of parameters that may result in different error responses in the above situations if there is a validation error:

* Enum parameters
* Parameters that are any subclass of ``AbstractParam`` (Dropwizard provides the subclasses ``InstantParam``, ``InstantSecondParam``, ``LocalDateParam``, ``LocalDateTimeParam``, ``LocalTimeParam``, ``OffsetDateTimeParam``, ``YearMonthParam``, ``YearParam``, ``ZonedDateTimeParam``, ``ZoneIdParam``, ``BooleanParam``, ``DateTimeParam``, ``DurationParam``, ``InstantParam``, ``IntParam``, ``LocalDateParam``, ``LongParam``, ``NonEmptyStringParam``, ``SizeParam``, and ``UUIDParam``)

If this code change causes an undesired change in the error responses:

* If the error responses are not being mapped to a response by any exception mapper, then either register the default exception mappers (by enabling the ``registerDefaultExceptionMappers`` option) or register Dropwizard's ``LoggingExceptionMapper`` to restore the previous behavior, or register your own custom exception mapper that maps ``WebApplicationException`` instances.
* If you have a custom subclass of ``AbstractParam`` which overrides ``AbstractParam.mediaType()``, then the media type should be set in the exception mapper which maps ``WebApplicationException`` instances instead. It is also possible to override ``AbstractParam.error(String input, Exception e)`` to return a response with an entity and the correct media type directly.

See `PR #5691 <https://github.com/dropwizard/dropwizard/pull/5691>`_ for more details about this change.
