.. _man-core:

###############
Dropwizard Core
###############

.. highlight:: text

.. rubric:: The ``dropwizard-core`` module provides you with everything you'll need for most of your
            applications.

It includes:

* Jetty, a high-performance HTTP server.
* Jersey, a full-featured RESTful web framework.
* Jackson, the best JSON library for the JVM.
* Metrics, an excellent library for application metrics.
* Guava, Google's excellent utility library.
* Logback, the successor to Log4j, Java's most widely-used logging framework.
* Hibernate Validator, the reference implementation of the Java Bean Validation standard.

Dropwizard consists mostly of glue code to automatically connect and configure these components.

.. _man-core-organization:

Organizing Your Project
=======================

In general, we recommend you separate your projects into three Maven modules: ``project-api``,
``project-client``, and ``project-application``.

``project-api`` should contain your :ref:`man-core-representations`; ``project-client`` should use
those classes and an :ref:`HTTP client <man-client>` to implement a full-fledged client for your
application, and ``project-application`` should provide the actual application implementation, including
:ref:`man-core-resources`.

Our applications tend to look like this:

* ``com.example.myapplication``:

  * ``api``: :ref:`man-core-representations`.
  * ``cli``: :ref:`man-core-commands`
  * ``client``: :ref:`Client <man-client>` implementation for your application
  * ``core``: Domain implementation
  * ``jdbi``: :ref:`Database <man-jdbi>` access classes
  * ``health``: :ref:`man-core-healthchecks`
  * ``resources``: :ref:`man-core-resources`
  * ``MyApplication``: The :ref:`application <man-core-application>` class
  * ``MyApplicationConfiguration``: :ref:`configuration <man-core-configuration>` class

.. _man-core-application:

Application
===========

The main entry point into a Dropwizard application is, unsurprisingly, the ``Application`` class. Each
``Application`` has a **name**, which is mostly used to render the command-line interface. In the
constructor of your ``Application`` you can add :ref:`man-core-bundles` and :ref:`man-core-commands` to
your application.

.. _man-core-configuration:

Configuration
=============

Dropwizard provides a number of built-in configuration parameters. They are
well documented in the `example project's configuration`__.

.. __: https://github.com/dropwizard/dropwizard/blob/master/dropwizard-example/example.yml

Each ``Application`` subclass has a single type parameter: that of its matching ``Configuration``
subclass. These are usually at the root of your application's main package. For example, your User
application would have two classes: ``UserApplicationConfiguration``, extending ``Configuration``, and
``UserApplication``, extending ``Application<UserApplicationConfiguration>``.

When your application runs :ref:`man-core-commands-configured` like the ``server`` command, Dropwizard
parses the provided YAML configuration file and builds an instance of your application's configuration
class by mapping YAML field names to object field names.

.. note::

    If your configuration file doesn't end in ``.yml`` or ``.yaml``, Dropwizard tries to parse it
    as a JSON file.

To keep your configuration file and class manageable, we recommend grouping related
configuration parameters into independent configuration classes. If your application requires a set of
configuration parameters in order to connect to a message queue, for example, we recommend that you
create a new ``MessageQueueFactory`` class:

.. code-block:: java

    public class MessageQueueFactory {
        @NotEmpty
        private String host;

        @Min(1)
        @Max(65535)
        private int port = 5672;

        @JsonProperty
        public String getHost() {
            return host;
        }

        @JsonProperty
        public void setHost(String host) {
            this.host = host;
        }

        @JsonProperty
        public int getPort() {
            return port;
        }

        @JsonProperty
        public void setPort(int port) {
            this.port = port;
        }

        public MessageQueueClient build(Environment environment) {
            MessageQueueClient client = new MessageQueueClient(getHost(), getPort());
            environment.lifecycle().manage(new Managed() {
                @Override
                public void start() {
                }

                @Override
                public void stop() {
                    client.close();
                }
            });
            return client;
        }
    }

In this example our factory will automatically tie our ``MessageQueueClient`` connection to the
lifecycle of our application's ``Environment``.

Your main ``Configuration`` subclass can then include this as a member field:

.. code-block:: java

    public class ExampleConfiguration extends Configuration {
        @Valid
        @NotNull
        private MessageQueueFactory messageQueue = new MessageQueueFactory();

        @JsonProperty("messageQueue")
        public MessageQueueFactory getMessageQueueFactory() {
            return messageQueue;
        }

        @JsonProperty("messageQueue")
        public void setMessageQueueFactory(MessageQueueFactory factory) {
            this.messageQueue = factory;
        }
    }

And your ``Application`` subclass can then use your factory to directly construct a client for the
message queue:

.. code-block:: java

    public void run(ExampleConfiguration configuration,
                    Environment environment) {
        MessageQueueClient messageQueue = configuration.getMessageQueueFactory().build(environment);
    }

Then, in your application's YAML file, you can use a nested ``messageQueue`` field:

.. code-block:: java

    messageQueue:
      host: mq.example.com
      port: 5673

The ``@NotNull``, ``@NotEmpty``, ``@Min``, ``@Max``, and ``@Valid`` annotations are part of
:ref:`man-validation` functionality. If your YAML configuration file's
``messageQueue.host`` field was missing (or was a blank string), Dropwizard would refuse to start
and would output an error message describing the issues.

Once your application has parsed the YAML file and constructed its ``Configuration`` instance,
Dropwizard then calls your ``Application`` subclass to initialize your application's ``Environment``.

