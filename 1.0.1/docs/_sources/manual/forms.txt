.. _man-forms:

################
Dropwizard Forms
################

.. highlight:: text

.. rubric:: The ``dropwizard-forms`` module provides you with a support for multi-part forms
            via Jersey_.

.. _Jersey: https://jersey.java.net/

Adding The Bundle
=================

Then, in your application's ``initialize`` method, add a new ``MultiPartBundle`` subclass:

.. code-block:: java

    @Override
    public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
        bootstrap.addBundle(new MultiPartBundle());
    }

More Information
================

For additional and more detailed documentation about the Jersey multi-part support, please refer to the
documentation in the `Jersey User Guide`_ and Javadoc_.

.. _Jersey User Guide: https://jersey.java.net/documentation/latest/media.html#multipart
.. _Javadoc: https://jersey.java.net/apidocs/latest/jersey/org/glassfish/jersey/media/multipart/package-summary.html
