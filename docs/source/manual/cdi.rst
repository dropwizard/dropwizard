.. _man-cdi:

###################
Dropwizard CDI Weld
###################

.. rubric:: The ``dropwizard-cdi-weld`` module provides a ``WeldBundle`` enabling CDI for Dropwizard.

.. _man-cdi-weld:

WeldBundle
==========

Add the following dependency to your ``pom.xml`.

.. code-block:: xml

    <dependency>
      <groupId>io.dropwizard</groupId>
      <artifactId>dropwizard-cdi-weld</artifactId>
    </dependency>

In your application class add the ``WeldBundle`` as first step in the initialize method.

.. code-block:: java

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(new WeldBundle());
    }

For every module in your project that requires injection ensure you define a ``beans.xml``
at `resources/META-INF/beans.xml`. This way the right modules will be scanned at start up.

To scan all classes in the module your ``beans.xml`` should look like:

.. code-block:: xml

  <beans xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
         http://xmlns.jcp.org/xml/ns/javaee/beans_1_1.xsd"
         bean-discovery-mode="all">
  </beans>

To extend default behaviour (https://docs.jboss.org/weld/reference/latest/en-US/html/) a framework like Apache DeltaSpike (https://deltaspike.apache.org/) can be used.
