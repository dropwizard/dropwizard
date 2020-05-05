.. _upgrade-notes-dropwizard-2_0_x:

##################################
Upgrade Notes for Dropwizard 2.0.x
##################################

Dropwizard Bill of Materials (BOM)
==================================

Starting with Dropwizard 2.0.0, the ``io.dropwizard:dropwizard-bom``
artifact only specifies the versions of the official Dropwizard modules
but no transitive dependencies anymore.

If you want to pin the transitive dependencies, you'll have to use the
``io.dropwizard:dropwizard-dependencies`` artifact.

It can be used as parent POM, for which you can override individual
dependency versions by setting certain Maven properties, see
`dropwizard-dependencies/pom.xml <https://github.com/dropwizard/dropwizard/blob/5f4ef68cdc1f42f4b21c018cb364bea9fc7f9827/dropwizard-dependencies/pom.xml#L20-L68>`__
for a complete list.

.. code:: xml

   <parent>
     <groupId>io.dropwizard</groupId>
     <artifactId>dropwizard-dependencies</artifactId>
     <version>2.0.0</version>
   </parent>

   <properties>
     <!-- Use older version of Google Guava -->
     <guava.version>28.0-jre</guava.version>
   </properties>

Alternatively, you can also import it as a regular BOM without the
possibility to override specific transitive dependency versions with a
Maven property:

.. code:: xml

   <dependencyManagement>
     <dependencies>
       <dependency>
         <groupId>io.dropwizard</groupId>
         <artifactId>dropwizard-dependencies</artifactId>
         <version>2.0.0</version>
         <type>pom</type>
         <scope>import</scope>
       </dependency>
     </dependencies>
   </dependencyManagement>

See also: `#2897 <https://github.com/dropwizard/dropwizard/pull/2897>`__

Removed Configuration Options
=============================

The following configuration options have been removed, so Dropwizard
configuration files should no longer use these options

-  ``soLingerTime``: the configuration option would have become a noop
   anyways. See
   `#2490 <https://github.com/dropwizard/dropwizard/pull/2490>`__ for
   more info
-  ``blockingTimeout``: was previously used as an internal jetty
   failsafe mechanism, `and that use case was no longer deemed
   necessary <https://github.com/eclipse/jetty.project/issues/2525>`__.
   If one had previously used ``blockingTimeout`` to discard slow
   clients, please use the new configuration options
   ``minRequestDataPerSecond`` and ``minResponseDataPerSecond``
-  ``minRequestDataRate``: has been renamed to
   ``minRequestDataPerSecond`` and changed from a number to a size like
   "100 bytes"

Jersey
======

Dropwizard has upgraded to Eclipse Jersey 2.29, but it has come at some
migration cost:

If one created a custom provider (eg: parse / write JSON differently, so
a custom ``JacksonJaxbJsonProvider`` is written), you must annotate the
class with the appropriate ``@Consumes`` and ``@Produces`` and register
it with a Jersey ``Feature`` instead of an ``AbstractBinder`` if it been
so previously.

HK2 internal API has been updated, so if you previously had a
``AbstractValueFactoryProvider``, that will need to migrate to a
``AbstractValueParamProvider``

Jersey Reactive Client API was updated to remove ``RxClient``, as rx
capabilities are built into the client. You only need to use
Dropwizard's ``buildRx`` for client when you want a switch the default
to something like rxjava 2's ``Flowable``

Context injection on fields in resource instances
-------------------------------------------------

The given resource class has different behavior in Dropwizard 1.3 and
Dropwizard 2.0 depending on how it is registered.

.. code:: java

   @Path("/")
   @Produces(MediaType.APPLICATION_JSON)
   public class InfoResource {
       @Context
       UriInfo requestUri;

       @GET
       public String getInfo() {
           return requestUri.getRequestUri().toString()
       }
   }

There are two ways to register this resource:

.. code:: java

   @Override
   public void run(InfoConfiguration configuration, Environment environment) {
       // 1. Register an instance of the resource
       environment.jersey().register(new InfoResource());

       // 2. Register the class as a resource
       environment.jersey().register(InfoResource.class);
   }

The first method (registering an instance) will now not work in
Dropwizard 2.0. Migrating resource instances with field context
injections to Dropwizard 2.0 involves pushing the field into a parameter
in the desired endpoint

