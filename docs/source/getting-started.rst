.. _getting-started:

###############
Getting Started
###############

.. highlight:: text

.. rubric:: *Getting Started* will guide you through the process of creating a simple Dropwizard
            project: Hello World. Along the way, we'll explain the various underlying libraries and
            their roles, important concepts in Dropwizard, and suggest some organizational
            techniques to help you as your project grows. (Or you can just skip to
            the :ref:`fun part <gs-maven-setup>`.)

.. _gs-overview:

Overview
========

Dropwizard straddles the line between being a library and a framework. Its goal is to provide
performant, reliable implementations of everything a production-ready web service needs. Because
this functionality is extracted into a reusable library, your service remains lean and focused,
reducing both time-to-market and maintenance burdens.

.. _gs-jetty:

Jetty for HTTP
--------------

Because you can't be a web service without HTTP, Dropwizard uses the Jetty_ HTTP library to embed
an incredibly tuned HTTP server directly into your project. Instead of handing your service off to a
complicated application server, Dropwizard projects have a ``main`` method which spins up an HTTP
server. Running your service as a simple process eliminates a number of unsavory aspects of Java in
production (no PermGem issues, no application server configuration and maintenance, no arcane
deployment tools, no ``ClassLoader`` troubles, no hidden application logs, no trying to tune a
single garbage collector to work with multiple application workloads) and allows you to use all of
the existing Unix process management tools instead.

.. _Jetty: http://www.eclipse.org/jetty/

.. _gs-jersey:

Jersey for REST
---------------

For building RESTful web services, we've found nothing beats Jersey_ (the `JAX-RS`_ reference
implementation) in terms of features or performance. It allows you to write clean, testable classes
which gracefully map HTTP requests to simple Java objects. It supports streaming output, matrix URI
parameters, conditional ``GET`` requests, and much, much more.

.. _Jersey: http://jersey.java.net
.. _JAX-RS: http://jcp.org/en/jsr/detail?id=311

.. _gs-jackson:

Jackson for JSON
----------------

In terms of data formats, JSON has become the web’s *lingua franca*, and Jackson_ is the king of
JSON on the JVM. In addition to being lightning fast, it has a sophisticated object mapper, allowing
you to export your domain models directly.

.. _Jackson: http://jackson.codehaus.org/

.. _gs-metrics:

Metrics for metrics
-------------------

Our very own Metrics_ library rounds things out, providing you with unparalleled insight into your
code's behavior in your production environment.

.. _Metrics: http://metrics.codahale.com

.. _gs-and-friends:

And Friends
-----------

In addition to Jetty_, Jersey_, and Jackson_, Dropwizard also includes a number of libraries that
we've come to rely on:

* Guava_, which, in addition to highly optimized immutable data structures, provides a growing
  number of classes to speed up development in Java.
* Logback_ and slf4j_ for performant logging.
* `Hibernate Validator`_, the `JSR-303`_ reference implementation, provides an easy, declarative
  framework for validating user input and generating helpful, internationalizable error messages.
* The `Apache HttpClient`_ and Jersey_ client libraries allow for both low- and high-level
  interaction with other web services.
* JDBI_ is the most straight-forward way to use a relational database with Java.
* Liquibase_ is a great way to keep your database schema in check throughout your development and
  release cycles, applying high-level database refactorings instead of one-off DDL scripts.
* Freemarker_ is a simple template system for more user-facing services.

.. _Guava: http://code.google.com/p/guava-libraries/
.. _Logback: http://logback.qos.ch/
.. _slf4j: http://www.slf4j.org/
.. _Hibernate Validator: http://www.hibernate.org/subprojects/validator.html
.. _JSR-303: http://jcp.org/en/jsr/detail?id=303
.. _Apache HttpClient: http://hc.apache.org/httpcomponents-client-ga/index.html
.. _JDBI: http://www.jdbi.org
.. _Freemarker: http://freemarker.sourceforge.net/
.. _Liquibase: http://www.liquibase.org

Now that you've gotten the lay of the land, let's dig in!

.. _gs-maven-setup:

Setting Up Maven
================

We recommend you use Maven_ for new Dropwizard services. If you're a big Ant_ / Ivy_, Buildr_,
Gradle_, SBT_, Leiningen_, or Gant_ fan, that’s cool, but we use Maven and we'll be using Maven as
we go through this example service. If you have any questions about how Maven works,
`Maven: The Complete Reference`__ should have what you’re looking for. (We’re assuming
you know how to create a new Maven project. If not, you can use
`this <https://gist.github.com/2019732>`_ to get started.)

