.. _manual-testing:

##################
Testing Dropwizard
##################

.. highlight:: text

.. rubric:: The ``dropwizard-testing`` module provides you with some handy classes for testing
            your :ref:`representation classes <man-core-representations>`
            and :ref:`resource classes <man-core-resources>`. It also provides a JUnit rule
            for full-stack testing of your entire app.

.. _man-testing-representations:

Testing Representations
=======================

While Jackson's JSON support is powerful and fairly easy-to-use, you shouldn't just rely on
eyeballing your representation classes to ensure you're actually producing the API you think you
are. By using the helper methods in `JsonHelpers` you can add unit tests for serializing and
deserializing your representation classes to and from JSON.

Let's assume we have a ``Person`` class which your API uses as both a request entity (e.g., when
writing via a ``PUT`` request) and a response entity (e.g., when reading via a ``GET`` request):

.. code-block:: java

    public class Person {
        @JsonProperty
        private String name;

        @JsonProperty
        private String email;

        private Person() {
            // Jackson deserialization
        }

        public Person(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        // hashCode
        // equals
        // toString etc.
    }

.. _man-testing-representations-fixtures:

Fixtures
--------

First, write out the exact JSON representation of a ``Person`` in the
``src/test/resources/fixtures`` directory of your Dropwizard project as ``person.json``:

.. code-block:: javascript

    {
        "name": "Luther Blissett",
        "email": "lb@example.com"
    }

.. _man-testing-representations-serialization:

Testing Serialization
---------------------

Next, write a test for serializing a ``Person`` instance to JSON:

.. code-block:: java

    import static com.codahale.dropwizard.testing.JsonHelpers.*;
    import static org.hamcrest.Matchers.*;

    @Test
    public void serializesToJSON() throws Exception {
        final Person person = new Person("Luther Blissett", "lb@example.com");
        assertThat("a Person can be serialized to JSON",
                   asJson(person),
                   is(equalTo(jsonFixture("fixtures/person.json"))));
    }

This test uses `Hamcrest matchers`_ and JUnit_ to test that when a ``Person`` instance is serialized
via Jackson it matches the JSON in the fixture file. (The comparison is done via a normalized JSON
string representation, so whitespace doesn't affect the results.)

.. _Hamcrest matchers: http://code.google.com/p/hamcrest/
.. _JUnit: http://www.junit.org/

.. _man-testing-representations-deserialization:

Testing Deserialization
-----------------------

Next, write a test for deserializing a ``Person`` instance from JSON:

.. code-block:: java

    import static com.codahale.dropwizard.testing.JsonHelpers.*;
    import static org.hamcrest.Matchers.*;

    @Test
    public void deserializesFromJSON() throws Exception {
        final Person person = new Person("Luther Blissett", "lb@example.com");
        assertThat("a Person can be deserialized from JSON",
                   fromJson(jsonFixture("fixtures/person.json"), Person.class),
                   is(person));
    }


This test uses `Hamcrest matchers`_ and JUnit_ to test that when a ``Person`` instance is
deserialized via Jackson from the specified JSON fixture it matches the given object.

.. _man-testing-resources:

Testing Resources
=================

While many resource classes can be tested just by calling the methods on the class in a test, some
resources lend themselves to a more full-stack approach. For these, use ``ResourceTest``, which
loads a given resource instance in an in-memory Jersey server:

.. _man-testing-resources-example:

.. code-block:: java

    import static org.fest.assertions.api.Assertions.assertThat;

    public class PersonResourceTest extends ResourceTest {
        private final Person person = new Person("blah", "blah@example.com");
        private final PersonDAO dao = mock(PersonDAO.class);

        @Override
        protected void setUpResources() {
            when(dao.fetchPerson(anyString())).thenReturn(person);
            addResource(new PersonResource(dao));
        }

        @Test
        public void simpleResourceTest() throws Exception {
            assertThat(client().resource("/person/blah").get(Person.class))
                       .isEqualTo(person);

            verify(dao).fetchPerson("blah");
        }
    }

In your ``#setUpResources()`` method, instantiate the various resource instances you want to test
and add them to the test context via ``#addResource(Object)``. In your actual test methods, use
``#client()`` which returns a Jersey ``Client`` instance which will talk to your resource instances.

This doesn't require opening a port, but ``ResourceTest`` tests will perform all the serialization,
deserialization, and validation that happens inside of the HTTP process.

This also doesn't require a full integration test. In the above
:ref:`example <man-testing-resources-example>`, a mocked ``PersonDAO`` is passed to the
``PersonResource`` instance to isolate it from the database. Not only does this make the test much
faster, but it allows your resource unit tests to test error conditions and edge cases much more
easily.

.. hint::

    You can trust ``PersonDAO`` works because you've got working unit tests for it, right?

Should you, at some point, grow tired of the near-infinite amount of debug logging produced by
``ResourceTest`` you can use the ``java.util.logging`` API to silence the ``com.sun.jersey`` logger.


Integrated Testing
==================
It can be useful to start up your entire app and hit it with real HTTP requests during testing. This can be
achieved by adding ``DropwizardAppRule`` to your JUnit test class, which will start the app prior to any tests
running and stop it again when they've completed (roughly equivalent to having used ``@BeforeClass`` and ``@AfterClass``).
``DropwizardAppRule`` also exposes the app's ``Configuration``,
``Environment`` and the app object itself so that these can be queried by the tests.

.. code-block:: java

    public class LoginAcceptanceTest {

        @ClassRule
        public static final DropwizardAppRule<TestConfiguration> RULE =
                new DropwizardAppRule<TestConfiguration>(MyApp.class, resourceFilePath("my-app-config.yaml"));

        @Test
        public void loginHandlerRedirectsAfterPost() {
            Client client = new Client();

            ClientResponse response = client.resource(
                    String.format("http://localhost:%d/login", RULE.getLocalPort()))
                    .post(ClientResponse.class, loginForm());

            assertThat(response.getStatus(), is(302));
        }
    }
