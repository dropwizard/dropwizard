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