.. _Maven: http://maven.apache.org
.. _Ant: http://ant.apache.org/
.. _Ivy: http://ant.apache.org/ivy/
.. _Buildr: http://buildr.apache.org/
.. _Gradle: http://www.gradle.org/
.. _SBT: https://github.com/harrah/xsbt/wiki
.. _Gant: http://gant.codehaus.org/
.. _Leiningen: https://github.com/technomancy/leiningen
.. __: http://www.sonatype.com/books/mvnref-book/reference/

Add the ``dropwizard-core`` library as a dependency:

.. _gs-pom-dependencies:

.. code-block:: xml

    <dependencies>
        <dependency>
            <groupId>com.yammer.dropwizard</groupId>
            <artifactId>dropwizard-core</artifactId>
            <version>0.6.0</version>
        </dependency>
    </dependencies>

Alright, that's enough XML. We’ve got a Maven project set up now, and it's time to start writing
real code.

.. _gs-configuration:

Creating A Configuration Class
==============================

Each Dropwizard service has its own subclass of the ``Configuration`` class which specify
environment-specific parameters. These parameters are specified in a YAML_ configuration file which
is deserialized to an instance of your service's configuration class and validated.

.. _YAML: http://www.yaml.org/

The service we're building is a high-performance Hello World service, and part of our requirements
is that we need to be able to vary how it says hello from environment to environment. We'll need to
specify at least two things to begin with: a template for saying hello and a default name to use in
case the user doesn't specify their name.

Here's what our configuration class will look like:

.. _gs-configuration-class:

.. code-block:: java

    package com.example.helloworld;
    
    import com.yammer.dropwizard.config.Configuration;
    import com.fasterxml.jackson.annotation.JsonProperty;
    import org.hibernate.validator.constraints.NotEmpty;
    
    public class HelloWorldConfiguration extends Configuration {
        @NotEmpty
        @JsonProperty
        private String template;
    
        @NotEmpty
        @JsonProperty
        private String defaultName = "Stranger";
    
        public String getTemplate() {
            return template;
        }
    
        public String getDefaultName() {
            return defaultName;
        }
    }

There's a lot going on here, so let’s unpack a bit of it.

When this class is deserialized from the YAML file, it will pull two root-level fields from the YAML
object: ``template``, the template for our Hello World saying, and ``defaultName``, the default name
to use. Both ``template`` and ``defaultName`` are annotated with ``@NotEmpty``, so if the YAML
configuration file has blank values for either or is missing ``template`` entirely an informative
exception will be thrown and your service won't start.

.. note::

    The mapping from YAML to your service's ``Configuration`` instance is done by Jackson_. This
    means your ``Configuration`` class can use all of Jackson's
    `object-mapping annotations`__. The validation of ``@NotEmpty`` is handled
    by Hibernate Validator, which has a `wide range of built-in constraints`__
    for you to use.

.. __: http://wiki.fasterxml.com/JacksonAnnotations
.. __: http://docs.jboss.org/hibernate/validator/4.2/reference/en-US/html_single/#validator-defineconstraints-builtin

Our YAML file, then, will look like this:

.. _gs-yaml-file:

.. code-block:: yaml

    template: Hello, %s!
    defaultName: Stranger

Dropwizard has *many* more configuration parameters than that, but they all have sane defaults so
you can keep your configuration files small and focused.

So save that YAML file as ``hello-world.yml``, because we'll be getting up and running pretty soon
and we'll need it. Next up, we're creating our service class!

.. _gs-service:

Creating A Service Class
========================

Combined with your project's ``Configuration`` subclass, its ``Service`` form the core of your
Dropwizard service. The ``Service`` class pulls together the various bundles and commands which
provide basic functionality. (More on that later.) For now, though, our ``HelloWorldService`` looks
like this:

.. code-block:: java

    package com.example.helloworld;
    
    import com.yammer.dropwizard.Service;
    import com.yammer.dropwizard.config.Bootstrap;
    import com.yammer.dropwizard.config.Environment;
    
    public class HelloWorldService extends Service<HelloWorldConfiguration> {
        public static void main(String[] args) throws Exception {
            new HelloWorldService().run(args);
        }

        @Override
        public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
            bootstrap.setName("hello-world");
        }

        @Override
        public void run(HelloWorldConfiguration configuration,
                        Environment environment) {
            // nothing to do yet
        }
    
    }

