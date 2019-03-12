.. _upgrade-notes-dropwizard-2_0_x:

##################################
Upgrade Notes for Dropwizard 2.0.x
##################################

Removed Configuration Options
=============================

The following configuration options have been removed, so Dropwizard configuration files should no longer use these options

-  ``soLingerTime``: the configuration option would have become a noop
   anyways. See :pr:`2490` for more info
-  ``blockingTimeout``: was previously used as an internal Jetty failsafe mechanism,
   `and that use case was no longer deemed necessary <https://github.com/eclipse/jetty.project/issues/2525>`__.
   If one had previously used ``blockingTimeout`` to discard slow clients, please use the new configuration options
   ``minRequestDataPerSecond`` and ``minResponseDataPerSecond``
-  ``minRequestDataRate``: has been renamed to ``minRequestDataPerSecond`` and changed from a number to a size like "100 bytes"

Jersey changes
==============

Dropwizard has upgraded to the latest and greatest Jersey, but it has come at some migration cost:

If one created a custom provider (eg: parse / write JSON differently, so a custom ``JacksonJaxbJsonProvider`` is written),
you must annotate the class with the appropriate ``@Consumes`` and ``@Produces`` and register it with a Jersey ``Feature``
instead of an ``AbstractBinder`` if it been so previously.

`Jersey 2.26 <https://jersey.github.io/release-notes/2.26.html>`_ introduced it's own injection facade.
``dropwizard-jersey`` will automatically bring in the ``jersey-hk2`` dependency, but you will need to change your
imports from ``org.glassfish.hk2.utilities.binding.*`` to ``org.glassfish.jersey.internal.inject.*``.

HK2 internal API has been updated, so if you previously had a ``AbstractValueFactoryProvider``,
that will need to migrate to a ``AbstractValueParamProvider``.

Jersey Reactive Client API was updated to remove ``RxClient``, as reactive capabilities are built into the client.
You only need to use Dropwizard’s ``buildRx`` for client when you want a switch the default to something like RxJava 2’s ``Flowable``.

More Secure TLS
===============

Dropwizard 2.0, by default, only allows cipher suites that support forward secrecy.
The only cipher suites newly disabled are those under the ``TLS_RSA_*`` family.
Clients which don’t support forward secrecy (expected to be a small amount)
may now find that they can’t communicate with a Dropwizard 2.0 server.
If necessary one can override what cipher suites are blacklisted using the ``excludedCipherSuites`` configuration option.

Dropwizard 2.0, by default, only supports TLS 1.2. While Dropwizard 1.x effectively only supported TLS 1.2,
due to the supported cipher suites, one could still conceivably configure their server or receive a client
that could negotiate a TLS 1.0 or 1.1 connection.
One can still decide what TLS protocols are on the blacklist by configuring ``excludedProtocols``.

We also hope that in 2.0 it is more clear what protocols and cipher suites are enabled / disabled,
as previously one would see the following statement logged on startup:

::

   Supported protocols: [SSLv2Hello, SSLv3, TLSv1, TLSv1.1, TLSv1.2]

While not technically wrong, displaying the protocols that *could* be enabled is misleading
as it makes one believe that Dropwizard employs extremely unsafe defaults.
We’ve reworked what is logged to only the protocols and cipher suites that Dropwizard *will* expose.
And log the protocols and cipher suites that Dropwizard will reject,
and thus could expose them if configured to do so.
So now you’ll see the following in the logs:

::

   Enabled protocols: [TLSv1.2]
   Disabled protocols: [SSLv2Hello, SSLv3, TLSv1, TLSv1.1]

Miscellaneous
=============

Improved validation message for min/max duration
------------------------------------------------

``@MinDuration`` / ``@MaxDuration`` have had their validation messages improved, so instead of

   messageRate must be less than (or equal to, if in ‘inclusive’ mode) 1
   MINUTES

one will see if inclusive is true

::

   messageRate must be less than or equal to 1 MINUTES

if inclusive is false:

::

   messageRate must be less than 1 MINUTES