.. note::

    You can override configuration settings by passing special Java system properties when starting
    your application. Overrides must start with prefix ``dw.``, followed by the path to the
    configuration value being overridden.

    For example, to override the Logging level, you could start your application like this:

    ``java -Ddw.logging.level=DEBUG server my-config.json``

    This will work even if the configuration setting in question does not exist in your config file, in
    which case it will get added.

    You can override configuration settings in arrays of objects like this:

    ``java -Ddw.server.applicationConnectors[0].port=9090 server my-config.json``

    You can override configuration settings in maps like this:

    ``java -Ddw.database.properties.hibernate.hbm2ddl.auto=none server my-config.json``

    You can also override a configuration setting that is an array of strings by using the ',' character
    as an array element separator. For example, to override a configuration setting myapp.myserver.hosts
    that is an array of strings in the configuration, you could start your service like this:
    ``java -Ddw.myapp.myserver.hosts=server1,server2,server3 server my-config.json``

    If you need to use the ',' character in one of the values, you can escape it by using '\,' instead.

    The array override facility only handles configuration elements that are arrays of simple strings.
    Also, the setting in question must already exist in your configuration file as an array;
    this mechanism will not work if the configuration key being overridden does not exist in your configuration
    file. If it does not exist or is not an array setting, it will get added as a simple string setting, including
    the ',' characters as part of the string.

.. _man-core-environment-variables:

Environment variables
---------------------

The ``dropwizard-configuration`` module also provides the capabilities to substitute configuration settings with the
value of environment variables using a ``SubstitutingSourceProvider`` and ``EnvironmentVariableSubstitutor``.

.. code-block:: java

    public class MyApplication extends Application<MyConfiguration> {
        // [...]
        @Override
        public void initialize(Bootstrap<MyConfiguration> bootstrap) {
            // Enable variable substitution with environment variables
            bootstrap.setConfigurationSourceProvider(
                    new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                                                       new EnvironmentVariableSubstitutor(false)
                    )
            );

        }

        // [...]
    }

The configuration settings which should be substituted need to be explicitly written in the configuration file and
follow the substitution rules of StrSubstitutor_ from the Apache Commons Lang library.

.. code-block:: yaml

    mySetting: ${DW_MY_SETTING}
    defaultSetting: ${DW_DEFAULT_SETTING:-default value}

In general ``SubstitutingSourceProvider`` isn't restricted to substitute environment variables but can be used to replace
variables in the configuration source with arbitrary values by passing a custom ``StrSubstitutor`` implementation.

.. _StrSubstitutor: https://commons.apache.org/proper/commons-lang/javadocs/api-release/org/apache/commons/lang3/text/StrSubstitutor.html

.. _man-core-ssl:

SSL
---

SSL support is built into Dropwizard. You will need to provide your own java
keystore, which is outside the scope of this document (``keytool`` is the
command you need). There is a test keystore you can use in the
`Dropwizard example project`__.

.. __: https://github.com/dropwizard/dropwizard/tree/master/dropwizard-example

.. code-block:: yaml

    server:
      applicationConnectors:
        - type: https
          port: 8443
          keyStorePath: example.keystore
          keyStorePassword: example
          validateCerts: false


.. _man-core-bootstrapping:

Bootstrapping
=============

Before a Dropwizard application can provide the command-line interface, parse a configuration file, or
run as a server, it must first go through a bootstrapping phase. This phase corresponds to your
``Application`` subclass's ``initialize`` method. You can add :ref:`man-core-bundles`,
:ref:`man-core-commands`, or register Jackson modules to allow you to include custom types as part
of your configuration class.


.. _man-core-environments:

Environments
============

A Dropwizard ``Environment`` consists of all the :ref:`man-core-resources`, servlets, filters,
:ref:`man-core-healthchecks`, Jersey providers, :ref:`man-core-managed`, :ref:`man-core-tasks`, and
Jersey properties which your application provides.

Each ``Application`` subclass implements a ``run`` method. This is where you should be creating new
resource instances, etc., and adding them to the given ``Environment`` class:

.. code-block:: java

    @Override
    public void run(ExampleConfiguration config,
                    Environment environment) {
        // encapsulate complicated setup logic in factories
        final Thingy thingy = config.getThingyFactory().build();

        environment.jersey().register(new ThingyResource(thingy));
        environment.healthChecks().register("thingy", new ThingyHealthCheck(thingy));
    }

It's important to keep the ``run`` method clean, so if creating an instance of something is
complicated, like the ``Thingy`` class above, extract that logic into a factory.

.. _man-core-healthchecks:

Health Checks
=============

A health check is a runtime test which you can use to verify your application's behavior in its
production environment. For example, you may want to ensure that your database client is connected
to the database:

.. code-block:: java

    public class DatabaseHealthCheck extends HealthCheck {
        private final Database database;

        public DatabaseHealthCheck(Database database) {
            this.database = database;
        }

        @Override
        protected Result check() throws Exception {
            if (database.isConnected()) {
                return Result.healthy();
            } else {
                return Result.unhealthy("Cannot connect to " + database.getUrl());
            }
        }
    }

You can then add this health check to your application's environment:

.. code-block:: java

    environment.healthChecks().register("database", new DatabaseHealthCheck(database));

By sending a ``GET`` request to ``/healthcheck`` on the admin port you can run these tests and view
the results::

    $ curl http://dw.example.com:8081/healthcheck
    {"deadlocks":{"healthy":true},"database":{"healthy":true}}

If all health checks report success, a ``200 OK`` is returned. If any fail, a
``500 Internal Server Error`` is returned with the error messages and exception stack traces (if an
exception was thrown).

All Dropwizard applications ship with the ``deadlocks`` health check installed by default, which uses
Java 1.6's built-in thread deadlock detection to determine if any threads are deadlocked.

.. _man-core-managed:

Managed Objects
===============

