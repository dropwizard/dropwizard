.. _manual-templates:

####################
Dropwizard Templates
####################

.. highlight:: text

.. rubric:: The ``dropwizard-templates`` module provides you with simple, fast templates using the
            Freemarker_.

.. _Freemarker: http://freemarker.sourceforge.net/

To enable Freemarker templates for your :ref:`service <man-core-service>`, add the
``TemplateBundle``:

.. code-block:: java

    public MyService() {
        super("my-service");
        addBundle(new TemplateBundle());
    }

Then, in your :ref:`resource method <man-core-resources>`, return a ``Viewable`` instance:

.. code-block:: java

    @Path("/people/{id}")
    @Produces(MediaType.TEXT_HTML)
    public class PersonResource {

        private final PersonDAO dao;

        public PersonResource(PersonDAO dao) {
            this.dao = dao;
        }

        @GET
        public Viewable getPerson(@PathParam("id") String id) {
            final Person person = dao.find(id);
            return new Viewable("index.ftl", person);
        }
    }

``index.ftl`` is the path of the template relative to the class name. If this class was
``com.example.service.PersonResource``, Jersey would then look for the file
``src/main/resources/com/example/service/PersonResource/index.ftl``, which might look something like
this:

.. code-block:: text
    :emphasize-lines: 1, 5

    <#-- @ftlvariable name="" type="com.example.core.Person" -->
    <html>
        <body>
            <!-- calls Person#getName() and sanitizes it -->
            <h1>Hello, ${name?html}!</h1>
        </body>
    </html>

The ``@fltvariable`` lets Freemarker (and any Freemarker IDE plugins you may be using) that the
default object is a ``com.example.core.Person`` instance. If you attempt to call a property which
doesn't exist on ``Person``--``getConnectionPool()``, for example--it will flag that line in your
IDE.

For more information on how to use Freemarker, see the `Freemarker documentation <Freemarker>`_.
