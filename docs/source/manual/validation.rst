.. _man-validation:

#####################
Dropwizard Validation
#####################

.. highlight:: text

.. rubric:: Dropwizard comes with a host of validation tools out of the box to allow endpoints to return meaningful error messages when constraints are violated. `Hibernate Validator`_ is packaged with Dropwizard, so what can be done in Hibernate Validator, can be done with Dropwizard.

.. _Hibernate Validator: http://hibernate.org/validator/

.. _man-validation-validations:

Validations
===========

Almost anything can be validated on resource endpoints. To give a quick example, the following
endpoint doesn't allow a null or empty ``name`` query parameter.

.. code-block:: java

    @GET
    public String find(@QueryParam("name") @NotEmpty String arg) {
        // ...
    }

If a client sends an empty or nonexistent name query param, Dropwizard will respond with a ``400 Bad Request``
code with the error: ``query param name may not be empty``.

Additionally, annotations such as ``HeaderParam``, ``CookieParam``, ``FormParam``, etc, can be
constrained with violations giving descriptive errors and 400 status codes.

.. _man-validation-validations-constraining-entities:

Constraining Entities
*********************

If we're accepting client-provided ``Person``, we probably want to ensure that the ``name`` field of
the object isn't ``null`` or blank in the request. We can do this as follows:

.. code-block:: java

    public class Person {

        @NotEmpty // ensure that name isn't null or blank
        private final String name;

        @JsonCreator
        public Person(@JsonProperty("name") String name) {
            this.name = name;
        }

        @JsonProperty("name")
        public String getName() {
            return name;
        }
    }

Then, in our resource class, we can add the ``@Valid`` annotation to the ``Person`` annotation:

.. code-block:: java

    @PUT
    public Person replace(@NotNull @Valid Person person) {
        // ...
    }

If the name field is missing, Dropwizard will return a ``422 Unprocessable Entity`` response
detailing the validation errors: ``name may not be empty``

.. note::

    You don't need ``@Valid`` when the type you are validating can be validated directly (``int``,
    ``String``, ``Integer``). If a class has fields that need validating, then instances of the
    class must be marked ``@Valid``. For more information, see the Hibernate Validator documentation
    on `Object graphs`_ and `Cascaded validation`_.

.. _Object graphs: http://docs.jboss.org/hibernate/validator/5.2/reference/en-US/html/chapter-bean-constraints.html#section-object-graph-validation

.. _Cascaded validation: http://docs.jboss.org/hibernate/validator/5.2/reference/en-US/html/chapter-method-constraints.html#_cascaded_validation

Since our entity is also annotated with ``@NotNull``, Dropwizard will also guard against ``null``
input with a response stating that the body must not be null.

.. _man-validation-validations-optional-constraints:

``Optional<T>`` Constraints
***************************

If an entity, field, or parameter is not required, it can be wrapped in an ``Optional<T>``, but the
inner value can still be constrained with the ``@UnwrapValidatedValue`` annotation. If the
``Optional`` is absent, then the constraints are not applied.

.. note::

    Be careful when using constraints with ``*Param`` annotations on ``Optional<String>`` parameters
    as there is a subtle, but important distinction between null and empty. If a client requests
    ``bar?q=``, ``q`` will evaluate to ``Optional.of("")``. If you want ``q`` to evaluate to
    ``Optional.absent()`` in this situation, change the type to ``NonEmptyStringParam``

.. note::

    Param types such as ``IntParam`` and ``NonEmptyStringParam`` can also be constrained.

There is a caveat regarding ``@UnwrapValidatedValue`` and ``*Param`` types, as there still are some
cumbersome situations when constraints need to be applied to the container and the value.

.. code-block:: java

    @POST
    // The @NotNull is supposed to mean that the parameter is required but the Max(3) is supposed to
    // apply to the contained integer. Currently, this code will fail saying that Max can't
    // be applied on an IntParam
    public List<Person> createNum(@QueryParam("num") @UnwrapValidatedValue(false)
                                  @NotNull @Max(3) IntParam num) {
        // ...
    }

    @GET
    // Similarly, the underlying validation framework can't unwrap nested types (an integer wrapped
    // in an IntParam wrapped in an Optional), regardless if the @UnwrapValidatedValue is used
    public Person retrieve(@QueryParam("num") @Max(3) Optional<IntParam> num) {
        // ...
    }

To work around these limitations, if the parameter is required check for it in the endpoint and
throw an exception, else use ``@DefaultValue`` or move the ``Optional`` into the endpoint.

.. code-block:: java

    @POST
    // Workaround to handle required int params and validations
    public List<Person> createNum(@QueryParam("num") @Max(3) IntParam num) {
        if (num == null) {
            throw new WebApplicationException("query param num must not be null", 400);
        }
        // ...
    }

    @GET
    // Workaround to handle optional int params and validations with DefaultValue
    public Person retrieve(@QueryParam("num") @DefaultValue("0") @Max(3) IntParam num) {
        // ...
    }

    @GET
    // Workaround to handle optional int params and validations with Optional
    public Person retrieve2(@QueryParam("num") @Max(3) IntParam num) {
        Optional.fromNullable(num);
        // ...
    }

.. _man-validation-validations-return-value-validations:

Return Value Validations
************************

It's reasonable to want to make guarantees to clients regarding the server response. For example,
you may want to assert that no response will ever be ``null``, and if an endpoint creates a
``Person`` that the person is valid.

.. code-block:: java

    @POST
    @NotNull
    @Valid
    public Person create() {
        return new Person(null);
    }

