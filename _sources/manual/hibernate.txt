.. _man-hibernate:

####################
Dropwizard Hibernate
####################

.. highlight:: text

.. rubric:: The ``dropwizard-hibernate`` module provides you with managed access to Hibernate_, a
            powerful, industry-standard object-relation mapper (ORM).

.. _Hibernate: http://www.hibernate.org/

Configuration
=============

To create a :ref:`managed <man-core-managed>`, instrumented ``SessionFactory`` instance, your
:ref:`configuration class <man-core-configuration>` needs a ``DataSourceFactory`` instance:

.. code-block:: java

    public class ExampleConfiguration extends Configuration {
        @Valid
        @NotNull
        private DataSourceFactory database = new DataSourceFactory();

        @JsonProperty("database")
        public DataSourceFactory getDataSourceFactory() {
            return database;
        }
    }

Then, add a ``HibernateBundle`` instance to your application class, specifying your entity classes
and how to get a ``DataSourceFactory`` from your configuration subclass:

.. code-block:: java

    private final HibernateBundle<ExampleConfiguration> hibernate = new HibernateBundle<ExampleConfiguration>(Person.class) {
        @Override
        public DataSourceFactory getDataSourceFactory(ExampleConfiguration configuration) {
            return configuration.getDataSourceFactory();
        }
    };

    @Override
    public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
        bootstrap.addBundle(hibernate);
    }

    @Override
    public void run(ExampleConfiguration config, Environment environment) {
        final UserDAO dao = new UserDAO(hibernate.getSessionFactory());
        environment.jersey().register(new UserResource(dao));
    }

This will create a new :ref:`managed <man-core-managed>` connection pool to the database, a
:ref:`health check <man-core-healthchecks>` for connectivity to the database, and a new
``SessionFactory`` instance for you to use in your DAO classes.

Your application's configuration file will then look like this:

.. code-block:: yaml

    database:
      # the name of your JDBC driver
      driverClass: org.postgresql.Driver

      # the username
      user: pg-user

      # the password
      password: iAMs00perSecrEET

      # the JDBC URL
      url: jdbc:postgresql://db.example.com/db-prod

      # any properties specific to your JDBC driver:
      properties:
        charSet: UTF-8
        hibernate.dialect: org.hibernate.dialect.PostgreSQLDialect

      # the maximum amount of time to wait on an empty pool before throwing an exception
      maxWaitForConnection: 1s

      # the SQL query to run when validating a connection's liveness
      validationQuery: "/* MyApplication Health Check */ SELECT 1"

      # the minimum number of connections to keep open
      minSize: 8

      # the maximum number of connections to keep open
      maxSize: 32

      # whether or not idle connections should be validated
      checkConnectionWhileIdle: false

Usage
=====

Data Access Objects
-------------------

Dropwizard comes with ``AbstractDAO``, a minimal template for entity-specific DAO classes. It
contains type-safe wrappers for most of ``SessionFactory``'s common operations:

.. code-block:: java

    public class PersonDAO extends AbstractDAO<Person> {
        public PersonDAO(SessionFactory factory) {
            super(factory);
        }

        public Person findById(Long id) {
            return get(id);
        }

        public long create(Person person) {
            return persist(person).getId();
        }

        public List<Person> findAll() {
            return list(namedQuery("com.example.helloworld.core.Person.findAll"));
        }
    }

Transactional Resource Methods
------------------------------

Dropwizard uses a declarative method of scoping transactional boundaries. Not all resource methods
actually require database access, so the ``@UnitOfWork`` annotation is provided:

.. code-block:: java

    @GET
    @Timed
    @UnitOfWork
    public Person findPerson(@PathParam("id") LongParam id) {
        return dao.findById(id.get());
    }

This will automatically open a session, begin a transaction, call ``findById``, commit the
transaction, and finally close the session. If an exception is thrown, the transaction is rolled
back.

.. important:: The Hibernate session is closed **before** your resource method's return value (e.g.,
               the ``Person`` from the database), which means your resource method (or DAO) is
               responsible for initializing all lazily-loaded collections, etc., before returning.
               Otherwise, you'll get a ``LazyInitializationException`` thrown in your template (or
               ``null`` values produced by Jackson).

Prepended Comments
==================

Dropwizard automatically configures Hibernate to prepend a comment describing the context of all
queries:

.. code-block:: sql

    /* load com.example.helloworld.core.Person */
    select
        person0_.id as id0_0_,
        person0_.fullName as fullName0_0_,
        person0_.jobTitle as jobTitle0_0_
    from people person0_
    where person0_.id=?

This will allow you to quickly determine the origin of any slow or misbehaving queries.