As you can see, ``HelloWorldService`` is parameterized with the service's configuration type,
``HelloWorldConfiguration``. An ``initialize`` method is used to configure aspects of the service
required before the service is run — in this case, our service's name: ``hello-world``. Also, we've
added a ``static`` ``main`` method, which will be our service's entry point. Right now, we don't
have any functionality implemented, so our ``run`` method is a little boring. Let’s fix that!

.. _gs-representation:

Creating A Representation Class
===============================

Before we can get into the nuts-and-bolts of our Hello World service, we need to stop and think
about our API. Luckily, our service needs to conform to an industry standard, `RFC 1149`__, which
specifies the following JSON representation of a Hello World saying:

.. __: http://www.ietf.org/rfc/rfc1149.txt

.. code-block:: javascript
    
    {
      "id": 1,
      "content": "Hello, stranger!"
    }


The ``id`` field is a unique identifier for the saying, and ``content`` is the textual
representation of the saying. (Thankfully, this is a fairly straight-forward industry standard.)

To model this representation, we'll create a representation class:

.. code-block:: java

    package com.example.helloworld.core;
    
    public class Saying {
        private final long id;
        private final String content;
    
        public Saying(long id, String content) {
            this.id = id;
            this.content = content;
        }
    
        public long getId() {
            return id;
        }
    
        public String getContent() {
            return content;
        }
    }

This is a pretty simple POJO, but there are a few things worth noting here.

First, it's immutable. This makes ``Saying`` instances *very* easy to reason about in multi-threaded
environments as well as single-threaded environments. Second, it uses the Java Bean standard for the
``id`` and ``content`` properties. This allows Jackson_ to serialize it to the JSON we need. The
Jackson object mapping code will populate the ``id`` field of the JSON object with the return value
of ``#getId()``, likewise with ``content`` and ``#getContent()``.

.. note::
    
    The JSON serialization here is done by Jackson, which supports far more than simple JavaBean
    objects like this one. In addition to the sophisticated set of
    `annotations`__, you can even write your own custom serializers and
    deserializers.

.. __: http://wiki.fasterxml.com/JacksonAnnotations

Now that we've got our representation class, it makes sense to start in on the
resource it represents.

.. _gs-resource:

Creating A Resource Class
=========================

Jersey resources are the meat-and-potatoes of a Dropwizard service. Each resource class is
associated with a URI template. For our service, we need a resource which returns new ``Saying``
instances from the URI ``/hello-world``, so our resource class will look like this:

.. code-block:: java

    package com.example.helloworld.resources;
    
    import com.example.helloworld.core.Saying;
    import com.google.common.base.Optional;
    import com.yammer.metrics.annotation.Timed;
    
    import javax.ws.rs.GET;
    import javax.ws.rs.Path;
    import javax.ws.rs.Produces;
    import javax.ws.rs.QueryParam;
    import javax.ws.rs.core.MediaType;
    import java.util.concurrent.atomic.AtomicLong;
    
    @Path("/hello-world")
    @Produces(MediaType.APPLICATION_JSON)
    public class HelloWorldResource {
        private final String template;
        private final String defaultName;
        private final AtomicLong counter;
    
        public HelloWorldResource(String template, String defaultName) {
            this.template = template;
            this.defaultName = defaultName;
            this.counter = new AtomicLong();
        }
    
        @GET
        @Timed
        public Saying sayHello(@QueryParam("name") Optional<String> name) {
            return new Saying(counter.incrementAndGet(),
                              String.format(template, name.or(defaultName)));
        }
    }

Finally, we're in the thick of it! Let's start from the top and work our way down.

``HelloWorldResource`` has two annotations: ``@Path`` and ``@Produces``. ``@Path("/hello-world")``
tells Jersey that this resource is accessible at the URI ``/hello-world``, and
``@Produces(MediaType.APPLICATION_JSON)`` lets Jersey's content negotiation code know that this
resource produces representations which are ``application/json``.

``HelloWorldResource`` takes two parameters for construction: the ``template`` it uses to produce
the saying and the ``defaultName`` used when the user declines to tell us their name. An
``AtomicLong`` provides us with a cheap, thread-safe way of generating unique(ish) IDs.

.. warning::

    Resource classes are used by multiple threads concurrently. In general, we recommend that
    resources be stateless/immutable, but it's important to keep the context in mind.

