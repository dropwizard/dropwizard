.. _man-core:

###############
Dropwizard Core
###############

.. highlight:: text

.. rubric:: The ``dropwizard-core`` module provides you with everything you'll need for most of your
            services.

It includes:

* Jetty, a high-performance HTTP server.
* Jersey, a full-featured RESTful web framework.
* Jackson, the best JSON library for the JVM.
* Metrics, `Yammer's`__ own library for application metrics.
* Guava, Google's excellent utility library.
* Logback, the successor to Log4j, Java's most widely-used logging framework.
* Hibernate Validator, the reference implementation of the Java Bean Validation standard.

.. __: https://www.yammer.com

Dropwizard consists mostly of glue code to automatically connect and configure these components.

.. _man-core-organization:

Organizing Your Project
=======================

In general, we recommend you separate your projects into three Maven modules: ``project-api``,
``project-client``, and ``project-service``.

``project-api`` should contain your :ref:`man-core-representations`; ``project-client`` should use
those classes and an :ref:`HTTP client <man-client>` to implement a full-fledged client for your
service, and ``project-service`` should provide the actual service implementation, including
:ref:`man-core-resources`.

Our services tend to look like this:

* ``com.example.myservice``:

  * ``api``: :ref:`man-core-representations`.
  * ``cli``: :ref:`man-core-commands`
  * ``client``: :ref:`Client <man-client>` implementation for your service
  * ``core``: Domain implementation
  * ``jdbi``: :ref:`Database <man-jdbi>` access classes
  * ``health``: :ref:`man-core-healthchecks`
  * ``resources``: :ref:`man-core-resources`
  * ``MyService``: The :ref:`service <man-core-service>` class
  * ``MyServiceConfiguration``: :ref:`configuration <man-core-configuration>` class

.. _man-core-service:

Service
=======

The main entry point into a Dropwizard service is, unsurprisingly, the ``Service`` class. Each
``Service`` has a **name**, which is mostly used to render the command-line interface. In the
constructor of your ``Service`` you can add :ref:`man-core-bundles` and :ref:`man-core-commands` to
your service.

.. _man-core-configuration:

Configuration
=============

Dropwizard provides a number of built-in configuration parameters. They are
well documented in the `example project's configuration`__.

.. __: https://github.com/codahale/dropwizard/blob/master/dropwizard-example/example.yml

Each ``Service`` subclass has a single type parameter: that of its matching ``Configuration``
subclass. These are usually at the root of your service's main package. For example, your User
service would have two classes: ``UserServiceConfiguration``, extending ``Configuration``, and
``UserService``, extending ``Service<UserServiceConfiguration>``.

When your service runs :ref:`man-core-commands-configured` like the ``server`` command, Dropwizard
parses the provided YAML configuration file and builds an instance of your service's configuration
class by mapping YAML field names to object field names.

.. note::

    If your configuration file doesn't end in ``.yml`` or ``.yaml``, Dropwizard tries to parse it
    as a JSON file.

In order to keep your configuration file and class manageable, we recommend grouping related
configuration parameters into independent configuration classes. If your service requires a set of
configuration parameters in order to connect to a message queue, for example, we recommend that you
create a new ``MessageQueueConfiguration`` class:

.. code-block:: java

    public class MessageQueueConfiguration {
        @NotEmpty
        @JsonProperty
        private String host;

        @Min(1)
        @Max(65535)
        @JsonProperty
        private int port = 5672;

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }
    }

Your main ``Configuration`` subclass can then include this as a member field:

.. code-block:: java

    public class ExampleServiceConfiguration extends Configuration {
        @Valid
        @NotNull
        @JsonProperty
        private MessageQueueConfiguration messageQueue = new MessageQueueConfiguration();

        public MessageQueueConfiguration getMessageQueueConfiguration() {
            return messageQueue;
        }
    }

Then, in your service's YAML file, you can use a nested ``messageQueue`` field:

.. code-block:: java

    messageQueue:
      host: mq.example.com
      port: 5673

