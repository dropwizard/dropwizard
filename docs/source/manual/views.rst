.. _manual-views:

################
Dropwizard Views
################

.. highlight:: text

.. rubric:: The ``dropwizard-views-mustache`` & ``dropwizard-views-freemarker`` modules provide you with simple, fast HTML views using either FreeMarker_ or Mustache_.

.. _FreeMarker: http://FreeMarker.sourceforge.net/
.. _Mustache: http://mustache.github.com/mustache.5.html

To enable views for your :ref:`Application <man-core-application>`, add the ``ViewBundle`` in the ``initialize`` method of your Application class:

.. code-block:: java

    public void initialize(Bootstrap<MyConfiguration> bootstrap) {
        bootstrap.addBundle(new ViewBundle<MyConfiguration>());
    }

You can pass configuration through to view renderers by overriding ``getViewConfiguration``:

.. code-block:: java

    public void initialize(Bootstrap<MyConfiguration> bootstrap) {
        bootstrap.addBundle(new ViewBundle<MyConfiguration>() {
            @Override
            public Map<String, Map<String, String>> getViewConfiguration(MyConfiguration config) {
                return config.getViewRendererConfiguration();
            }
        });
    }

The returned map should have, for each extension (such as ``.ftl``), a ``Map<String, String>`` describing how to configure the renderer. Specific keys and their meanings can be found in the FreeMarker and Mustache documentation:

.. code-block:: yaml

    views:
      .ftl:
        strict_syntax: yes

Then, in your :ref:`resource method <man-core-resources>`, add a ``View`` class:

.. code-block:: java

    public class PersonView extends View {
        private final Person person;

        public PersonView(Person person) {
            super("person.ftl");
            this.person = person;
        }

        public Person getPerson() {
            return person;
        }
    }

``person.ftl`` is the path of the template relative to the class name. If this class was
``com.example.service.PersonView``, Dropwizard would then look for the file
``src/main/resources/com/example/service/person.ftl``.

If your template ends with ``.ftl``, it'll be interpreted as a FreeMarker_ template. If it ends with
``.mustache``, it'll be interpreted as a Mustache template.

.. tip::

    Dropwizard Freemarker_ Views also support localized template files. It picks up the client's locale
    from their ``Accept-Language``, so you can add a French template in ``person_fr.ftl`` or a Canadian
    template in ``person_en_CA.ftl``.

Your template file might look something like this:

.. code-block:: html
    :emphasize-lines: 1,5

    <#-- @ftlvariable name="" type="com.example.views.PersonView" -->
    <html>
        <body>
            <!-- calls getPerson().getName() and sanitizes it -->
            <h1>Hello, ${person.name?html}!</h1>
        </body>
    </html>

The ``@ftlvariable`` lets FreeMarker (and any FreeMarker IDE plugins you may be using) know that the
root object is a ``com.example.views.PersonView`` instance. If you attempt to call a property which
doesn't exist on ``PersonView`` -- ``getConnectionPool()``, for example -- it will flag that line in
your IDE.

Once you have your view and template, you can simply return an instance of your ``View`` subclass:

.. code-block:: java

    @Path("/people/{id}")
    @Produces(MediaType.TEXT_HTML)
    public class PersonResource {
        private final PersonDAO dao;

        public PersonResource(PersonDAO dao) {
            this.dao = dao;
        }

        @GET
        public PersonView getPerson(@PathParam("id") String id) {
            return new PersonView(dao.find(id));
        }
    }

.. tip::

    Jackson can also serialize your views, allowing you to serve both ``text/html`` and
    ``application/json`` with a single representation class.

For more information on how to use FreeMarker, see the `FreeMarker`_ documentation.

For more information on how to use Mustache, see the `Mustache`_ and `Mustache.java`_ documentation.

 .. _Mustache.java: https://github.com/spullara/mustache.java

.. _man-views-template-errors:

Template Errors
===============

If there is an error with the template (eg. the template file is not found or there is a compilation
error with the template), the user will receive a ``500 Internal Sever Error`` with a generic HTML
message. The exact error will logged under debug mode.
