.. _man-jdbi3:

################
Dropwizard JDBI3
################

.. highlight:: text

.. rubric:: The ``dropwizard-jdbi3`` module provides you with managed access to JDBI_, a flexible and
            modular library for interacting with relational databases via SQL.

.. _JDBI: http://jdbi.org/

Configuration
=============

To create a :ref:`managed <man-core-managed>`, instrumented ``Jdbi`` instance, your
:ref:`configuration class <man-core-configuration>` needs a ``DataSourceFactory`` instance:

.. code-block:: java

    public class ExampleConfiguration extends Configuration {
        @Valid
        @NotNull
        private DataSourceFactory database = new DataSourceFactory();

        @JsonProperty("database")
        public void setDataSourceFactory(DataSourceFactory factory) {
            this.database = factory;
        }

        @JsonProperty("database")
        public DataSourceFactory getDataSourceFactory() {
            return database;
        }
    }

Then, in your service's ``run`` method, create a new ``JdbiFactory``:

.. code-block:: java

    @Override
    public void run(ExampleConfiguration config, Environment environment) {
        final JdbiFactory factory = new JdbiFactory();
        final Jdbi jdbi = factory.build(environment, config.getDataSourceFactory(), "postgresql");
        environment.jersey().register(new UserResource(jdbi));
    }

This will create a new :ref:`managed <man-core-managed>` connection pool to the database, a
:ref:`health check <man-core-healthchecks>` for connectivity to the database, and a new ``Jdbi``
instance for you to use.

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

      # the timeout before a connection validation queries fail
      validationQueryTimeout: 3s

      # the minimum number of connections to keep open
      minSize: 8

      # the maximum number of connections to keep open
      maxSize: 32

      # whether or not idle connections should be validated
      checkConnectionWhileIdle: false

      # the amount of time to sleep between runs of the idle connection validation, abandoned cleaner and idle pool resizing
      evictionInterval: 10s

      # the minimum amount of time an connection must sit idle in the pool before it is eligible for eviction
      minIdleTime: 1 minute

Plugins
=======

JDBI3 is built using plugins_ to add features to its core implementation.
Dropwizard adds the sqlobject_ and guava_ plugins by default, but you are free
to add other existing plugins you might need or create your own.

.. _plugins: http://jdbi.org/#_third_party_integration
.. _sqlobject: http://jdbi.org/#_sql_objects
.. _guava: http://jdbi.org/#_google_guava

Usage
=====

We highly recommend you use JDBI's `SQL Objects API`_, which allows you to write DAO classes as
interfaces:

.. _SQL Objects API: `sqlobject`_

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
code (e.g., ``RowMapper`` -> domain objects) into testable, reusable classes.

Exception Handling
==================

By adding the ``JdbiExceptionsBundle`` to your :ref:`application <man-core-application>`, Dropwizard
will automatically unwrap any thrown ``SQLException`` or ``JdbiException`` instances.
This is critical for debugging, since otherwise only the common wrapper exception's stack trace is
logged.

Prepended Comments
==================

If you're using JDBI's `SQL Objects API`_ (and you should be), ``dropwizard-jdbi3`` will
automatically prepend the SQL object's class and method name to the SQL query as an SQL comment:

.. code-block:: sql

    /* com.example.service.dao.UserDAO.findByName */
    SELECT id, name, email
    FROM users
    WHERE name = 'Coda';

This will allow you to quickly determine the origin of any slow or misbehaving queries.
