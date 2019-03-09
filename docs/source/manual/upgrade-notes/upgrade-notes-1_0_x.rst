.. _upgrade-notes-dropwizard-1_0_x:

##################################
Upgrade Notes for Dropwizard 1.0.x
##################################

Change the project compile and target level to 1.8
==================================================

Dropwizard 1.0.0 is compiled against JDK 1.8 uses its features extensively.
So, to use this version of Dropwizard your project should be compiled and targeted to run on JDK 1.8.

Remove the ``dropwizard-java8`` module
======================================

Support for Java 8 features is now provided out of the box.

Migrate ``dropwizard-spdy`` to ``dropwizard-http2``
===================================================

If you used the SPDY connector, you should use the HTTP/2 integration now.

.. code-block:: yaml

   # - type: spdy3
   - type: h2
     port: 8445
     keyStorePath: example.keystore
     keyStorePassword: example

Replace Guava’s ``Optional`` by ``java.util.Optional`` in Dropwizard public API
===============================================================================

Although Guava’s ``Optional`` should be still supported in your Jersey and JDBI resources,
Dropwizard API now exposes optional results as ``java.util.Optional``.

For example, in authenticators you should change ``Optional.absent`` to ``Optional.empty``.

Migrate your Hibernate resources to Hibernate 5
===============================================

Checkout the `Hibernate 5.0 migration guide <https://github.com/hibernate/hibernate-orm/blob/5.0/migration-guide.adoc>`__

Add missing ``@Valid`` annotations
==================================

In 0.9.x, ``@Validated`` was sufficient to enable validation.
In 1.0.x, it is `necessary <https://github.com/dropwizard/dropwizard/pull/1251#issuecomment-142645734>`__ to include ``@Valid`` as well.