Most applications involve objects which need to be started and stopped: thread pools, database
connections, etc. Dropwizard provides the ``Managed`` interface for this. You can either have the
class in question implement the ``#start()`` and ``#stop()`` methods, or write a wrapper class which
does so. Adding a ``Managed`` instance to your application's ``Environment`` ties that object's
lifecycle to that of the application's HTTP server. Before the server starts, the ``#start()`` method is
called. After the server has stopped (and after its graceful shutdown period) the ``#stop()`` method
is called.

For example, given a theoretical Riak__ client which needs to be started and stopped:

.. __: http://basho.com/products/

.. code-block:: java

    public class RiakClientManager implements Managed {
        private final RiakClient client;

        public RiakClientManager(RiakClient client) {
            this.client = client;
        }

        @Override
        public void start() throws Exception {
            client.start();
        }

        @Override
        public void stop() throws Exception {
            client.stop();
        }
    }

.. code-block:: java

    public class MyApplication extends Application<MyConfiguration>{
        @Override
        public void run(MyApplicationConfiguration configuration, Environment environment) {
            RiakClient client = ...;
            RiakClientManager riakClientManager = new RiakClientManager(client);
            environment.lifecycle().manage(riakClientManager);
        }
    }

If ``RiakClientManager#start()`` throws an exception--e.g., an error connecting to the server--your
application will not start and a full exception will be logged. If ``RiakClientManager#stop()`` throws
an exception, the exception will be logged but your application will still be able to shut down.

It should be noted that ``Environment`` has built-in factory methods for ``ExecutorService`` and
``ScheduledExecutorService`` instances which are managed. See ``LifecycleEnvironment#executorService``
and ``LifecycleEnvironment#scheduledExecutorService`` for details.

.. _man-core-bundles:

Bundles
=======

