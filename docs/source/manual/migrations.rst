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
``DatabaseConfiguration`` instance:

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

Adding The Bundle
=================

Then, in your service's ``initialize`` method, add a new ``MigrationsBundle`` subclass:

.. code-block:: java

    @Override
    public void initialize(Bootstrap<MyConfiguration> bootstrap) {
        bootstrap.addBundle(new MigrationsBundle<MyConfiguration>() {
            @Override
            public DatabaseConfiguration getDatabaseConfiguration(MyConfiguration configuration) {
                return configuration.getDatabaseConfiguration();
            }
        });
    }

Defining Migrations
===================

Your database migrations are stored in your Dropwizard project, in
``src/main/resources/migrations.xml``. This file will be packaged with your service, allowing you to
run migrations using your service's command-line interface.

For example, to create a new ``people`` table, I might create an initial ``migrations.xml`` like
this:

.. code-block:: xml

    <?xml version="1.0" encoding="UTF-8"?>

    <databaseChangeLog
            xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
             http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-2.0.xsd">

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

This will output a Liquibase_ change log with a change set capable of recreating your database.

Tagging Your Schema
===================

To tag your schema at a particular point in time (e.g., to make rolling back easier), use the
``db tag`` command:

.. code-block:: text

    java -jar hello-world.jar db tag helloworld.yml 2012-10-08-pre-user-move

Migrating Your Schema
=====================

To apply pending change sets to your database schema, run the ``db migrate`` comnand:

.. code-block:: text

    java -jar hello-world.jar db migrate helloworld.yml

.. warning::

    This will potentially make irreversible changes to your database. Always check the pending DDL
    scripts by using the ``--dry-run`` flag first. This will output the SQL to be run to stdout.

.. note::

    To apply only a specific number of pending change sets, use the ``--count`` flag.

Rolling Back Your Schema
========================

To roll back change sets which have already been applied, run the ``db rollback`` command. You will
need to specify either a **tag**, a **date**, or a **number of change sets** to roll back to:

.. code-block:: text

    java -jar hello-world.jar db rollback helloworld.yml --tag 2012-10-08-pre-user-move

.. warning::

    This will potentially make irreversible changes to your database. Always check the pending DDL
    scripts by using the ``--dry-run`` flag first. This will output the SQL to be run to stdout.

Testing Migrations
==================

To verify that a set of pending change sets can be fully rolled back, use the ``db test`` command,
which will migrate forward, roll back to the original state, then migrate forward again:

.. code-block:: text

    java -jar hello-world.jar db test helloworld.yml

.. warning::

    Do not run this in production, for obvious reasons.

Preparing A Rollback Script
===========================

To prepare a rollback script for pending change sets *before* they have been applied, use the
``db prepare-rollback`` command:

.. code-block:: text

    java -jar hello-world.jar db prepare-rollback helloworld.yml

This will output a DDL script to stdout capable of rolling back all unapplied change sets.

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

Fast-Forwarding Through A Change Set
====================================

To mark a pending change set as applied (e.g., after having backfilled your ``migrations.xml`` with
``db dump``), use the ``db fast-forward`` command:

.. code-block:: text

     java -jar hello-world.jar db fast-forward helloworld.yml

This will mark the next pending change set as applied. You can also use the ``--all`` flag to mark
all pending change sets as applied.

More Information
================

For more information on available commands, either use the ``db --help`` command, or for more
detailed help on a specific command, use ``db <cmd> --help``.
