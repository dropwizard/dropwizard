.. _man-jdbi:

###############
Dropwizard JDBI
###############

.. highlight:: text

.. rubric:: The ``dropwizard-jdbi`` module provides you with managed access to JDBI_, a flexible and
            modular library for interacting with relational databases via SQL.

.. _JDBI: http://jdbi.org/

Configuration
=============

To create a :ref:`managed <man-core-managed>`, instrumented ``DBI`` instance, your
:ref:`configuration class <man-core-configuration>` needs a ``DatabaseConfiguration`` instance:

.. code-block:: java

    public class ExampleConfiguration extends Configuration {
        @Valid
        @NotNull
        @JsonProperty
        private DatabaseConfiguration database = new DatabaseConfiguration();

        public DatabaseConfiguration getDatabaseConfiguration() {
            return database;
        }
    }

Then, in your service's ``run`` method, create a new ``DBIFactory``:

.. code-block:: java

    @Override
    public void run(ExampleConfiguration config,
                    Environment environment) throws ClassNotFoundException {
        final DBIFactory factory = new DBIFactory(environment);
        final DBI jdbi = factory.build(config.getDatabaseConfiguration(), "postgresql");
        final UserDAO dao = jdbi.onDemand(UserDAO.class);
        environment.addResource(new UserResource(dao));
    }

This will create a new :ref:`managed <man-core-managed>` connection pool to the database, a
:ref:`health check <man-core-healthchecks>` for connectivity to the database, and a new ``DBI``
instance for you to use. Note the ``ClassNotFoundException`` is thrown by the ``DBIFactory`` class
when the ``build`` method is unable to locate the JDBC driver class. This will cause the service to
exit displaying the output of the exception.

Your service's configuration file will then look like this:

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

      # the maximum amount of time to wait on an empty pool before throwing an exception
      maxWaitForConnection: 1s

      # the SQL query to run when validating a connection's liveness
      validationQuery: "/* MyService Health Check */ SELECT 1"

      # the minimum number of connections to keep open
      minSize: 8

      # the maximum number of connections to keep open
      maxSize: 32

      # whether or not idle connections should be validated
      checkConnectionWhileIdle: false

      # how long a connection must be held before it can be validated
      checkConnectionHealthWhenIdleFor: 10s

      # the maximum lifetime of an idle connection
      closeConnectionIfIdleFor: 1 minute

Usage
=====

We highly recommend you use JDBI's `SQL Objects API`_, which allows you to write DAO classes as
interfaces:

.. _SQL Objects API: http://jdbi.org/sql_object_overview/

.. code-block:: java

    public interface MyDAO {
      @SqlUpdate("create table something (id int primary key, name varchar(100))")
      void createSomethingTable();

      @SqlUpdate("insert into something (id, name) values (:id, :name)")
      void insert(@Bind("id") int id, @Bind("name") String name);

      @SqlQuery("select name from something where id = :id")
      String findNameById(@Bind("id") int id);
    }

    final MyDAO dao = database.onDemand(MyDAO.class);

This ensures your DAO classes are trivially mockable, as well as encouraging you to extract mapping
code (e.g., ``ResultSet`` -> domain objects) into testable, reusable classes.

Exception Handling
==================

By adding the ``DBIExceptionsBundle`` to your :ref:`service <man-core-service>`, your Dropwizard
application will automatically unwrap any thrown ``SQLException`` or ``DBIException`` instances.
This is critical for debugging, since otherwise only the common wrapper exception's stack trace is
logged.

Prepended Comments
==================

If you're using JDBI's `SQL Objects API`_ (and you should be), ``dropwizard-jdbi`` will
automatically prepend the SQL object's class and method name to the SQL query as an SQL comment:

.. code-block:: sql

    /* com.example.service.dao.UserDAO.findByName */
    SELECT id, name, email
    FROM users
    WHERE name = 'Coda';

This will allow you to quickly determine the origin of any slow or misbehaving queries.

Guava Support
=============

``dropwizard-jdbi`` supports ``Optional<T>`` arguments and ``ImmutableList<T>`` and
``ImmutableSet<T>`` query results.