``#sayHello(Optional<String>)`` is the meat of this class, and it's a fairly simple method. The
``@QueryParam("name")`` annotation tells Jersey to map the ``name`` parameter from the query string
to the ``name`` parameter in the method. If the client sends a request to
``/hello-world?name=Dougie``, ``sayHello`` will be called with ``Optional.of("Dougie")``; if there
is no ``name`` parameter in the query string, ``sayHello`` will be called with ``Optional.absent()``.
(Support for Guava's ``Optional`` is a little extra sauce that Dropwizard adds to Jersey's existing
functionality.)

Inside the ``sayHello`` method, we increment the counter, format the template using
``String.format(String, Object...)``, and return a new ``Saying`` instance.

Because ``sayHello`` is annotated with ``@Timed``, Dropwizard automatically records the duration and
rate of its invocations as a Metrics Timer.

Once ``sayHello`` has returned, Jersey takes the ``Saying`` instance and looks for a provider class
which can write ``Saying`` instances as ``application/json``. Dropwizard has one such provider built
in which allows for producing and consuming Java objects as JSON objects. The provider writes out
the JSON and the client receives a ``200 OK`` response with a content type of ``application/json``.

.. _gs-resource-register:

Registering A Resource
----------------------

Before that will actually work, though, we need to go back to ``HelloWorldService`` and add this new
resource class. In its ``run`` method we can read the template and default name from the
``HelloWorldConfiguration`` instance, create a new ``HelloWorldService`` instance, and then add it
to the service's environment:
    
.. code-block:: java

    @Override
    public void run(HelloWorldConfiguration configuration,
                    Environment environment) {
        final String template = configuration.getTemplate();
        final String defaultName = configuration.getDefaultName();
        environment.addResource(new HelloWorldResource(template, defaultName));
    }

When our service starts, we create a new instance of our resource class with the
parameters from the configuration file and hand it off to the ``Environment``,
which acts like a registry of all the things your service can do.

.. note::

    A Dropwizard service can contain *many* resource classes, each corresponding to its own URI
    pattern. Just add another ``@Path``-annotated resource class and call ``addResource`` with an
    instance of the new class.

Before we go too far, we should add a health check for our service.

.. _gs-healthcheck:

Creating A Health Check
=======================

Health checks give you a way of adding small tests to your service to allow you and your ops team to
verify that your service is functioning correctly in production. We **strongly** recommend that all
of your services have at least a minimal set of health checks.

.. note::

    We recommend this so strongly, in fact, that Dropwizard will nag you should you neglect to add a
    health check to your project.

Since formatting strings is not likely to fail while a service is running (unlike, say, a database
connection pool), we'll have to get a little creative here. We'll add a health check to make sure we
can actually format the provided template:

.. code-block:: java

    package com.example.helloworld.health;
    
    import com.yammer.metrics.core.HealthCheck;
    
    public class TemplateHealthCheck extends HealthCheck {
        private final String template;
    
        public TemplateHealthCheck(String template) {
            super("template");
            this.template = template;
        }
    
        @Override
        protected Result check() throws Exception {
            final String saying = String.format(template, "TEST");
            if (!saying.contains("TEST")) {
                return Result.unhealthy("template doesn't include a name");
            }
            return Result.healthy();
        }
    }


``TemplateHealthCheck`` checks for two things: that the provided template is actually a well-formed
format string, and that the template actually produces output with the given name.

If the string is not a well-formed format string (for example, someone accidentally put
``Hello, %s%`` in the configuration file), then ``String.format(String, Object...)`` will throw an
``IllegalFormatException`` and the health check will implicitly fail. If the rendered saying doesn't
include the test string, the health check will explicitly fail by returning an unhealthy ``Result``.

.. _gs-healthcheck-add:

Adding A Health Check
---------------------

As with most things in Dropwizard, we create a new instance with the appropriate parameters and add
it to the ``Environment``:

.. code-block:: java

    @Override
    public void run(HelloWorldConfiguration configuration,
                    Environment environment) {
        final String template = configuration.getTemplate();
        final String defaultName = configuration.getDefaultName();
        environment.addResource(new HelloWorldResource(template, defaultName));
        environment.addHealthCheck(new TemplateHealthCheck(template));
    }


Now we're almost ready to go!

.. _gs-building:

Building Fat JARs
=================

