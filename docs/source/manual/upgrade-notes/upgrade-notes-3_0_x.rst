.. _upgrade-notes-dropwizard-3_0_x:

##################################
Upgrade Notes for Dropwizard 3.0.x
##################################

Dropwizard Package Structure and JPMS
=====================================

In order to properly support the Java Platform Module System (JPMS), the Java packages in modules must not overlap, or put differently, the packages may not be split into multiple modules.

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
