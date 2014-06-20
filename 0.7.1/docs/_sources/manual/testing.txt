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
are. By using the helper methods in `FixtureHelpers` you can add unit tests for serializing and
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

    import static io.dropwizard.testing.FixtureHelpers.*;
    import static org.fest.assertions.api.Assertions.assertThat;
    import io.dropwizard.jackson.Jackson;
    import org.junit.Test;
    import com.fasterxml.jackson.databind.ObjectMapper;

    public class PersonTest {

        private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

        @Test
        public void serializesToJSON() throws Exception {
            final Person person = new Person("Luther Blissett", "lb@example.com");
            assertThat(MAPPER.writeValueAsString(person))
                    .isEqualTo(fixture("fixtures/person.json"));
        }
    }

This test uses `FEST matchers`_ and JUnit_ to test that when a ``Person`` instance is serialized
via Jackson it matches the JSON in the fixture file. (The comparison is done via a normalized JSON
string representation, so whitespace doesn't affect the results.)

.. _FEST matchers: https://code.google.com/p/fest/
.. _JUnit: http://www.junit.org/

.. _man-testing-representations-deserialization:

Testing Deserialization
-----------------------

Next, write a test for deserializing a ``Person`` instance from JSON:

.. code-block:: java

    import static io.dropwizard.testing.FixtureHelpers.*;
    import static org.fest.assertions.api.Assertions.assertThat;
    import io.dropwizard.jackson.Jackson;
    import org.junit.Test;
    import com.fasterxml.jackson.databind.ObjectMapper;

    public class PersonTest {

        private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

        @Test
        public void deserializesFromJSON() throws Exception {
            final Person person = new Person("Luther Blissett", "lb@example.com");
            assertThat(MAPPER.readValue(fixture("fixtures/person.json"), Person.class))
                    .isEqualTo(person);
        }
    }


This test uses `FEST matchers`_ and JUnit_ to test that when a ``Person`` instance is
deserialized via Jackson from the specified JSON fixture it matches the given object.

.. _man-testing-resources:

Testing Resources
=================

While many resource classes can be tested just by calling the methods on the class in a test, some
resources lend themselves to a more full-stack approach. For these, use ``ResourceTestRule``, which
loads a given resource instance in an in-memory Jersey server:

.. _man-testing-resources-example:

.. code-block:: java

    import static org.fest.assertions.api.Assertions.assertThat;
    import static org.mockito.Mockito.*;

    public class PersonResourceTest {

        private static final PeopleStore dao = mock(PeopleStore.class);

        @ClassRule
        public static final ResourceTestRule resources = ResourceTestRule.builder()
                .addResource(new PersonResource(dao))
                .build();

        private final Person person = new Person("blah", "blah@example.com");

        @Before
        public void setup() {
            when(dao.fetchPerson(eq("blah"))).thenReturn(person);
            // we have to reset the mock after each test because of the
            // @ClassRule, or use a @Rule as mentioned below.
            reset(dao);
        }

        @Test
        public void testGetPerson() {
            assertThat(resources.client().resource("/person/blah").get(Person.class))
                    .isEqualTo(person);
            verify(dao).fetchPerson("blah");
        }
    }

Instansiate a ``ResourceTestRule`` using its ``Builder`` and add the various resource instances you
want to test via ``ResourceTestRule.Builder#addResource(Object)``. Use a ``@ClassRule`` annotation
to have the rule wrap the entire test class or the ``@Rule`` annotation to have the rule wrap
each test individually (make sure to remove static final modifier from ``resources``).

In your tests, use ``#client()``, which returns a Jersey ``Client`` instance to talk to and test
your instances.

This doesn't require opening a port, but ``ResourceTestRule`` tests will perform all the serialization,
deserialization, and validation that happens inside of the HTTP process.

This also doesn't require a full integration test. In the above
:ref:`example <man-testing-resources-example>`, a mocked ``PeopleStore`` is passed to the
``PersonResource`` instance to isolate it from the database. Not only does this make the test much
faster, but it allows your resource unit tests to test error conditions and edge cases much more
easily.

.. hint::

    You can trust ``PeopleStore`` works because you've got working unit tests for it, right?

Should you, at some point, grow tired of the near-infinite amount of debug logging produced by
``ResourceTestRule`` you can use the ``java.util.logging`` API to silence the ``com.sun.jersey`` logger.

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

            assertThat(response.getStatus()).isEqualTo(302);
        }
    }