We recommend that you build your Dropwizard services as "fat" JAR files-single ``.jar`` files which
contain *all* of the ``.class`` files required to run your service. This allows you to build a
single deployable artifact which you can promote from your staging environment to your QA
environment to your production environment without worrying about differences in installed
libraries. To start building our Hello World service as a fat JAR, we need to configure a Maven
plugin called ``maven-shade``. In the ``<build><plugins>`` section of your ``pom.xml`` file, add
this:

.. code-block:: xml
    :emphasize-lines: 6,8,9,10,11,12,13,14,15,26,27,28,29

    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
        <version>1.6</version>
        <configuration>
            <createDependencyReducedPom>true</createDependencyReducedPom>
            <filters>
                <filter>
                    <artifact>*:*</artifact>
                    <excludes>
                        <exclude>META-INF/*.SF</exclude>
                        <exclude>META-INF/*.DSA</exclude>
                        <exclude>META-INF/*.RSA</exclude>
                    </excludes>
                </filter>
            </filters>
        </configuration>
        <executions>
            <execution>
                <phase>package</phase>
                <goals>
                    <goal>shade</goal>
                </goals>
                <configuration>
                    <transformers>
                        <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
                        <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                            <mainClass>com.example.helloworld.HelloWorldService</mainClass>
                        </transformer>
                    </transformers>
                </configuration>
            </execution>
        </executions>
    </plugin>

This configures Maven to do a couple of things during its ``package`` phase:

* Produce a ``pom.xml`` file which doesn't include dependencies for the libraries whose contents are
  included in the fat JAR.
* Exclude all digital signatures from signed JARs. If you don't, then Java considers the signature
  invalid and won't load or run your JAR file.
* Collate the various ``META-INF/services`` entries in the JARs instead of overwriting them. (Jersey
  doesn't work without those.)
* Set ``com.example.helloworld.HelloWorldService`` as the JAR's ``MainClass``. This will allow you
  to run the JAR using ``java -jar``.

.. warning::

    If your application has a dependency which *must* be signed (e.g., a `JCA/JCE`__ provider or
    other trusted library), you have to add an exclusion__ to the ``maven-shade-plugin``
    configuration for that library and include that JAR in the classpath.

.. __: http://docs.oracle.com/javase/7/docs/technotes/guides/security/crypto/CryptoSpec.html
.. __: http://maven.apache.org/plugins/maven-shade-plugin/examples/includes-excludes.html

.. _gs-versions:

Versioning Your JARs
--------------------

Dropwizard can also use the project version if it's embedded in the JAR's manifest as the
``Implementation-Version``. To embed this information using Maven, add the following to the
``<build><plugins>`` section of your ``pom.xml`` file:

.. code-block:: xml

    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-jar-plugin</artifactId>
        <version>2.3.2</version>
        <configuration>
            <archive>
                <manifest>
                    <addDefaultImplementationEntries>true</addDefaultImplementationEntries>
                </manifest>
            </archive>
        </configuration>
    </plugin>

This can be handy when trying to figure out what version of your service you have deployed on a
machine.

Once you've got that configured, go into your project directory and run ``mvn package`` (or run the
``package`` goal from your IDE). You should see something like this:

.. code-block:: text

    [INFO] Including org.eclipse.jetty:jetty-util:jar:7.6.0.RC0 in the shaded jar.
    [INFO] Including com.google.guava:guava:jar:10.0.1 in the shaded jar.
    [INFO] Including com.google.code.findbugs:jsr305:jar:1.3.9 in the shaded jar.
    [INFO] Including org.hibernate:hibernate-validator:jar:4.2.0.Final in the shaded jar.
    [INFO] Including javax.validation:validation-api:jar:1.0.0.GA in the shaded jar.
    [INFO] Including org.yaml:snakeyaml:jar:1.9 in the shaded jar.
    [INFO] Replacing original artifact with shaded artifact.
    [INFO] Replacing /Users/yourname/Projects/hello-world/target/hello-world-0.0.1-SNAPSHOT.jar with /Users/yourname/Projects/hello-world/target/hello-world-0.0.1-SNAPSHOT-shaded.jar
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    [INFO] Total time: 8.415s
    [INFO] Finished at: Fri Dec 02 16:26:42 PST 2011
    [INFO] Final Memory: 11M/81M
    [INFO] ------------------------------------------------------------------------

**Congratulations!** You've built your first Dropwizard project! Now it's time to
run it!

.. _gs-running:

Running Your Service
====================

Now that you’ve built a JAR file, it's time to run it.

In your project directory, run this:

.. code-block:: text

    java -jar target/hello-world-0.0.1-SNAPSHOT.jar

You should see something like the following:

.. code-block:: text

    usage: java -jar hello-world-0.0.1-SNAPSHOT.jar
           [-h] [-v] {server} ...

    positional arguments:
      {server}               available commands

    optional arguments:
      -h, --help             show this help message and exit
      -v, --version          show the service version and exit

Dropwizard takes the first command line argument and dispatches it to a matching command. In this
case, the only command available is ``server``, which runs your service as an HTTP server. The
``server`` command requires a configuration file, so let’s go ahead and give it
:ref:`the YAML file we previously saved <gs-yaml-file>`::

    java -jar target/hello-world-0.0.1-SNAPSHOT.jar server hello-world.yml

You should see something like the following:

.. code-block:: text

    INFO  [2011-12-03 00:38:32,927] com.yammer.dropwizard.cli.ServerCommand: Starting hello-world
    INFO  [2011-12-03 00:38:32,931] org.eclipse.jetty.server.Server: jetty-7.x.y-SNAPSHOT
    INFO  [2011-12-03 00:38:32,936] org.eclipse.jetty.server.handler.ContextHandler: started o.e.j.s.ServletContextHandler{/,null}
    INFO  [2011-12-03 00:38:32,999] com.sun.jersey.server.impl.application.WebApplicationImpl: Initiating Jersey application, version 'Jersey: 1.10 11/02/2011 03:53 PM'
    INFO  [2011-12-03 00:38:33,041] com.yammer.dropwizard.config.Environment:

        GET     /hello-world (com.example.helloworld.resources.HelloWorldResource)

    INFO  [2011-12-03 00:38:33,215] org.eclipse.jetty.server.handler.ContextHandler: started o.e.j.s.ServletContextHandler{/,null}
    INFO  [2011-12-03 00:38:33,235] org.eclipse.jetty.server.AbstractConnector: Started BlockingChannelConnector@0.0.0.0:8080 STARTING
    INFO  [2011-12-03 00:38:33,238] org.eclipse.jetty.server.AbstractConnector: Started SocketConnector@0.0.0.0:8081 STARTING

Your Dropwizard service is now listening on ports ``8080`` for service requests and ``8081`` for
administration requests. If you press ``^C``, the service will shut down gracefully, first closing
the server socket, then allowing a few seconds for in-flight requests to be processed, then shutting
down the process itself. (You can disable this to allow for faster restarts during development, by
setting ``http.shutdownGracePeriod`` to ``0s`` in your configuration file.)

But while it's up, let's give it a whirl!
`Click here to say hello! <http://localhost:8080/hello-world>`_
`Click here to get even friendlier! <http://localhost:8080/hello-world?name=Successful+Dropwizard+User>`_

So, we're generating sayings. Awesome. But that's not all your service can do. One of the main
reasons for using Dropwizard is the out-of-the-box operational tools it provides, all of which can
be found `on the admin port <http://localhost:8081/>`_.

If you click through to the `metrics resource <http://localhost:8081/metrics>`_, you can see all of
your service's metrics represented as a JSON object.

The `threads resource <http://localhost:8081/threads>`_ allows you to quickly get a thread dump of
all the threads running in that process.

.. hint:: When a Jetty worker thread is handling an incoming HTTP request, the thread name is set to
          the method and URI of the request. This can be *very* helpful when debugging a
          poorly-behaving request.

The `healthcheck resource <http://localhost:8081/healthcheck>`_ runs the
:ref:`health check class we wrote <gs-healthcheck>`. You should see something like this:

.. code-block:: text

    * deadlocks: OK
    * template: OK


``template`` here is the result of your ``TemplateHealthCheck``, which unsurprisingly passed.
``deadlocks`` is a built-in health check which looks for deadlocked JVM threads and prints out a
listing if any are found.

.. _gs-next:

Next Steps
==========

Well, congratulations. You've got a Hello World service ready for production (except for the lack of
tests) that's capable of doing 15,000-20,000 requests per second. Hopefully you've gotten a feel for
how Dropwizard combines Jetty, Jersey, Jackson, and other stable, mature libraries to provide a
phenomenal platform for developing RESTful web services.

There's a lot more to Dropwizard than is covered here (commands, bundles, servlets, advanced
configuration, validation, HTTP clients, database clients, templates, etc.), all of which is covered
by the :ref:`User Manual <manual-index>`.
