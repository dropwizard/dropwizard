.. _man-migrations:

#####################
Dropwizard Migrations
#####################

.. highlight:: text

.. rubric:: The ``dropwizard-migrations`` module provides you with a wrapper for Liquibase_ database
            refactoring.

.. _Liquibase: http://www.liquibase.org

Configuration
=============

Like :ref:`man-jdbi`, your :ref:`configuration class <man-core-configuration>` needs a
``DataSourceFactory`` instance:

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

Adding The Bundle
=================

Then, in your application's ``initialize`` method, add a new ``MigrationsBundle`` subclass:

.. code-block:: java

    @Override
    public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
        bootstrap.addBundle(new MigrationsBundle<ExampleConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(ExampleConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        });
    }

Defining Migrations
===================

Your database migrations are stored in your Dropwizard project, in
``src/main/resources/migrations.xml``. This file will be packaged with your application, allowing you to
run migrations using your application's command-line interface.

For example, to create a new ``people`` table, you might create an initial ``migrations.xml`` like
this:

.. code-block:: xml

    <?xml version="1.0" encoding="UTF-8"?>

    <databaseChangeLog
            xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
             http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.1.xsd">

        <changeSet id="1" author="codahale">
            <createTable tableName="people">
                <column name="id" type="bigint" autoIncrement="true">
                    <constraints primaryKey="true" nullable="false"/>
                </column>
                <column name="fullName" type="varchar(255)">
                    <constraints nullable="false"/>
                </column>
                <column name="jobTitle" type="varchar(255)"/>
            </createTable>
        </changeSet>
    </databaseChangeLog>

For more information on available database refactorings, check the Liquibase_ documentation.

Checking Your Database's State
==============================

To check the state of your database, use the ``db status`` command:

.. code-block:: text

    java -jar hello-world.jar db status helloworld.yml

Dumping Your Schema
===================

If your database already has an existing schema and you'd like to pre-seed your ``migrations.xml``
document, you can run the ``db dump`` command:

.. code-block:: text

    java -jar hello-world.jar db dump helloworld.yml

This will output a Liquibase_ change log with a changeset capable of recreating your database.

Tagging Your Schema
===================

To tag your schema at a particular point in time (e.g., to make rolling back easier), use the
``db tag`` command:

.. code-block:: text

    java -jar hello-world.jar db tag helloworld.yml 2012-10-08-pre-user-move

Migrating Your Schema
=====================

To apply pending changesets to your database schema, run the ``db migrate`` command:

.. code-block:: text

    java -jar hello-world.jar db migrate helloworld.yml

.. warning::

    This will potentially make irreversible changes to your database. Always check the pending DDL
    scripts by using the ``--dry-run`` flag first. This will output the SQL to be run to stdout.

.. note::

    To apply only a specific number of pending changesets, use the ``--count`` flag.

Rolling Back Your Schema
========================

To roll back changesets which have already been applied, run the ``db rollback`` command. You will
need to specify either a **tag**, a **date**, or a **number of changesets** to roll back to:

.. code-block:: text

    java -jar hello-world.jar db rollback helloworld.yml --tag 2012-10-08-pre-user-move

.. warning::

    This will potentially make irreversible changes to your database. Always check the pending DDL
    scripts by using the ``--dry-run`` flag first. This will output the SQL to be run to stdout.

Testing Migrations
==================

To verify that a set of pending changesets can be fully rolled back, use the ``db test`` command,
which will migrate forward, roll back to the original state, then migrate forward again:

.. code-block:: text

    java -jar hello-world.jar db test helloworld.yml

.. warning::

    Do not run this in production, for obvious reasons.

Preparing A Rollback Script
===========================

To prepare a rollback script for pending changesets *before* they have been applied, use the
``db prepare-rollback`` command:

.. code-block:: text

    java -jar hello-world.jar db prepare-rollback helloworld.yml

This will output a DDL script to stdout capable of rolling back all unapplied changesets.

Generating Documentation
========================

To generate HTML documentation on the current status of the database, use the ``db generate-docs``
command:

.. code-block:: text

     java -jar hello-world.jar db generate-docs helloworld.yml ~/db-docs/

Dropping All Objects
====================

To drop all objects in the database, use the ``db drop-all`` command:

.. code-block:: text

     java -jar hello-world.jar db drop-all --confirm-delete-everything helloworld.yml

.. warning::

    You need to specify the ``--confirm-delete-everything`` flag because this command **deletes
    everything in the database**. Be sure you want to do that first.

Fast-Forwarding Through A Changeset
====================================

To mark a pending changeset as applied (e.g., after having backfilled your ``migrations.xml`` with
``db dump``), use the ``db fast-forward`` command:

.. code-block:: text

     java -jar hello-world.jar db fast-forward helloworld.yml

This will mark the next pending changeset as applied. You can also use the ``--all`` flag to mark
all pending changesets as applied.

Support For Adding Multiple Migration Bundles
=============================================

Assuming migrations need to be done for two different databases, you would need to have two different data source factories:

.. code-block:: java

    public class ExampleConfiguration extends Configuration {
        @Valid
        @NotNull
        private DataSourceFactory database1 = new DataSourceFactory();

        @Valid
        @NotNull
        private DataSourceFactory database2 = new DataSourceFactory();

        @JsonProperty("database1")
        public DataSourceFactory getDb1DataSourceFactory() {
            return database1;
        }

        @JsonProperty("database2")
        public DataSourceFactory getDb2DataSourceFactory() {
            return database2;
        }
    }

Now multiple migration bundles can be added with unique names like so:

.. code-block:: java

    @Override
    public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
        bootstrap.addBundle(new MigrationsBundle<ExampleConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(ExampleConfiguration configuration) {
                return configuration.getDb1DataSourceFactory();
            }

            @Override
            public String name() {
                return "db1";
            }
        });

        bootstrap.addBundle(new MigrationsBundle<ExampleConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(ExampleConfiguration configuration) {
                return configuration.getDb2DataSourceFactory();
            }

            @Override
            public String name() {
                return "db2";
            }
        });
    }

To migrate your schema:

.. code-block:: text

    java -jar hello-world.jar db1 migrate helloworld.yml

and

.. code-block:: text

    java -jar hello-world.jar db2 migrate helloworld.yml

.. note::

    Whenever a name is added to a migration bundle, it becomes the command that needs to be run at the command line.
    eg: To check the state of your database, use the ``status`` command:

.. code-block:: text

    java -jar hello-world.jar db1 status helloworld.yml

or

.. code-block:: text

    java -jar hello-world.jar db2 status helloworld.yml

By default the migration bundle uses the "db" command. By overriding you can customize it to provide any name you want
and have multiple migration bundles. Wherever the "db" command was being used, this custom name can be used.

There will also be a need to provide different change log migration files as well. This can be done as

.. code-block:: text

    java -jar hello-world.jar db1 migrate helloworld.yml --migrations <path_to_db1_migrations.xml>

.. code-block:: text

    java -jar hello-world.jar db2 migrate helloworld.yml --migrations <path_to_db2_migrations.xml>

More Information
================

If you are using databases supporting multiple schemas like PostgreSQL, Oracle, or H2, you can use the
optional ``--catalog`` and ``--schema`` arguments to specify the database catalog and schema used for the
Liquibase commands.

For more information on available commands, either use the ``db --help`` command, or for more
detailed help on a specific command, use ``db <cmd> --help``.
