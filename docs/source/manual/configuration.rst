.. _man-configuration:

####################
Dropwizard Configuration
####################

.. highlight:: text

.. rubric:: The ``dropwizard-configuration`` module provides you with a polymorphic configuration
            mechanism.


Overview
=============

Create a :ref:`configuration <man-core-configuration>`


.. _man-configuration-servers:

Servers
--------

.. code-block:: yaml

    server:
      type: default
      maxThreads: 1024


All
,,,

====================== ===========  ===========
Name                   Default      Description
====================== ===========  ===========
type                   default      - default
                                    - simple	
maxThreads             1024         The maximum number of threads to use for requests.
minThreads             8            The minimum number of threads to use for requests.
maxQueuedRequests      1024         The maximum number of requests to queue before blocking the acceptors.
idleThreadTimeout      1 minute     The amount of time a worker thread can be idle before being stopped.
nofileSoftLimit        (none)       The number of open file descriptors before a soft error is issued.
                                    Requires Jetty's ``libsetuid.so`` on ``java.library.path``.
nofileHardLimit        (none)       The number of open file descriptors before a hard error is issued.
                                    Requires Jetty's ``libsetuid.so`` on ``java.library.path``.
gid                    (none)       The group ID to switch to once the connectors have started.
                                    Requires Jetty's ``libsetuid.so`` on ``java.library.path``.
uid                    (none)       The user ID to switch to once the connectors have started.
                                    Requires Jetty's ``libsetuid.so`` on ``java.library.path``.
user                   (none)       The username to switch to once the connectors have started.
                                    Requires Jetty's ``libsetuid.so`` on ``java.library.path``.
group                  (none)       The group to switch to once the connectors have started.
                                    Requires Jetty's ``libsetuid.so`` on ``java.library.path``.
umask                  (none)       The umask to switch to once the connectors have started.
                                    Requires Jetty's ``libsetuid.so`` on ``java.library.path``.
startsAsRoot           (none)       Whether or not the Dropwizard application is started as a root user.
                                    Requires Jetty's ``libsetuid.so`` on ``java.library.path``.
====================== ===========  ===========


.. _man-configuration-gzip:

GZip
.....

.. code-block:: yaml

    server:
      gzip: 
        bufferSize: 8KiB


+----------------------+------------+---------------------------------------------------------------------------------------------------+ 
|     Name             | Default    | Description                                                                                       | 
+======================+============+===================================================================================================+ 
| enabled              | true       | If true, all requests with gzip in their Accept-Content-Encoding                                  | 
|                      |            | headers will have their response entities encoded with gzip.                                      |
+----------------------+------------+---------------------------------------------------------------------------------------------------+
| minimumEntitySize    | 256 bytes  | All response entities under this size are not compressed.                                         |
+----------------------+------------+---------------------------------------------------------------------------------------------------+
| bufferSize           | 8KiB       | The size of the buffer to use when compressing.                                                   |
+----------------------+------------+---------------------------------------------------------------------------------------------------+
| excludedUserAgents   | []         | The set of user agents to exclude from compression.                                               |
+----------------------+------------+---------------------------------------------------------------------------------------------------+
| compressedMimeTypes  | []         | If specified, the set of mime types to compress.                                                  |
+----------------------+------------+---------------------------------------------------------------------------------------------------+


.. _man-configuration-requestLog:

Request Log
...........

.. code-block:: yaml

    server:
      requestLog: 
        timeZone: UTC


====================== ===========  ===========
Name                   Default      Description
====================== ===========  ===========
timeZone               UTC          The time zone to which request timestamps will be converted.
====================== ===========  ===========


.. _man-configuration-simple:

Simple
,,,,,,

====================== ===========  ===========
Name                   Default      Description
====================== ===========  ===========
example                 xxx         Sample description
====================== ===========  ===========


.. _man-configuration-default:

Default
,,,,,,,

====================== ===========  ===========
Name                   Default      Description
====================== ===========  ===========
example                 xxx         Sample description
====================== ===========  ===========


.. _man-configuration-connectors:

Connectors
==========

.. _man-configuration-http:

HTTP
------

====================== ===========  ===========
Name                   Default      Description
====================== ===========  ===========
example                 xxx         Sample description
====================== ===========  ===========


.. _man-configuration-https:

HTTPS
------

====================== ===========  ===========
Name                   Default      Description
====================== ===========  ===========
example                 xxx         Sample description
====================== ===========  ===========


.. _man-configuration-spdy:

SPDY
------

====================== ===========  ===========
Name                   Default      Description
====================== ===========  ===========
example                 xxx         Sample description
====================== ===========  ===========


.. _man-configuration-logging:

Logging
=========

====================== ===========  ===========
Name                   Default      Description
====================== ===========  ===========
example                 xxx         Sample description
====================== ===========  ===========


.. _man-configuration-metrics:

Metrics
=========

====================== ===========  ===========
Name                   Default      Description
====================== ===========  ===========
example                 xxx         Sample description
====================== ===========  ===========


