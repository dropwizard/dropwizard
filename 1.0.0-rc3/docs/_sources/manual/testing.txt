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
eyeballing your representation classes to ensure you're producing the API you think you
are. By using the helper methods in `FixtureHelpers`, you can add unit tests for serializing and
deserializing your representation classes to and from JSON.

Let's assume we have a ``Person`` class which your API uses as both a request entity (e.g., when
writing via a ``PUT`` request) and a response entity (e.g., when reading via a ``GET`` request):

.. code-block:: java

    public class Person {
        private String name;
        private String email;

        private Person() {
            // Jackson deserialization
        }

        public Person(String name, String email) {
            this.name = name;
            this.email = email;
        }

        @JsonProperty
        public String getName() {
            return name;
        }

        @JsonProperty
        public void setName(String name) {
            this.name = name;
        }

        @JsonProperty
        public String getEmail() {
            return email;
        }

        @JsonProperty
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
    import static org.assertj.core.api.Assertions.assertThat;
    import io.dropwizard.jackson.Jackson;
    import org.junit.Test;
    import com.fasterxml.jackson.databind.ObjectMapper;

    public class PersonTest {

        private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

        @Test
        public void serializesToJSON() throws Exception {
            final Person person = new Person("Luther Blissett", "lb@example.com");

            final String expected = MAPPER.writeValueAsString(
                    MAPPER.readValue(fixture("fixtures/person.json"), Person.class));

            assertThat(MAPPER.writeValueAsString(person)).isEqualTo(expected);
        }
    }

This test uses `AssertJ assertions`_ and JUnit_ to test that when a ``Person`` instance is serialized
via Jackson it matches the JSON in the fixture file. (The comparison is done on a normalized JSON
string representation, so formatting doesn't affect the results.)

.. _AssertJ assertions: http://assertj.org/assertj-core-conditions.html
.. _JUnit: http://www.junit.org/

.. _man-testing-representations-deserialization:

Testing Deserialization
-----------------------

Next, write a test for deserializing a ``Person`` instance from JSON:

.. code-block:: java

    import static io.dropwizard.testing.FixtureHelpers.*;
    import static org.assertj.core.api.Assertions.assertThat;
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


This test uses `AssertJ assertions`_ and JUnit_ to test that when a ``Person`` instance is
deserialized via Jackson from the specified JSON fixture it matches the given object.

.. _man-testing-resources:

Testing Resources
=================

While many resource classes can be tested just by calling the methods on the class in a test, some
resources lend themselves to a more full-stack approach. For these, use ``ResourceTestRule``, which
loads a given resource instance in an in-memory Jersey server:

.. _man-testing-resources-example:

.. code-block:: java

    import static org.assertj.core.api.Assertions.assertThat;
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
        }

        @After
        public void tearDown(){
            // we have to reset the mock after each test because of the
            // @ClassRule, or use a @Rule as mentioned below.
            reset(dao);
        }

        @Test
        public void testGetPerson() {
            assertThat(resources.client().target("/person/blah").request().get(Person.class))
                    .isEqualTo(person);
            verify(dao).fetchPerson("blah");
        }
    }

Instantiate a ``ResourceTestRule`` using its ``Builder`` and add the various resource instances you
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

Default Exception Mappers
-------------------------

By default, a ``ResourceTestRule`` will register all the default exception mappers (this behavior is new in 1.0). If
``registerDefaultExceptionMappers`` in the configuration yaml is planned to be set to ``false``,
``ResourceTestRule.Builder#setRegisterDefaultExceptionMappers(boolean)`` will also need to be set to ``false``. Then,
all custom exception mappers will need to be registered on the builder, similarly to how they are registered in an
``Application`` class.

Test Containers
---------------

Note that the in-memory Jersey test container does not support all features, such as the ``@Context`` injection used by
``BasicAuthFactory`` and ``OAuthFactory``. A different `test container`__ can be used via
``ResourceTestRule.Builder#setTestContainerFactory(TestContainerFactory)``.

For example, if you want to use the `Grizzly`_ HTTP server (which supports ``@Context`` injections) you need to add the
dependency for the Jersey Test Framework providers to your Maven POM and set ``GrizzlyWebTestContainerFactory`` as
``TestContainerFactory`` in your test classes.

.. code-block:: xml

    <dependency>
        <groupId>org.glassfish.jersey.test-framework.providers</groupId>
        <artifactId>jersey-test-framework-provider-grizzly2</artifactId>
        <version>${jersey.version}</version>
        <scope>test</scope>
        <exclusions>
            <exclusion>
                <groupId>javax.servlet</groupId>
                <artifactId>javax.servlet-api</artifactId>
            </exclusion>
            <exclusion>
                <groupId>junit</groupId>
                <artifactId>junit</artifactId>
            </exclusion>
        </exclusions>
    </dependency>


