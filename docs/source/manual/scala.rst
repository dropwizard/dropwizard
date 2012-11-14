.. _manual-scala:

##################
Dropwizard & Scala
##################

.. highlight:: text

.. rubric:: The ``dropwizard-scala`` module provides you with glue code required to write your
            Dropwizard services in Scala_.


.. _Scala: http://www.scala-lang.org

Dropwizard :ref:`services <man-core-service>` should extend ``ScalaService`` instead of ``Service``
and add ``ScalaBundle``:

.. code-block:: scala

    object ExampleService extends ScalaService[ExampleConfiguration]) {
      def initialize(bootstrap: Bootstrap[ExampleConfiguration]) {
        bootstrap.setName("example")
        bootstrap.addBundle(new ScalaBundle)
      }

      def run(configuration: ExampleConfiguration, environment: Environment) {
        environment.addResource(new ExampleResource)
      }
    }

.. _man-scala-features:

Features
========

``dropwizard-scala`` provides the following:

* ``QueryParam``-annotated parameters of type ``Seq[String]``, ``List[String]``, ``Vector[String]``,
  ``IndexedSeq[String]``, ``Set[String]``, and ``Option[String]``.
* Case class (i.e., ``Product`` instances) JSON request and response entities.
* ``Array[A]`` request and response entities. (Due to the JVM's type erasure and mismatches between
  Scala and Java type signatures, this is the only "generic" class supported since ``Array`` type
  parameters are reified.)