In this instance, instead of returning someone with a null name, Dropwizard will return an ``HTTP
500 Internal Server Error`` with the error ``server response name may not be empty``, so the client
knows the server failed through no fault of their own.

Analogous to an empty request body, an empty entity annotated with ``@NotNull`` will return ``server
response may not be null``

Annotations
===========

In addition to the `annotations defined in Hibernate Validator`_, Dropwizard contains another set of annotations,
which are briefly shown below.

.. _annotations defined in Hibernate Validator: http://docs.jboss.org/hibernate/validator/5.2/reference/en-US/html/chapter-bean-constraints.html#section-builtin-constraints

.. code-block:: java

    public class Person {
        @NotEmpty
        private final String name;

        @NotEmpty
        @OneOf(value = {"m", "f"}, ignoreCase = true, ignoreWhitespace = true)
        // @OneOf forces a value to value within certain values.
        private final String gender;

        @Max(10)
        @Min(0)
        // The integer contained, if present, can attain a min value of 0 and a max of 10.
        private final Optional<Integer> animals;

        @JsonCreator
        public Person(@JsonProperty("name") String name) {
            this.name = name;
        }

        @JsonProperty("name")
        public String getName() {
            return name;
        }

        // Method that must return true for the object to be valid
        @ValidationMethod(message="name may not be Coda")
        @JsonIgnore
        public boolean isNotCoda() {
            return !"Coda".equals(name);
        }
    }

The reason why Dropwizard defines ``@ValidationMethod`` is that more complex validations (for
example, cross-field comparisons) are often hard to do using declarative annotations. Adding
``@ValidationMethod`` to any ``boolean``-returning method which begins with ``is`` is a short and
simple workaround:

.. note::

    Due to the rather daft JavaBeans conventions, when using ``@ValidationMethod``, the method must
    begin with ``is`` (e.g., ``#isValidPortRange()``. This is a limitation of Hibernate Validator,
    not Dropwizard.

.. _man-validation-annotations-validated:

Validating Grouped Constraints with ``@Validated``
**************************************************

The ``@Validated`` annotation allows for `validation groups`_ to be specifically set, instead of the
default group. This is useful when different endpoints share the same entity but may have different
requirements.

.. _validation groups: https://docs.jboss.org/hibernate/validator/5.2/reference/en-US/html/chapter-groups.html

Going back to our favorite ``Person`` class. Let's say we initially coded it such that ``name`` has
to be non-empty, but realized that business requirements needs the max length to be no more than 5.
Instead of blowing away our current version of our API and creating angry clients, we can accept
both versions of the API but at different endpoints.

.. code-block:: java

    public interface Version1Checks { }

    public interface Version2Checks { }

    public class Person {
        @NotEmpty(groups = Version1Checks.class)
        @Length(max = 5, groups = Version2Checks.class)
        private String name;

        @JsonCreator
        public Person(@JsonProperty("name") String name) {
            this.name = name;
        }

        @JsonProperty
        public String getName() {
            return name;
        }
    }

    @Path("/person")
    @Produces(MediaType.APPLICATION_JSON)
    public class PersonResource {
        @POST
        @Path("/v1")
        public void createPersonV1(@Valid @Validated(Version1Checks.class) Person person) {
        }

        @POST
        @Path("/v2")
        public void createPersonV2(@Valid @Validated({Version1Checks.class, Version2Checks.class}) Person person) {
        }
    }

Now, when clients hit ``/person/v1`` the ``Person`` entity will be checked by all the constraints
that are a part of the ``Version1Checks`` group. If ``/person/v2`` is hit, then all the validations
are performed.

.. note::

    Since interfaces can inherit other interfaces, ``Version2Checks`` can extend ``Version1Checks``
    and wherever ``@Validated(Version2Checks.class)`` is used, version 1 constraints are checked
    too.

.. _man-validation-testing:

Testing
=======

It is critical to test the constraints so that you can ensure the assumptions about the data hold
and see what kinds of error messages clients will receive for bad input. The recommended way for
testing annotations is through :ref:`Testing Resources <man-testing-resources>`, as Dropwizard does
a bit of magic behind the scenes when a constraint violation occurs to set the response's status
code and ensure that the error messages are user friendly.

.. code-block:: java

    @Test
    public void personNeedsAName() {
        // Tests what happens when a person with a null name is sent to
        // the endpoint.
        final Response post = resources.client()
                .target("/person/v1").request()
                .post(Entity.json(new Person(null)));

        // Clients will receive a 422 on bad request entity
        assertThat(post.getStatus()).isEqualTo(422);

        // Check to make sure that errors are correct and human readable
        ValidationErrorMessage msg = post.readEntity(ValidationErrorMessage.class);
        assertThat(msg.getErrors())
                .containsOnly("name may not be empty");
    }

.. _man-validation-extending:

Extending
=========

While Dropwizard provides good defaults for error messages, one size may not fit all and so there
are a series of extension points. To register your own
``ExceptionMapper<JerseyViolationException>`` you'll need to first set
``registerDefaultExceptionMappers`` to false in the configuration file or in code before registering
your exception mapper with jersey. Then, optionally, register other default exception mappers:

* ``LoggingExceptionMapper<Throwable>``
* ``JsonProcessingExceptionMapper``
* ``EarlyEofExceptionMapper``

If you need to validate entities outside of resource endpoints, the validator can be accessed in the
``Environment`` when the application is first ran.

.. code-block:: java

    Validator validator = environment.getValidator();
    Set<ConstraintViolation> errors = validator.validate(/* instance of class */)

The method used to determine what status code to return based on violations is
``ConstraintViolations.determineStatus``

The method used to determine the human friendly error message due to a constraint violation is
``ConstraintMessage.getMessage``.
