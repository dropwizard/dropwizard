.. _manual-upgrading:

##################
Upgrading to 0.7
##################

.. highlight:: text

.. rubric:: Dropwizard under went a significant number of changes between 0.6.2 and 0.7.

These changes include:

* Change to the artifact and package name from ``com.yammer`` to ``io.dropwizard``
* Renaming the ``Service`` class to ``Application``
* Lots of updated dependencies updated including (but not limited to), jetty and metrics, 
  both of which include incompatable changes see the :ref:`release notes<release-notes>` for a complete list


Packaging changes between 0.6 and 0.7
=====================================

Assuming you are using maven for your dependency management then you will need to update your ``pom.xml``

From:

.. code-block:: xml

    <dependencies>
        <dependency>
            <groupId>com.yammer.dropwizard</groupId>
            <artifactId>dropwizard-core</artifactId>
            <version>0.6.2</version>
        </dependency>
    </dependencies>

To:

.. code-block:: xml

    <dependencies>
        <dependency>
            <groupId>io.dropwizard</groupId>
            <artifactId>dropwizard-core</artifactId>
            <version>0.7.0</version>
        </dependency>
    </dependencies>

Note that :ref:`getting started guide<getting-started>` recomends using a maven property for the version.

As you might expect the group id for all of the dropwizard modules has changed in a similar way.

Code changes between 0.6 and 0.7
================================

The package name for all dropwizard classes has changed from ``com.yammer.dropwizard`` to 
``io.dropwizard``. 

The core ``Service`` class has been renamed to ``Application`` so at the very least you will need to change 
the your ``Service`` implementation:

.. code-block:: java

    import io.dropwizard.Application; // used to be com.yammer.dropwizard.Service
    
    public class YourService extends Application<YourConfiguration> {
        
    }

Obivously this is the bare minium it is reconmened that you rename the class to ``YourApplication``.

Other class changes
-------------------

``com.yammer.dropwizard.config.Environment`` -> ``io.dropwizard.setup.Environment``
``com.yammer.dropwizard.config.Bootstrap`` -> ``io.dropwizard.setup.Bootstrap``
``com.yammer.dropwizard.config.Configuration`` -> ``io.dropwizard.Configuration``

Because of the upgrade of the metrics library to 3.x all references to ``com.yammer.metrics`` 
with ``com.codahale.metrics``.

``com.yammer.metrics.core.HealthCheck`` -> ``com.codahale.metrics.health.HealthCheck``