The ``@NotNull``, ``@NotEmpty``, ``@Min``, ``@Max``, and ``@Valid`` annotations are part of Dropwizard's
:ref:`man-core-representations-validation` functionality. If your YAML configuration file's
``messageQueue.host`` field was missing (or was a blank string), Dropwizard would refuse to start
and would output an error message describing the issues.

Once your service has parsed the YAML file and constructed its ``Configuration`` instance,
Dropwizard then calls your ``Service`` subclass to initialize your service's ``Environment``.

.. note::

    You can override configuration settings by passing special Java system properties when starting
    your service. Overrides must start with prefix ``dw.``, followed by the path to the
    configuration value being overridden.

    For example, to override the HTTP port to use, you could start your service like this:

    ``java -Ddw.http.port=9090 server my-config.json``

.. _man-core-environments:

SSL
---

SSL support is built into Dropwizard. You will need to provide your own java
key store, which is outside the scope of this document (``keytool`` is the
command you need). There is a test keystore you can use in the
`Dropwizard example project`__.

.. __: https://github.com/codahale/dropwizard/tree/master/dropwizard-example

.. code-block:: yaml

    http:
      ssl:
        keyStorePath: ./example.keystore
        keyStorePassword: example

        # optional, JKS is default. JCEKS is another likely candidate.
        keyStoreType: JKS

Bootstrapping
=============

Before a Dropwizard service can provide the command-line interface, parse a configuration file, or
run as a server, it must first go through a bootstrapping phase. This phase corresponds to your
``Service`` subclass's ``initialize`` method. You can add :ref:`man-core-bundles`,
:ref:`man-core-commands`, or register Jackson modules to allow you to include custom types as part
of your configuration class.

Environments
============

A Dropwizard ``Environment`` consists of all the :ref:`man-core-resources`, servlets, filters,
:ref:`man-core-healthchecks`, Jersey providers, :ref:`man-core-managed`, :ref:`man-core-tasks`, and
Jersey properties which your service provides.

Each ``Service`` subclass implements a ``run`` method. This is where you should be creating new
resource instances, etc., and adding them to the given ``Environment`` class:

.. code-block:: java

    @Override
    public void run(ExampleConfiguration config,
                    Environment environment) {
        // encapsulate complicated setup logic in factories
        final ThingyFactory thingyFactory = new ThingyFactory(config.getThingyConfiguration());

        final Thingy thingy = thingyFactory.build();

        environment.addResource(new ThingyResource(thingy));
        environment.addHealthCheck(new ThingyHealthCheck(thingy));
    }

It's important to keep the ``run`` method clean, so if creating an instance of something is
complicated, like the ``Thingy`` class above, extract that logic into a factory.

.. _man-core-healthchecks:

Health Checks
=============

A health check is a runtime test which you can use to verify your service's behavior in its
production environment. For example, you may want to ensure that your database client is connected
to the database:

