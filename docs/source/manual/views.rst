.. _manual-views:

################
Dropwizard Views
################

.. highlight:: text

.. rubric:: The ``dropwizard-views-mustache`` & ``dropwizard-views-freemarker`` modules provide you with simple, fast HTML views using either FreeMarker_ or Mustache_.

.. _FreeMarker: https://freemarker.apache.org/
.. _Mustache: https://mustache.github.io/mustache.5.html

To enable views for your :ref:`Application <man-core-application>`, add the ``ViewBundle`` in the ``initialize`` method of your Application class:

.. dropwizard_literalinclude:: /examples/views/src/main/java/io/dropwizard/documentation/ViewsApp.java
    :language: java
    :start-after: // views: ViewsApp#initialize->ViewBundle
    :end-before: // views: ViewsApp#initialize->ViewBundle
    :dedent: 8

You can pass configuration through to view renderers by overriding ``getViewConfiguration``:

.. dropwizard_literalinclude:: /examples/views/src/main/java/io/dropwizard/documentation/ViewsApp.java
    :language: java
    :start-after: // views: ViewsApp#initialize->ViewBundle->custom
    :end-before: // views: ViewsApp#initialize->ViewBundle->custom
    :dedent: 8

The returned map should have, for each renderer (such as ``freemarker`` or ``mustache``), a ``Map<String, String>`` describing how to configure the renderer. Specific keys and their meanings can be found in the FreeMarker and Mustache documentation:

.. dropwizard_literalinclude:: /examples/views/src/config/config.yml
    :language: yaml

Then, in your :ref:`resource method <man-core-resources>`, add a ``View`` class:

.. dropwizard_literalinclude:: /examples/views/src/main/java/io/dropwizard/documentation/PersonView.java
    :language: java

``person.ftl`` is the path of the template relative to the class name. If this class was
``com.example.service.PersonView``, Dropwizard would then look for the file
``src/main/resources/com/example/service/person.ftl``.

If your template path contains ``.ftl``, ``.ftlh``, or ``.ftlx``, it'll be interpreted as a FreeMarker_ template. If it contains
``.mustache``, it'll be interpreted as a Mustache template.

.. tip::

    Dropwizard Freemarker_ Views also support localized template files. It picks up the client's locale
    from their ``Accept-Language``, so you can add a French template in ``person_fr.ftl`` or a Canadian
    template in ``person_en_CA.ftl``.

Your template file might look something like this:

.. dropwizard_literalinclude:: /examples/views/src/main/resources/person.ftl
    :language: none
    :emphasize-lines: 1,5

The ``@ftlvariable`` lets FreeMarker (and any FreeMarker IDE plugins you may be using) know that the
root object is a ``com.example.views.PersonView`` instance. If you attempt to call a property which
doesn't exist on ``PersonView`` -- ``getConnectionPool()``, for example -- it will flag that line in
your IDE.

Once you have your view and template, you can simply return an instance of your ``View`` subclass:

.. dropwizard_literalinclude:: /examples/views/src/main/java/io/dropwizard/documentation/PersonResource.java
    :language: java

.. tip::

    Jackson can also serialize your views, allowing you to serve both ``text/html`` and
    ``application/json`` with a single representation class.

For more information on how to use FreeMarker, see the `FreeMarker`_ documentation.

For more information on how to use Mustache, see the `Mustache`_ and `Mustache.java`_ documentation.

 .. _Mustache.java: https://github.com/spullara/mustache.java

.. _man-views-template-errors:

Template Errors
===============

By default, if there is an error with the template (eg. the template file is not found or there is a
compilation error with the template), the user will receive a ``500 Internal Server Error`` with a
generic HTML message. The exact error will logged under error mode.

To customize the behavior, create an exception mapper that will override the default one by looking
for ``ViewRenderException``:

.. dropwizard_literalinclude:: /examples/views/src/main/java/io/dropwizard/documentation/ViewsApp.java
    :language: java
    :start-after: // views: ViewsApp#run->ExtendedExceptionMapper->ViewRenderException
    :end-before: // views: ViewsApp#run->ExtendedExceptionMapper->ViewRenderException
    :dedent: 8

As an example, to return a 404 instead of a internal server error when one's
mustache templates can't be found:

.. dropwizard_literalinclude:: /examples/views/src/main/java/io/dropwizard/documentation/ViewsApp.java
    :language: java
    :start-after: // views: ViewsApp#run->ExtendedExceptionMapper->MustacheNotFoundException
    :end-before: // views: ViewsApp#run->ExtendedExceptionMapper->MustacheNotFoundException
    :dedent: 8


Caching
=======
By default templates are cached to improve loading time. If you want to disable it during the development mode,
set the ``cache`` property to ``false`` in the view configuration.

.. dropwizard_literalinclude:: /examples/views/src/config/config.yml
    :language: yaml
    :start-after: # views: config->mustache
    :end-before: # views: config->mustache

Custom Error Pages
==================

To get HTML error pages that fit in with your application, you can use a custom error view. Create a ``View`` that
takes an ``ErrorMessage`` parameter in its constructor, and hook it up by registering a instance of
``ErrorEntityWriter``.

.. dropwizard_literalinclude:: /examples/views/src/main/java/io/dropwizard/documentation/ViewsApp.java
    :language: java
    :start-after: // views: ViewsApp#run->ErrorEntityWriter->ErrorMessage
    :end-before: // views: ViewsApp#run->ErrorEntityWriter->ErrorMessage
    :dedent: 8

For validation error messages, you'll need to register another ``ErrorEntityWriter`` that handles
``ValidationErrorMessage`` objects.

.. dropwizard_literalinclude:: /examples/views/src/main/java/io/dropwizard/documentation/ViewsApp.java
    :language: java
    :start-after: // views: ViewsApp#run->ErrorEntityWriter->ValidationErrorMessage
    :end-before: // views: ViewsApp#run->ErrorEntityWriter->ValidationErrorMessage
    :dedent: 8