.. code:: diff

     @Path("/")
     @Produces(MediaType.APPLICATION_JSON)
     public class InfoResource {
   -     @Context
   -     UriInfo requestUri;
     
         @GET
   -     public String getInfo() {
   +     public String getInfo(@Context UriInfo requestUri) {
             return requestUri.getRequestUri().toString()
         }
     }

For more information see
`#2781 <https://github.com/dropwizard/dropwizard/issues/2781>`__

More Secure TLS
===============

Dropwizard 2.0, by default, only allows cipher suites that support
forward secrecy. The only cipher suites newly disabled are those under
the ``TLS_RSA_*`` family. Clients who don't support forward secrecy
(expected to be a small amount) may now find that they can't communicate
with a Dropwizard 2.0 server. If necessary one can override what cipher
suites are blacklisted using the ``excludedCipherSuites`` configuration
option.

Dropwizard 2.0, by default, only supports TLS 1.2. While Dropwizard 1.x
effectively only supported TLS 1.2, due to the supported cipher suites,
one could still conceivably configure their server or receive a client
that could negotiate a TLS 1.0 or 1.1 connection. One can still decide
what TLS protocols are on the blacklist by configuring
``excludedProtocols``

We also hope that in 2.0 it is more clear what protocols and cipher
suites are enabled / disabled, as previously one would see the following
statement logged on startup:

::

   Supported protocols: [SSLv2Hello, SSLv3, TLSv1, TLSv1.1, TLSv1.2]

While not technically wrong, displaying the protocols that *could* be
enabled is misleading as it makes one believe that Dropwizard employs
extremely unsafe defaults. We've reworked what is logged to only the
protocols and cipher suites that Dropwizard *will* expose. And log the
protocols and cipher suites that Dropwizard will reject, and thus could
expose them if configured to do so. So now you'll see the following in
the logs:

::

   Enabled protocols: [TLSv1.2]
   Disabled protocols: [SSLv2Hello, SSLv3, TLSv1, TLSv1.1]

Jackson Changes
===============

``DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES`` is now disabled by
default, so unrecognized fields will now be silently ignored. One can
revert back to the 1.x behavior with:

.. code:: java

   public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
       bootstrap.getObjectMapper().enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
   }

.. _support-for-jdbi-2x-moved-out-of-dropwizard-core-modules:

Support for JDBI 2.x moved out of Dropwizard core modules
=========================================================

The ``dropwizard-jdbi`` module has been moved out of Dropwizard core
modules
(`#2922 <https://github.com/dropwizard/dropwizard/pull/2922>`__).

The reason for this is that JDBI 2.x hasn't been updated since January
2017 and the ``dropwizard-jdbi3`` module, which targets its successor
Jdbi 3.x, still is part of the Dropwizard core modules.

If you want to keep using JDBI 2.x, you can change the Maven coordinates
of ``dropwizard-jdbi`` as follows:

.. code:: xml

   <!-- Old artifact coordinates -->
   <dependency>
       <groupId>io.dropwizard</groupId>
       <artifactId>dropwizard-jdbi</artifactId>
       <version>2.0.0</version>
   </dependency>

.. code:: xml

   <!-- New artifact coordinates -->
   <dependency>
       <groupId>io.dropwizard.modules</groupId>
       <artifactId>dropwizard-jdbi</artifactId>
       <version>2.0.0</version>
   </dependency>


Miscellaneous
=============

Improved validation message for min/max duration
------------------------------------------------

``@MinDuration`` / ``@MaxDuration`` have had their validation messages
improved, so instead of

   messageRate must be less than (or equal to, if in 'inclusive' mode) 1
   MINUTES

one will see if inclusive is true

::

   messageRate must be less than or equal to 1 MINUTES

if inclusive is false:

::

   messageRate must be less than 1 MINUTES

Task execute method
-------------------
The ``parameters`` argument of the ``Task.execute`` method has a slightly different ``Map`` type.
Classes extending the abstract class ``Task`` should therefore change

.. code:: java

        @Override
        public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
        
into

.. code:: java

        @Override
        public void execute(Map<String,List<String>> parameters, PrintWriter output) throws Exception {