.. code-block:: java

    public class DatabaseHealthCheck extends HealthCheck {
        private final Database database;

        public DatabaseHealthCheck(Database database) {
            super("database");
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

You can then add this health check to your service's environment:

.. code-block:: java

    environment.addHealthCheck(new DatabaseHealthCheck(database));

By sending a ``GET`` request to ``/healthcheck`` on the admin port you can run these tests and view
the results::

    $ curl http://dw.example.com:8081/healthcheck
    * deadlocks: OK
    * database: OK

If all health checks report success, a ``200 OK`` is returned. If any fail, a
``500 Internal Server Error`` is returned with the error messages and exception stack traces (if an
exception was thrown).

All Dropwizard services ship with the ``deadlocks`` health check installed by default, which uses
Java 1.6's built-in thread deadlock detection to determine if any threads are deadlocked.

.. _man-core-managed:

Managed Objects
===============

Most services involve objects which need to be started and stopped: thread pools, database
connections, etc. Dropwizard provides the ``Managed`` interface for this. You can either have the
class in question implement the ``#start()`` and ``#stop()`` methods, or write a wrapper class which
does so. Adding a ``Managed`` instance to your service's ``Environment`` ties that object's
lifecycle to that of the service's HTTP server. Before the server starts, the ``#start()`` method is
called. After the server has stopped (and after its graceful shutdown period) the ``#stop()`` method
is called.

For example, given a theoretical Riak__ client which needs to be started and stopped:

.. __: http://riak.basho.com

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


If ``RiakClientManager#start()`` throws an exception--e.g., an error connecting to the server--your
service will not start and a full exception will be logged. If ``RiakClientManager#stop()`` throws
an exception, the exception will be logged but your service will still be able to shut down.

It should be noted that ``Environment`` has built-in factory methods for ``ExecutorService`` and
``ScheduledExecutorService`` instances which are managed. See ``Environment#managedExecutorService``
and ``Environment#managedScheduledExecutorService`` for details.

.. _man-core-bundles:

Bundles
=======

A Dropwizard bundle is a reusable group of functionality, used to define blocks of a service's
behavior. For example, ``AssetBundle`` provides a simple way to serve static assets from your
service's ``src/main/resources/assets`` directory as files available from ``/assets/*`` in your
service.

Some bundles require configuration parameters. These bundles implement ``ConfiguredBundle`` and will
require your service's ``Configuration`` subclass to implement a specific interface.

Serving Assets
--------------

Either your service or your static assets can be served from the root path, but
not both. The latter is useful when using Dropwizard to back a Javascript
application. To enable it, move your service to a sub-URL.

.. code-block:: yaml

    http:
      rootPath: /service/*  # Default is /*

Then use an extended ``AssetsBundle`` constructor to serve resources in the
``assets`` folder from the root path. ``index.htm`` is served as the default
page.

.. code-block:: java

    private HelloWorldService() {
        super("hello-world");

        // By default a restart will be required to pick up any changes to assets.
        // Use the following spec to disable that behaviour, useful when developing.
        //CacheBuilderSpec cacheSpec = CacheBuilderSpec.disableCaching();

        CacheBuilderSpec cacheSpec = AssetsBundle.DEFAULT_CACHE_SPEC;
        addBundle(new AssetsBundle("/assets/", cacheSpec, "/"));
    }

.. _man-core-commands:

Commands
========

Commands are basic actions which Dropwizard runs based on the arguments provided on the command
line. The built-in ``server`` command, for example, spins up an HTTP server and runs your service.
Each ``Command`` subclass has a name and a set of command line options which Dropwizard will use to
parse the given command line arguments.

.. _man-core-commands-configured:

Configured Commands
-------------------

Some commands require access to configuration parameters and should extend the ``ConfiguredCommand``
class, using your service's ``Configuration`` class as its type parameter. Dropwizard will treat the
first argument on the command line as the path to a YAML configuration file, parse and validate it,
and provide your command with an instance of the configuration class.

.. _man-core-commands-managed:

Managed Commands
----------------

Managed commands further extend configured commands by creating a lifecycle process for your
service's :ref:`man-core-managed`. All ``Managed`` instances registered with your service's
``Environment`` will be started before your command is run, and will be stopped afterward.

.. _man-core-tasks:

Tasks
=====

A ``Task`` is a run-time action your service provides access to on the administrative port via HTTP.
All Dropwizard services start with the ``gc`` task, which explicitly triggers the JVM's garbage
collection. (This is useful, for example, for running full garbage collections during off-peak times
or while the given service is out of rotation.)

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

Configuration
-------------

You can specify a default logger level and even override the levels of
other loggers in your YAML configuration file:

.. code-block:: yaml

    # Logging settings.
    logging:

      # The default level of all loggers. Can be OFF, ERROR, WARN, INFO, DEBUG, TRACE, or ALL.
      level: INFO

      # Logger-specific levels.
      loggers:

        # Overrides the level of com.example.dw.Thing and sets it to DEBUG.
        "com.example.dw.Thing": DEBUG

.. _man-core-logging-console:

Console Logging
---------------

By default, Dropwizard services log ``INFO`` and higher to ``STDOUT``. You can configure this by
editing the ``logging`` section of your YAML configuration file:

.. code-block:: yaml

    logging:

      # ...
      # Settings for logging to stdout.
      console:

        # If true, write log statements to stdout.
        enabled: true

        # Do not display log statements below this threshold to stdout.
        threshold: ALL

.. _man-core-logging-file:

File Logging
------------

Dropwizard can also log to an automatically rotated set of log files. This is the recommended
configuration for your production environment:

.. code-block:: yaml

    logging:

      # ...
      # Settings for logging to a file.
      file:

        # If true, write log statements to a file.
        enabled: false

        # Do not write log statements below this threshold to the file.
        threshold: ALL

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

      # ...
      # Settings for logging to syslog.
      syslog:

        # If true, write log statements to syslog.
        enabled: false

        # Do not write log statements below this threshold to syslog.
        threshold: ALL

        # The hostname of the syslog server to which statements will be sent.
        # N.B.: If this is the local host, the local syslog instance will need to be configured to
        # listen on an inet socket, not just a Unix socket.
        host: localhost

        # The syslog facility to which statements will be sent.
        facility: local0

.. _man-core-testing-services:

Testing Services
================

All of Dropwizard's APIs are designed with testability in mind, so even your services can have unit
tests:

.. code-block:: java

    public class MyServiceTest {
        private final Environment environment = mock(Environment.class);
        private final MyService service = new MyService();
        private final MyConfiguration config = new MyConfiguration();

        @Before
        public void setup() throws Exception {
            config.setMyParam("yay");
        }

        @Test
        public void buildsAThingResource() throws Exception {
            service.run(config, environment);

            verify(environment).addResource(any(ThingResource.class));
        }
    }

We highly recommend Mockito_ for all your mocking needs.

.. _Mockito: http://code.google.com/p/mockito/


.. _man-core-banners:

Banners
=======

At Yammer, each of our services prints out a big ASCII art banner on startup. Yours should, too.
It's fun. Just add a ``banner.txt`` class to ``src/main/resources`` and it'll print it out when your
service starts::

    INFO  [2011-12-09 21:56:37,209] com.yammer.dropwizard.cli.ServerCommand: Starting hello-world
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

Unsurprisingly, most of your day-to-day work with a Dropwizard service will be in the resource
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
                                              .build(userId.get(), id)
                           .build();
        }
    }

This class provides a resource (a user's list of notifications) which responds to ``GET`` and
``POST`` requests to ``/{user}/notifications``, providing and consuming ``application/json``
representations. There's quite a lot of functionality on display here, and this section will
explain in detail what's in play and how to use these features in your service.

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

If your service doesn't have a resource class whose ``@Path`` URI template matches the URI of an
incoming request, Jersey will automatically return a ``404 Not Found`` to the client.

.. _man-core-resources-methods:

Methods
-------

Methods on a resource class which accept incoming requests are annotated with the HTTP methods they
handle: ``@GET``, ``@POST``, ``@PUT``, ``@DELETE``, ``@HEAD``, ``@OPTIONS``, and even
``@HttpMethod`` for arbitrary new methods.

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
  ``IntParam`` (and all other ``com.yammer.dropwizard.jersey.params.*`` classes) parses the string
  as an ``Integer``, returning a ``400 Bad Request`` if the value is malformed.
* A ``@FormParam("name")``-annotated ``Set<String>`` parameter takes all the ``name`` values from a
  posted form and passes them to the method as a set of strings.

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
also runs that instance through Dropwizard's :ref:`man-core-representations-validation`.

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
exist), you have two options: throw a ``WebApplicationException`` or restructure your method to
return a ``Response``.

If at all possible, prefer throwing ``WebApplicationException`` instances to returning
``Response`` objects.

.. _man-core-resources-uris:

URIs
----

While Jersey doesn't quite have first-class support for hyperlink-driven services, the provided
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
``MessageBodyWriter`` providers, become the entities in your service's API. Dropwizard heavily
favors JSON, but it's possible to map from any POJO to custom formats and back.

.. _man-core-representations-basic:

Basic JSON
----------

Jackson is awesome at converting regular POJOs to JSON and back. This file:

.. code-block:: java

    public class Notification {
        @JsonProperty
        private String text;

        public Notification(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public String setText(String text) {
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
        @JsonProperty
        private final String text;

        public Notification(@JsonProperty("text") String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

.. _man-core-representations-advanced:

Advanced JSON
-------------

Not all JSON representations map nicely to the objects your service deals with, so it's sometimes
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
        @JsonProperty
        private String firstName;

        public Person(String firstName) {
            this.firstName = firstName;
        }

        public String getFirstName() {
            return firstName;
        }
    }

This gets converted into this JSON:

.. code-block:: javascript

    {
        "first_name": "Coda"
    }

.. _man-core-representations-validation:

Validation
----------

Like :ref:`man-core-configuration`, you can add validation annotations to fields of your
representation classes and validate them. If we're accepting client-provided ``Person`` objects, we
probably want to ensure that the ``name`` field of the object isn't ``null`` or blank. We can do
this as follows:

.. code-block:: java

    public class Person {
        @NotEmpty // ensure that name isn't null or blank
        @JsonProperty
        private final String name;

        public Person(@JsonProperty("name") String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

Then, in our resource class, we can add the ``@Valid`` annotation to the ``Person`` annotation:

.. code-block:: java

    @PUT
    public Response replace(@Valid Person person) {
        // ...
    }

If the ``name`` field is missing, Dropwizard will return a ``text/plain``
``422 Unprocessable Entity`` response detailing the validation errors::

    * name may not be empty

.. _man-core-resources-validation-advanced:

Advanced
********

More complex validations (for example, cross-field comparisons) are often hard to do using
declarative annotations. As an emergency maneuver, add the ``@ValidationMethod`` to any
``boolean``-returning method which begins with ``is``:

.. code-block:: java

    @ValidationMethod(message="may not be Coda")
    public boolean isNotCoda() {
        return !("Coda".equals(name));
    }

.. note::

    Due to the rather daft JavaBeans conventions, the method must begin with ``is`` (e.g.,
    ``#isValidPortRange()``. This is a limitation of Hibernate Validator, not Dropwizard.

.. _man-core-representations-streaming:

Streaming Output
----------------

If your service happens to return lots of information, you may get a big performance and efficiency
bump by using streaming output. By returning an object which implements Jersey's ``StreamingOutput``
interface, your method can stream the response entity in a chunk-encoded output stream. Otherwise,
you'll need to fully construct your return value and *then* hand it off to be sent to the client.

.. _man-core-representations-testing:

Testing
-------

The ``dropwizard-testing`` module contains a number of helper methods for testing JSON parsing and
generating. Given a JSON fixture file (e.g., ``src/test/resources/fixtures/person.json``), you can
test that a ``Person`` instance generates the same JSON as the fixture with the following:

.. code-block:: java

    import static com.yammer.dropwizard.testing.JsonHelpers.asJson;
    import static com.yammer.dropwizard.testing.JsonHelpers.jsonFixture;

    @Test
    public void producesTheExpectedJson() throws Exception {
        assertThat("rendering a person as JSON produces a valid API representation",
                   asJson(person),
                   is(jsonFixture("fixtures/person.json")));
    }

This does a whitespace- and comment-insensitive comparison of the generated JSON and the JSON in the
file. If they're different, both JSON representations are helpfully displayed in the assertion
error.

Likewise, you can also test the parsing of the same JSON file to guarantee round-trip compatibility:

.. code-block:: java

    import static com.yammer.dropwizard.testing.JsonHelpers.fromJson;

    @Test
    public void consumesTheExpectedJson() throws Exception {
        assertThat("parsing a valid API representation produces a person",
                   fromJson(jsonFixture("fixtures/person.json"), Person.class),
                   is(person));
    }

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
service's ``Environment`` on initialization.
