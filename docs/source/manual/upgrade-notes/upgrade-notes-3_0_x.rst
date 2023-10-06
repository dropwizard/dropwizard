.. _upgrade-notes-dropwizard-3_0_x:

##################################
Upgrade Notes for Dropwizard 3.0.x
##################################

Java version changes
====================
As already mentioned in the `Upgrade notes for 2.1.x <https://www.dropwizard.io/en/latest/manual/upgrade-notes/upgrade-notes-2_1_x.html>`_, Dropwizard 3.0.0 drops support for Java versions less than 11.

Jetty 10
========
The main change introduced in Dropwizard 3.0.0 is the upgrade to Jetty 10.0.x. Jetty 10.0.x is built for Java 11, therefore the Java version change was necessary.

This change comes with some migration cost. For detailed information regarding the changes introduced in Jetty 10.0.x, refer to the `Jetty migration guide <https://www.eclipse.org/jetty/documentation/jetty-10/programming-guide/index.html#pg-migration-94-to-10>`_.

The main changes for Dropwizard are the following:

 - the ``addLifeCycleListener(...)`` method was replaced by the ``addEventListener(...)`` method
 - the ``SecureRequestCustomizer`` was changed (see :ref:`SNI host checking <upgrade-notes-dropwizard-3_0_x-sni>`)
 - the option to exclude specific user agents got removed from the ``GzipHandlerFactory``

.. _upgrade-notes-dropwizard-3_0_x-sni:

SNI host checking
-----------------
Jetty 10.0.x introduces stricter SNI host checking. Therefore you may encounter problems when making requests over HTTPS.

To solve this issue, the ``HttpsConnectorFactory`` got the ``disableSniHostCheck`` configuration option, which defaults to ``false`` to enable strict security for an application.
When setting it to ``true``, the SNI host check gets disabled.

Apache HttpClient 5
===================
The Apache HttpClient was updated to version 5.x. This version moves several classes to other packages, moves classes between HttpClient and HttpCore and changes all namespace names.
The new prefixes are ``org.apache.hc.client5`` for the client classes and ``org.apache.hc.core5`` for the core classes.

The most functions from Dropwizard are provided as before, but some changes have to be made:

 - the ``normalizeUri`` setting is removed
 - the ``CredentialsProvider`` is replaced by the ``CredentialsStore``
 - the ``ServiceUnavailableRetryStrategy`` is removed
 - the ``HttpRequestRetryHandler`` is replaced by the ``HttpRequestRetryStrategy``

For more information refer to the `Apache HttpClient 5.X migration guide <https://hc.apache.org/httpcomponents-client-5.2.x/migration-guide/migration-to-classic.html>`_.

Dropwizard Package Structure and JPMS
=====================================

In order to properly support the Java Platform Module System (JPMS), the Java packages in modules must not overlap, or put differently, the packages may not be split into multiple modules.

Dropwizard 3.0.0 won't enable full support for the JPMS. Instead, as a transition step, automatic modules are introduced.

See also: `Java 9 Migration Guide: Split Packages <https://nipafx.dev/java-9-migration-guide/#split-packages>`_

Starting with Dropwizard 3.0.0 some core classes have been moved into distinct and clearly split up packages to clean up overlapping package structure.

Affected packages:

======================  =========================  ================================
Maven module            Old package                New package
======================  =========================  ================================
``dropwizard-core``     ``io.dropwizard``          ``io.drowizard.core``
``dropwizard-logging``  ``io.dropwizard.logging``  ``io.dropwizard.logging.common``
``dropwizard-metrics``  ``io.dropwizard.metrics``  ``io.dropwizard.metrics.common``
``dropwizard-views``    ``io.dropwizard.views``    ``io.dropwizard.views.common``
======================  =========================  ================================

This means that imports of core classes such as ``io.dropwizard.Application`` have to be updated for the new package structure and refer to ``io.dropwizard.core.Application``.

JUnit 4.x support
=================

Support for testing with JUnit 4.x has been moved from `dropwizard-testing` to `dropwizard-testing-junit4 <https://github.com/dropwizard/dropwizard-testing-junit4>`_.

Removal of classes from `dropwizard-util`
=========================================
Many classes from the ``dropwizard-util`` module are now obsolete, since the Java standard library provides replacements for them.

For example the ``Sets`` class provided helper methods for creating ``Set`` instances. Starting from Java 9, this can be done by using ``Set.of(...)``.

Jadira Usertype Core library
============================
Dropwizard previously registered the types from the Jadira Usertype Core library automatically.
In order to align the versions 3.0.x and 4.0.x we will drop support for this library in Dropwizard 3
because the current version of the Jadira Usertype Core library doesn't support Hibernate 6.x, which will be used in Dropwizard 4.0.x.

If you want to continue using this library, you have to set the property ``jadira.usertype.autoRegisterUserTypes`` to ``true`` in your application's database configuration
and add a dependency on the current version of the Usertype Core library.

Logback layout conversion words
===============================
Logback allows to use specific `conversion words <https://logback.qos.ch/manual/layouts.html#conversionWord>`_ in its ``PatternLayout`` to insert information obtained by an instance of a specific ``Converter``.

Previously, Dropwizard has overridden the abbreviated conversion words for exceptions (``ex``, ``xEx`` and ``rEx``) to apply stack trace prefixing with a `!` rather than a tab.

In Dropwizard 3.x these overrides are removed and all exception conversion words work as documented in the Logback manual.
To apply the stack trace prefixing, new conversion words are introduced with a prefix ``dw``. Therefore the following new conversion words can be used:

 - ``dwEx``, ``dwException`` and ``dwThrowable`` instead of ``ex``, ``exception`` and ``throwable``
 - ``dwXEx``, ``dwXException`` and ``dwXThrowable`` instead of ``xEx``, ``xException`` and ``xThrowable``
 - ``dwREx`` and ``dwRootException`` instead of ``rEx`` and ``rootException``

Those newly introduced conversion words work like the Logback ones, except that the first tab is replaced by a `!`.

To simplify the upgrade to Dropwizard 3.x for most users, the default Dropwizard logging layout is modified to use the new Dropwizard specific conversion words.