.. code-block:: java

    public class ResourceTestWithGrizzly {
        @ClassRule
        public static final ResourceTestRule RULE = ResourceTestRule.builder()
            .setTestContainerFactory(new GrizzlyWebTestContainerFactory())
            .addResource(new ExampleResource())
            .build();

        @Test
        public void testResource() {
            assertThat(RULE.getJerseyTest().target("/example").request()
                .get(String.class))
                .isEqualTo("example");
        }
    }

.. __: https://jersey.java.net/documentation/latest/test-framework.html
.. _Grizzly: https://grizzly.java.net/

.. _man-testing-clients:

Testing Client Implementations
==============================

To avoid circular dependencies in your projects or to speed up test runs, you can test your HTTP client code
by writing a JAX-RS resource as test double and let the ``DropwizardClientRule`` start and stop a simple Dropwizard
application containing your test doubles.

.. _man-testing-clients-example:

.. code-block:: java

    public class CustomClientTest {
        @Path("/ping")
        public static class PingResource {
            @GET
            public String ping() {
                return "pong";
            }
        }

        @ClassRule
        public static final DropwizardClientRule dropwizard = new DropwizardClientRule(new PingResource());

        @Test
        public void shouldPing() throws IOException {
            final URL url = new URL(dropwizard.baseUri() + "/ping");
            final String response = new BufferedReader(new InputStreamReader(url.openStream())).readLine();
            assertEquals("pong", response);
        }
    }

.. hint::

    Of course you would use your HTTP client in the ``@Test`` method and not ``java.net.URL#openStream()``.

The ``DropwizardClientRule`` takes care of:

* Creating a simple default configuration.
* Creating a simplistic application.
* Adding a dummy health check to the application to suppress the startup warning.
* Adding your JAX-RS resources (test doubles) to the Dropwizard application.
* Choosing a free random port number (important for running tests in parallel).
* Starting the Dropwizard application containing the test doubles.
* Stopping the Dropwizard application containing the test doubles.


Integration Testing
===================

It can be useful to start up your entire application and hit it with real HTTP requests during testing.
The ``dropwizard-testing`` module offers helper classes for your easily doing so.
The optional ``dropwizard-client`` module offers more helpers, e.g. a custom JerseyClientBuilder,
which is aware of your application's environment.

JUnit
-----
Adding ``DropwizardAppRule`` to your JUnit test class will start the app prior to any tests
running and stop it again when they've completed (roughly equivalent to having used ``@BeforeClass`` and ``@AfterClass``).
``DropwizardAppRule`` also exposes the app's ``Configuration``,
``Environment`` and the app object itself so that these can be queried by the tests.

.. code-block:: java

    public class LoginAcceptanceTest {

        @ClassRule
        public static final DropwizardAppRule<TestConfiguration> RULE =
                new DropwizardAppRule<TestConfiguration>(MyApp.class, ResourceHelpers.resourceFilePath("my-app-config.yaml"));

        @Test
        public void loginHandlerRedirectsAfterPost() {
            Client client = new JerseyClientBuilder(RULE.getEnvironment()).build("test client");

            Response response = client.target(
                     String.format("http://localhost:%d/login", RULE.getLocalPort()))
                    .request()
                    .post(Entity.json(loginForm()));

            assertThat(response.getStatus()).isEqualTo(302);
        }
    }

Non-JUnit
---------
By creating a DropwizardTestSupport instance in your test you can manually start and stop the app in your tests, you do this by calling its ``before`` and ``after`` methods. ``DropwizardTestSupport`` also exposes the app's ``Configuration``, ``Environment`` and the app object itself so that these can be queried by the tests.

.. code-block:: java

    public class LoginAcceptanceTest {

        public static final DropwizardTestSupport<TestConfiguration> SUPPORT =
                new DropwizardTestSupport<TestConfiguration>(MyApp.class,
                    ResourceHelpers.resourceFilePath("my-app-config.yaml"),
                    ConfigOverride.config("server.applicationConnectors[0].port", "0") // Optional, if not using a separate testing-specific configuration file, use a randomly selected port
                );

        @BeforeClass
        public void beforeClass() {
            SUPPORT.before();
        }

        @AfterClass
        public void afterClass() {
            SUPPORT.after();
        }

        @Test
        public void loginHandlerRedirectsAfterPost() {
            Client client = new JerseyClientBuilder(SUPPORT.getEnvironment()).build("test client");

            Response response = client.target(
                     String.format("http://localhost:%d/login", SUPPORT.getLocalPort()))
                    .request()
                    .post(Entity.json(loginForm()));

            assertThat(response.getStatus()).isEqualTo(302);
        }
    }