A Dropwizard bundle is a reusable group of functionality, used to define blocks of an application's
behavior. For example, ``AssetBundle`` from the ``dropwizard-assets`` module provides a simple way
to serve static assets from your application's ``src/main/resources/assets`` directory as files
available from ``/assets/*`` (or any other path) in your application.

Configured Bundles
------------------

Some bundles require configuration parameters. These bundles implement ``ConfiguredBundle`` and will
require your application's ``Configuration`` subclass to implement a specific interface.


For example: given the configured bundle ``MyConfiguredBundle`` and the interface ``MyConfiguredBundleConfig`` below.
Your application's ``Configuration`` subclass would need to implement ``MyConfiguredBundleConfig``.

.. code-block:: java

    public class MyConfiguredBundle implements ConfiguredBundle<MyConfiguredBundleConfig>{

        @Override
        public void run(MyConfiguredBundleConfig applicationConfig, Environment environment) {
            applicationConfig.getBundleSpecificConfig();
        }

        @Override
        public void initialize(Bootstrap<?> bootstrap) {

        }
    }

    public interface MyConfiguredBundleConfig{

        String getBundleSpecificConfig();

    }


Serving Assets
--------------

Either your application or your static assets can be served from the root path, but
not both. The latter is useful when using Dropwizard to back a Javascript
application. To enable it, move your application to a sub-URL.

.. code-block:: yaml

    server:
      rootPath: /api/

.. note::

    If you use the :ref:`man-configuration-simple` server configuration, then ``rootPath`` is calculated relatively  from
    ``applicationContextPath``. So, your API will be accessible from the path ``/application/api/``


Then use an extended ``AssetsBundle`` constructor to serve resources in the
``assets`` folder from the root path. ``index.htm`` is served as the default
page.

.. code-block:: java

    @Override
    public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/assets/", "/"));
    }

When an ``AssetBundle`` is added to the application, it is registered as a servlet
using a default name of ``assets``. If the application needs to have multiple ``AssetBundle``
instances, the extended constructor should be used to specify a unique name for the ``AssetBundle``.

.. code-block:: java

    @Override
    public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/assets/css", "/css", null, "css"));
        bootstrap.addBundle(new AssetsBundle("/assets/js", "/js", null, "js"));
        bootstrap.addBundle(new AssetsBundle("/assets/fonts", "/fonts", null, "fonts"));
    }

.. _man-core-commands:

Commands
========

Commands are basic actions which Dropwizard runs based on the arguments provided on the command
line. The built-in ``server`` command, for example, spins up an HTTP server and runs your application.
Each ``Command`` subclass has a name and a set of command line options which Dropwizard will use to
parse the given command line arguments.

Below is an example on how to add a command and have Dropwizard recognize it.

.. code-block:: java

    public class MyCommand extends Command {
        public MyCommand() {
            // The name of our command is "hello" and the description printed is
            // "Prints a greeting"
            super("hello", "Prints a greeting");
        }

        @Override
        public void configure(Subparser subparser) {
            // Add a command line option
            subparser.addArgument("-u", "--user")
                    .dest("user")
                    .type(String.class)
                    .required(true)
                    .help("The user of the program");
        }

        @Override
        public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
            System.out.println("Hello " + namespace.getString("user"));
        }
    }

Dropwizard recognizes our command once we add it in the ``initialize`` stage of our application.

.. code-block:: java

    public class MyApplication extends Application<MyConfiguration>{
        @Override
        public void initialize(Bootstrap<DropwizardConfiguration> bootstrap) {
            bootstrap.addCommand(new MyCommand());
        }
    }

To invoke the new functionality, run the following:

.. code-block:: text

    java -jar <jarfile> hello dropwizard

.. _man-core-commands-configured:

Configured Commands
-------------------

Some commands require access to configuration parameters and should extend the ``ConfiguredCommand``
class, using your application's ``Configuration`` class as its type parameter. By default,
Dropwizard will treat the last argument on the command line as the path to a YAML configuration
file, parse and validate it, and provide your command with an instance of the configuration class.

A ``ConfiguredCommand`` can have additional command line options specified, while keeping the last
argument the path to the YAML configuration.

.. code-block:: java

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        // Add a command line option
        subparser.addArgument("-u", "--user")
                .dest("user")
                .type(String.class)
                .required(true)
                .help("The user of the program");
    }

For more advanced customization of the command line (for example, having the configuration file
location specified by ``-c``), adapt the ConfiguredCommand_ class as needed.

.. _ConfiguredCommand: https://github.com/dropwizard/dropwizard/blob/master/dropwizard-core/src/main/java/io/dropwizard/cli/ConfiguredCommand.java

.. _man-core-tasks:

Tasks
=====

A ``Task`` is a run-time action your application provides access to on the administrative port via HTTP.
All Dropwizard applications start with: the ``gc`` task, which explicitly triggers the JVM's garbage
collection (This is useful, for example, for running full garbage collections during off-peak times
or while the given application is out of rotation.); and the ``log-level`` task, which configures the level
of any number of loggers at runtime (akin to Logback's ``JmxConfigurator``). The execute method of a ``Task``
can be annotated with ``@Timed``, ``@Metered``, and ``@ExceptionMetered``. Dropwizard will automatically
record runtime information about your tasks. Here's a basic task class:

.. code-block:: java

    public class TruncateDatabaseTask extends Task {
        private final Database database;

        public TruncateDatabaseTask(Database database) {
            super("truncate");
            this.database = database;
        }

          @Override
        public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
            this.database.truncate();
        }
    }

You can then add this task to your application's environment:

.. code-block:: java

    environment.admin().addTask(new TruncateDatabaseTask(database));

Running a task can be done by sending a ``POST`` request to ``/tasks/{task-name}`` on the admin
port. For example::

    $ curl -X POST http://dw.example.com:8081/tasks/gc
    Running GC...
    Done!

.. _man-core-logging:

Logging
=======

Dropwizard uses Logback_ for its logging backend. It provides an slf4j_ implementation, and even
routes all ``java.util.logging``, Log4j, and Apache Commons Logging usage through Logback.

.. _Logback: http://logback.qos.ch/
.. _slf4j: http://www.slf4j.org/

slf4j provides the following logging levels:

``ERROR``
  Error events that might still allow the application to continue running.
``WARN``
  Potentially harmful situations.
``INFO``
  Informational messages that highlight the progress of the application at coarse-grained level.
``DEBUG``
  Fine-grained informational events that are most useful to debug an application.
``TRACE``
  Finer-grained informational events than the ``DEBUG`` level.

.. _man-core-logging-format:

Log Format
----------

Dropwizard's log format has a few specific goals:

* Be human readable.
* Be machine parsable.
* Be easy for sleepy ops folks to figure out why things are pear-shaped at 3:30AM using standard
  UNIXy tools like ``tail`` and ``grep``.

The logging output looks like this::

    TRACE [2010-04-06 06:42:35,271] com.example.dw.Thing: Contemplating doing a thing.
    DEBUG [2010-04-06 06:42:35,274] com.example.dw.Thing: About to do a thing.
    INFO  [2010-04-06 06:42:35,274] com.example.dw.Thing: Doing a thing
    WARN  [2010-04-06 06:42:35,275] com.example.dw.Thing: Doing a thing
    ERROR [2010-04-06 06:42:35,275] com.example.dw.Thing: This may get ugly.
    ! java.lang.RuntimeException: oh noes!
    ! at com.example.dw.Thing.run(Thing.java:16)
    !

A few items of note:

* All timestamps are in UTC and ISO 8601 format.
* You can grep for messages of a specific level really easily::

    tail -f dw.log | grep '^WARN'

* You can grep for messages from a specific class or package really easily::

    tail -f dw.log | grep 'com.example.dw.Thing'

* You can even pull out full exception stack traces, plus the accompanying log message::

    tail -f dw.log | grep -B 1 '^\!'

* The `!` prefix does *not* apply to syslog appenders, as stack traces are sent separately from the main message.
  Instead, `\t` is used (this is the default value of the `SyslogAppender` that comes with Logback). This can be
  configured with the `stackTracePrefix` option when defining your appender.

Configuration
-------------

You can specify a default logger level, override the levels of other loggers in your YAML configuration file,
and even specify appenders for them. The latter form of configuration is preferable, but the former is also
acceptable.

.. code-block:: yaml

    # Logging settings.
    logging:

      # The default level of all loggers. Can be OFF, ERROR, WARN, INFO, DEBUG, TRACE, or ALL.
      level: INFO

      # Logger-specific levels.
      loggers:

        # Overrides the level of com.example.dw.Thing and sets it to DEBUG.
        "com.example.dw.Thing": DEBUG

        # Enables the SQL query log and redirect it to a separate file
        "org.hibernate.SQL":
          level: DEBUG
          # This line stops org.hibernate.SQL (or anything under it) from using the root logger
          additive: false
          appenders:
            - type: file
              currentLogFilename: ./logs/example-sql.log
              archivedLogFilenamePattern: ./logs/example-sql-%d.log.gz
              archivedFileCount: 5
.. _man-core-logging-console:

Console Logging
---------------

By default, Dropwizard applications log ``INFO`` and higher to ``STDOUT``. You can configure this by
editing the ``logging`` section of your YAML configuration file:

.. code-block:: yaml

    logging:
      appenders:
        - type: console
          threshold: WARN
          target: stderr

In the above, we're instead logging only ``WARN`` and ``ERROR`` messages to the ``STDERR`` device.

.. _man-core-logging-file:

File Logging
------------

Dropwizard can also log to an automatically rotated set of log files. This is the recommended
configuration for your production environment:

.. code-block:: yaml

    logging:

      appenders:
        - type: file
          # The file to which current statements will be logged.
          currentLogFilename: ./logs/example.log

          # When the log file rotates, the archived log will be renamed to this and gzipped. The
          # %d is replaced with the previous day (yyyy-MM-dd). Custom rolling windows can be created
          # by passing a SimpleDateFormat-compatible format as an argument: "%d{yyyy-MM-dd-hh}".
          archivedLogFilenamePattern: ./logs/example-%d.log.gz

          # The number of archived files to keep.
          archivedFileCount: 5

          # The timezone used to format dates. HINT: USE THE DEFAULT, UTC.
          timeZone: UTC

.. _man-core-logging-syslog:

Syslog Logging
--------------

Finally, Dropwizard can also log statements to syslog.

.. note::

    Because Java doesn't use the native syslog bindings, your syslog server **must** have an open
    network socket.

.. code-block:: yaml

    logging:

      appenders:
        - type: syslog
          # The hostname of the syslog server to which statements will be sent.
          # N.B.: If this is the local host, the local syslog instance will need to be configured to
          # listen on an inet socket, not just a Unix socket.
          host: localhost

          # The syslog facility to which statements will be sent.
          facility: local0

You can combine any number of different ``appenders``, including multiple instances of the same
appender with different configurations:

.. code-block:: yaml

    logging:

      # Permit DEBUG, INFO, WARN and ERROR messages to be logged by appenders.
      level: DEBUG

      appenders:
        # Log warnings and errors to stderr
        - type: console
          threshold: WARN
          target: stderr

        # Log info, warnings and errors to our apps' main log.
        # Rolled over daily and retained for 5 days.
        - type: file
          threshold: INFO
          currentLogFilename: ./logs/example.log
          archivedLogFilenamePattern: ./logs/example-%d.log.gz
          archivedFileCount: 5

        # Log debug messages, info, warnings and errors to our apps' debug log.
        # Rolled over hourly and retained for 6 hours
        - type: file
          threshold: DEBUG
          currentLogFilename: ./logs/debug.log
          archivedLogFilenamePattern: ./logs/debug-%d{yyyy-MM-dd-hh}.log.gz
          archivedFileCount: 6

.. _man-core-logging-http-config:

Logging Configuration via HTTP
------------------------------

Active log levels can be changed during the runtime of a Dropwizard application via HTTP using
the ``LogConfigurationTask``. For instance, to configure the log level for a
single ``Logger``:

.. code-block:: shell

    curl -X POST -d "logger=com.example.helloworld&level=INFO" http://localhost:8081/tasks/log-level

.. _man-core-testing-applications:

Testing Applications
====================

All of Dropwizard's APIs are designed with testability in mind, so even your applications can have unit
tests:

.. code-block:: java

    public class MyApplicationTest {
        private final Environment environment = mock(Environment.class);
        private final JerseyEnvironment jersey = mock(JerseyEnvironment.class);
        private final MyApplication application = new MyApplication();
        private final MyConfiguration config = new MyConfiguration();

        @Before
        public void setup() throws Exception {
            config.setMyParam("yay");
            when(environment.jersey()).thenReturn(jersey);
        }

        @Test
        public void buildsAThingResource() throws Exception {
            application.run(config, environment);

            verify(jersey).register(isA(ThingResource.class));
        }
    }

We highly recommend Mockito_ for all your mocking needs.

.. _Mockito: http://code.google.com/p/mockito/


.. _man-core-banners:

Banners
=======

We think applications should print out a big ASCII art banner on startup. Yours should, too. It's fun.
Just add a ``banner.txt`` class to ``src/main/resources`` and it'll print it out when your application
starts::

    INFO  [2011-12-09 21:56:37,209] io.dropwizard.cli.ServerCommand: Starting hello-world
                                                     dP
                                                     88
      .d8888b. dP.  .dP .d8888b. 88d8b.d8b. 88d888b. 88 .d8888b.
      88ooood8  `8bd8'  88'  `88 88'`88'`88 88'  `88 88 88ooood8
      88.  ...  .d88b.  88.  .88 88  88  88 88.  .88 88 88.  ...
      `88888P' dP'  `dP `88888P8 dP  dP  dP 88Y888P' dP `88888P'
                                            88
                                            dP

    INFO  [2011-12-09 21:56:37,214] org.eclipse.jetty.server.Server: jetty-7.6.0
    ...

We could probably make up an argument about why this is a serious devops best practice with high ROI
and an Agile Tool, but honestly we just enjoy this.

We recommend you use TAAG_ for all your ASCII art banner needs.

.. _TAAG: http://patorjk.com/software/taag/

.. _man-core-resources:

Resources
=========

Unsurprisingly, most of your day-to-day work with a Dropwizard application will be in the resource
classes, which model the resources exposed in your RESTful API. Dropwizard uses Jersey__ for this,
so most of this section is just re-hashing or collecting various bits of Jersey documentation.

.. __: http://jersey.java.net/

Jersey is a framework for mapping various aspects of incoming HTTP requests to POJOs and then
mapping various aspects of POJOs to outgoing HTTP responses. Here's a basic resource class:

.. _man-core-resources-example:

.. code-block:: java

    @Path("/{user}/notifications")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public class NotificationsResource {
        private final NotificationStore store;

        public NotificationsResource(NotificationStore store) {
            this.store = store;
        }

        @GET
        public NotificationList fetch(@PathParam("user") LongParam userId,
                                      @QueryParam("count") @DefaultValue("20") IntParam count) {
            final List<Notification> notifications = store.fetch(userId.get(), count.get());
            if (notifications != null) {
                return new NotificationList(userId, notifications);
            }
            throw new WebApplicationException(Status.NOT_FOUND);
        }

        @POST
        public Response add(@PathParam("user") LongParam userId,
                            @Valid Notification notification) {
            final long id = store.add(userId.get(), notification);
            return Response.created(UriBuilder.fromResource(NotificationResource.class)
                                              .build(userId.get(), id))
                           .build();
        }
    }

This class provides a resource (a user's list of notifications) which responds to ``GET`` and
``POST`` requests to ``/{user}/notifications``, providing and consuming ``application/json``
representations. There's quite a lot of functionality on display here, and this section will
explain in detail what's in play and how to use these features in your application.

.. _man-core-resources-paths:

Paths
-----

.. important::

    Every resource class must have a ``@Path`` annotation.

The ``@Path`` annotation isn't just a static string, it's a `URI Template`__. The ``{user}`` part
denotes a named variable, and when the template matches a URI the value of that variable will be
accessible via ``@PathParam``-annotated method parameters.

.. __: http://tools.ietf.org/html/draft-gregorio-uritemplate-07

For example, an incoming request for ``/1001/notifications`` would match the URI template, and the
value ``"1001"`` would be available as the path parameter named ``user``.

If your application doesn't have a resource class whose ``@Path`` URI template matches the URI of an
incoming request, Jersey will automatically return a ``404 Not Found`` to the client.

.. _man-core-resources-methods:

Methods
-------

Methods on a resource class which accept incoming requests are annotated with the HTTP methods they
handle: ``@GET``, ``@POST``, ``@PUT``, ``@DELETE``, ``@HEAD``, ``@OPTIONS``, ``@PATCH``.

Support for arbitrary new methods can be added via the ``@HttpMethod`` annotation. They also must
be added to the :ref:`list of allowed methods <man-configuration-all>`. This means, by default,
methods such as ``CONNECT`` and ``TRACE`` are blocked, and will return a ``405 Method Not Allowed``
response.

If a request comes in which matches a resource class's path but has a method which the class doesn't
support, Jersey will automatically return a ``405 Method Not Allowed`` to the client.

The return value of the method (in this case, a ``NotificationList`` instance) is then mapped to the
:ref:`negotiated media type <man-core-resources-media-types>` this case, our resource only supports
JSON, and so the ``NotificationList`` is serialized to JSON using Jackson.

.. _man-core-resources-metrics:

Metrics
-------

Every resource method can be annotated with ``@Timed``, ``@Metered``, and ``@ExceptionMetered``.
Dropwizard augments Jersey to automatically record runtime information about your resource methods.

* ``@Timed`` measures the duration of requests to a resource
* ``@Metered`` measures the rate at which the resource is accessed
* ``@ExceptionMetered`` measures how often exceptions occur processing the resource

.. _man-core-resources-parameters:

Parameters
----------

The annotated methods on a resource class can accept parameters which are mapped to from aspects of
the incoming request. The ``*Param`` annotations determine which part of the request the data is
mapped, and the parameter *type* determines how the data is mapped.

For example:

* A ``@PathParam("user")``-annotated ``String`` takes the raw value from the ``user`` variable in
  the matched URI template and passes it into the method as a ``String``.
* A ``@QueryParam("count")``-annotated ``IntParam`` parameter takes the first ``count`` value from
  the request's query string and passes it as a ``String`` to ``IntParam``'s constructor.
  ``IntParam`` (and all other ``io.dropwizard.jersey.params.*`` classes) parses the string
  as an ``Integer``, returning a ``400 Bad Request`` if the value is malformed.
* A ``@FormParam("name")``-annotated ``Set<String>`` parameter takes all the ``name`` values from a
  posted form and passes them to the method as a set of strings.
* A ``*Param``--annotated ``NonEmptyStringParam`` will interpret empty strings as absent strings,
  which is useful in cases where the endpoint treats empty strings and absent strings as
  interchangeable.

What's noteworthy here is that you can actually encapsulate the vast majority of your validation
logic using specialized parameter objects. See ``AbstractParam`` for details.

.. _man-core-resources-request-entities:

Request Entities
----------------

If you're handling request entities (e.g., an ``application/json`` object on a ``PUT`` request), you
can model this as a parameter without a ``*Param`` annotation. In the
:ref:`example code <man-core-resources-example>`, the ``add`` method provides a good example of
this:

.. code-block:: java
    :emphasize-lines: 3

    @POST
    public Response add(@PathParam("user") LongParam userId,
                        @Valid Notification notification) {
        final long id = store.add(userId.get(), notification);
        return Response.created(UriBuilder.fromResource(NotificationResource.class)
                                          .build(userId.get(), id)
                       .build();
    }

Jersey maps the request entity to any single, unbound parameter. In this case, because the resource
is annotated with ``@Consumes(MediaType.APPLICATION_JSON)``, it uses the Dropwizard-provided Jackson
support which, in addition to parsing the JSON and mapping it to an instance of ``Notification``,
also runs that instance through Dropwizard's :ref:`man-validation-validations-constraining-entities`.

If the deserialized ``Notification`` isn't valid, Dropwizard returns a ``422 Unprocessable Entity``
response to the client.

.. note::

    If your request entity parameter isn't annotated with ``@Valid``, it won't be validated.

.. _man-core-resources-media-types:

Media Types
-----------

Jersey also provides full content negotiation, so if your resource class consumes
``application/json`` but the client sends a ``text/plain`` entity, Jersey will automatically reply
with a ``406 Not Acceptable``. Jersey's even smart enough to use client-provided ``q``-values in
their ``Accept`` headers to pick the best response content type based on what both the client and
server will support.

.. _man-core-resources-responses:

Responses
---------

If your clients are expecting custom headers or additional information (or, if you simply desire an
additional degree of control over your responses), you can return explicitly-built ``Response``
objects:

.. code-block:: java

    return Response.noContent().language(Locale.GERMAN).build();


In general, though, we recommend you return actual domain objects if at all possible. It makes
:ref:`testing resources <man-core-resources-testing>` much easier.

.. _man-core-resource-error-handling:

Error Handling
--------------

If your resource class unintentionally throws an exception, Dropwizard will log that exception
(including stack traces) and return a terse, safe ``text/plain`` ``500 Internal Server Error``
response.

If your resource class needs to return an error to the client (e.g., the requested record doesn't
exist), you have two options: throw a subclass of ``Exception`` or restructure your method to
return a ``Response``.

If at all possible, prefer throwing ``Exception`` instances to returning
``Response`` objects.

If you throw a subclass of ``WebApplicationException`` jersey will map that to a defined response.

If you want more control, you can also declare JerseyProviders in your Environment to map Exceptions
to certain responses by calling ``JerseyEnvironment#register(Object)`` with an implementation of
javax.ws.rs.ext.ExceptionMapper.
e.g. Your resource throws an InvalidArgumentException, but the response would be 400, bad request.


.. _man-core-resources-uris:

URIs
----

While Jersey doesn't quite have first-class support for hyperlink-driven applications, the provided
``UriBuilder`` functionality does quite well.

Rather than duplicate resource URIs, it's possible (and recommended!) to initialize a ``UriBuilder``
with the path from the resource class itself:

.. code-block:: java

    UriBuilder.fromResource(UserResource.class).build(user.getId());

.. _man-core-resources-testing:

Testing
-------

As with just about everything in Dropwizard, we recommend you design your resources to be testable.
Dependencies which aren't request-injected should be passed in via the constructor and assigned to
``final`` fields.

Testing, then, consists of creating an instance of your resource class and passing it a mock.
(Again: Mockito_.)

.. code-block:: java

    public class NotificationsResourceTest {
        private final NotificationStore store = mock(NotificationStore.class);
        private final NotificationsResource resource = new NotificationsResource(store);

        @Test
        public void getsReturnNotifications() {
            final List<Notification> notifications = mock(List.class);
            when(store.fetch(1, 20)).thenReturn(notifications);

            final NotificationList list = resource.fetch(new LongParam("1"), new IntParam("20"));

            assertThat(list.getUserId(),
                      is(1L));

            assertThat(list.getNotifications(),
                       is(notifications));
        }
    }

Caching
-------

Adding a ``Cache-Control`` statement to your resource class is simple with Dropwizard:

.. code-block:: java

    @GET
    @CacheControl(maxAge = 6, maxAgeUnit = TimeUnit.HOURS)
    public String getCachableValue() {
        return "yay";
    }

The ``@CacheControl`` annotation will take all of the parameters of the ``Cache-Control`` header.

.. _man-core-representations:

Representations
===============

Representation classes are classes which, when handled to various Jersey ``MessageBodyReader`` and
``MessageBodyWriter`` providers, become the entities in your application's API. Dropwizard heavily
favors JSON, but it's possible to map from any POJO to custom formats and back.

.. _man-core-representations-basic:

Basic JSON
----------

Jackson is awesome at converting regular POJOs to JSON and back. This file:

.. code-block:: java

    public class Notification {
        private String text;

        public Notification(String text) {
            this.text = text;
        }

        @JsonProperty
        public String getText() {
            return text;
        }

        @JsonProperty
        public void setText(String text) {
            this.text = text;
        }
    }

gets converted into this JSON:

.. code-block:: javascript

    {
        "text": "hey it's the value of the text field"
    }

If, at some point, you need to change the JSON field name or the Java field without affecting the
other, you can add an explicit field name to the ``@JsonProperty`` annotation.

If you prefer immutable objects rather than JavaBeans, that's also doable:

.. code-block:: java

    public class Notification {
        private final String text;

        @JsonCreator
        public Notification(@JsonProperty("text") String text) {
            this.text = text;
        }

        @JsonProperty("text")
        public String getText() {
            return text;
        }
    }

.. _man-core-representations-advanced:

Advanced JSON
-------------

Not all JSON representations map nicely to the objects your application deals with, so it's sometimes
necessary to use custom serializers and deserializers. Just annotate your object like this:

.. code-block:: java

    @JsonSerialize(using=FunkySerializer.class)
    @JsonDeserialize(using=FunkyDeserializer.class)
    public class Funky {
        // ...
    }

Then make a ``FunkySerializer`` class which implements ``JsonSerializer<Funky>`` and a
``FunkyDeserializer`` class which implements ``JsonDeserializer<Funky>``.

.. _man-core-representations-advanced-snake-case:

``snake_case``
**************

A common issue with JSON is the disagreement between ``camelCase`` and ``snake_case`` field names.
Java and Javascript folks tend to like ``camelCase``; Ruby, Python, and Perl folks insist on
``snake_case``. To make Dropwizard automatically convert field names to ``snake_case`` (and back),
just annotate the class with ``@JsonSnakeCase``:

.. code-block:: java

    @JsonSnakeCase
    public class Person {
        private final String firstName;

        @JsonCreator
        public Person(@JsonProperty String firstName) {
            this.firstName = firstName;
        }

        @JsonProperty
        public String getFirstName() {
            return firstName;
        }
    }

This gets converted into this JSON:

.. code-block:: javascript

    {
        "first_name": "Coda"
    }

.. _man-core-representations-streaming:

Streaming Output
----------------

If your application happens to return lots of information, you may get a big performance and efficiency
bump by using streaming output. By returning an object which implements Jersey's ``StreamingOutput``
interface, your method can stream the response entity in a chunk-encoded output stream. Otherwise,
you'll need to fully construct your return value and *then* hand it off to be sent to the client.


.. _man-core-representations-html:

HTML Representations
--------------------

For generating HTML pages, check out Dropwizard's :ref:`views support <manual-views>`.

.. _man-core-representations-custom:

Custom Representations
----------------------

Sometimes, though, you've got some wacky output format you need to produce or consume and no amount
of arguing will make JSON acceptable. That's unfortunate but OK. You can add support for arbitrary
input and output formats by creating classes which implement Jersey's ``MessageBodyReader<T>`` and
``MessageBodyWriter<T>`` interfaces. (Make sure they're annotated with ``@Provider`` and
``@Produces("text/gibberish")`` or ``@Consumes("text/gibberish")``.) Once you're done, just add
instances of them (or their classes if they depend on Jersey's ``@Context`` injection) to your
application's ``Environment`` on initialization.

.. _man-core-jersey-filters:

Jersey filters
--------------

There might be cases when you want to filter out requests or modify them before they reach your Resources. Jersey
has a rich api for `filters and interceptors`_ that can be used directly in Dropwizard.
You can stop the request from reaching your resources by throwing a ``WebApplicationException``. Alternatively,
you can use filters to modify inbound requests or outbound responses.

.. _filters and interceptors: http://jersey.java.net/documentation/latest/filters-and-interceptors.html

.. code-block:: java

    @Provider
    public class DateNotSpecifiedFilter implements ContainerRequestFilter {
        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            String dateHeader = requestContext.getHeaderString(HttpHeaders.DATE);

            if (dateHeader == null) {
                Exception cause = new IllegalArgumentException("Date Header was not specified");
                throw new WebApplicationException(cause, Response.Status.BAD_REQUEST);
            }
        }
    }

This example filter checks the request for the "Date" header, and denies the request if was missing. Otherwise,
the request is passed through.

Filters can be dynamically bound to resource methods using `DynamicFeature`_:

.. _DynamicFeature: http://jax-rs-spec.java.net/nonav/2.0-rev-a/apidocs/index.html

.. code-block:: java

    @Provider
    public class DateRequiredFeature implements DynamicFeature {
        @Override
        public void configure(ResourceInfo resourceInfo, FeatureContext context) {
            if (resourceInfo.getResourceMethod().getAnnotation(DateRequired.class) != null) {
                context.register(DateNotSpecifiedFilter.class);
            }
        }
    }

The DynamicFeature is invoked by the Jersey runtime when the application is started. In this example, the feature checks
for methods that are annotated with ``@DateRequired`` and registers the ``DateNotSpecified`` filter on those methods only.

You typically register the feature in your Application class, like so:

.. code-block:: java

    environment.jersey().register(DateRequiredFeature.class);


.. _man-core-servlet-filters:

Servlet filters
---------------

Another way to create filters is by creating servlet filters. They offer a way to to register filters that apply both to servlet requests as well as resource requests.
Jetty comes with a few `bundled`_  filters which may already suit your needs. If you want to create your own filter,
this example demonstrates a servlet filter analogous to the previous example:

.. _bundled: http://www.eclipse.org/jetty/documentation/current/advanced-extras.html

.. code-block:: java

    public class DateNotSpecifiedServletFilter implements javax.servlet.Filter {
        // Other methods in interface omitted for brevity

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            if (request instanceof HttpServletRequest) {
                String dateHeader = ((HttpServletRequest) request).getHeader(HttpHeaders.DATE);

                if (dateHeader != null) {
                    chain.doFilter(request, response); // This signals that the request should pass this filter
                } else {
                    HttpServletResponse httpResponse = (HttpServletResponse) response;
                    httpResponse.setStatus(HttpStatus.BAD_REQUEST_400);
                    httpResponse.getWriter().print("Date Header was not specified");
                }
            }
        }
    }


This servlet filter can then be registered in your Application class by wrapping it in ``FilterHolder`` and adding it to the application context together with a
specification for which paths this filter should active. Here's an example:

.. code-block:: java

        environment.servlets().addFilter("DateNotSpecifiedServletFilter", new DateNotSpecifiedServletFilter())
                              .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");
.. _man-glue-detail:

How it's glued together
=======================

When your application starts up, it will spin up a Jetty HTTP server, see ``DefaultServerFactory``.
This server will have two handlers, one for your application port and the other for your admin port.
The admin handler creates and registers the ``AdminServlet``. This has a handle to all of the
application healthchecks and metrics via the ServletContext.

The application port has an HttpServlet as well, this is composed of ``DropwizardResourceConfig``,
which is an extension of Jersey's resource configuration that performs scanning to
find root resource and provider classes. Ultimately when you call
``env.jersey().register(new SomeResource())``,
you are adding to the ``DropwizardResourceConfig``. This config is a jersey ``Application``, so all of
your application resources are served from one ``Servlet``

``DropwizardResourceConfig`` is where the various ResourceMethodDispatchAdapter are registered to
enable the following functionality:

    * Resource method requests with ``@Timed``, ``@Metered``, ``@ExceptionMetered`` are delegated to special dispatchers which decorate the metric telemetry
    * Resources that return Guava Optional are unboxed. Present returns underlying type, and non-present 404s
    * Resource methods that are annotated with ``@CacheControl`` are delegated to a special dispatcher that decorates on the cache control headers
    * Enables using Jackson to parse request entities into objects and generate response entities from objects, all while performing validation
