Title: Getting Started
Template: manual.html.erb
Order: 2
* * *

The goal of this document is to guide you through the process of
creating a simple Dropwizard project: Hello World. Along the way, we’ll
explain the various underlying libraries and their roles, important
concepts in Dropwizard, and suggest some organizational techniques to
help you as your project grows. (Or you can just skip to the
[fun part](#maven).)

## Overview #overview

Dropwizard straddles the line between being a library and a framework.
Its goal is to provide performant, reliable implementations of
everything a production-ready web service needs. Because this
functionality is extracted into a reusable library, your service remains
lean and focused, reducing both time-to-market and maintenance burdens.

### Jetty for HTTP

Because you can't be a web service without HTTP, Dropwizard uses the
[Jetty](http://www.eclipse.org/jetty/) HTTP library to embed an
incredibly tuned HTTP server directly into your project. Instead of
handing your service off to a complicated application server, Dropwizard
projects have a `main` method which spins up an HTTP server. Running
your service as a simple process eliminates a number of unsavory aspects
of Java in production (no PermGem issues, no application server
configuration and maintenance, no arcane deployment tools, no
`ClassLoader` troubles, no hidden application logs, no trying to tune a
single garbage collector to work with multiple application workloads)
and allows you to use all of Unix's existing process management tools
instead.

### Jersey for REST

For building RESTful web services, we've found nothing beats
[Jersey](http://jersey.java.net) (the
[JAX-RS](http://jcp.org/en/jsr/detail?id=311) reference implementation)
in terms of features or performance. It allows you to write clean,
testable classes which gracefully map HTTP requests to simple Java
objects. It supports streaming output, matrix URI parameters,
conditional `GET` requests, and much, much more.

### Jackson for JSON

In terms of data formats, JSON has become the web’s *lingua franca*, and
[Jackson](http://jackson.codehaus.org/) is the king of JSON on the JVM.
In addition to being lightning fast, it has a sophisticated object
mapper, allowing you to export your domain models directly.

### Metrics for metrics

Our very own [Metrics](https://github.com/codahale/metrics) library
rounds things out, providing you with unparalleled insight into your
code’s behavior in your production environment.

### And Friends

In addition to Jetty, Jersey, and Jackson, Dropwizard also includes a
number of libraries that we've come to rely on:

-   [Guava](http://code.google.com/p/guava-libraries/), which, in
    addition to highly optimized immutable data structures, provides a
    growing number of classes to speed up development in Java.
-   [Log4j](http://logging.apache.org/log4j/1.2/) and
    [slf4j](http://www.slf4j.org/) for performant logging.
-   [Hibernate
    Validator](http://www.hibernate.org/subprojects/validator.html), the
    [JSR-303](http://jcp.org/en/jsr/detail?id=303) reference
    implementation, provides an easy, declarative framework for
    validating user input and generating helpful, internationalizable
    error messages.
-   [Apache
    HttpClient](http://hc.apache.org/httpcomponents-client-ga/index.html)
    and Jersey's client library allow for both low- and high-level
    interaction with other web services.
-   [JDBI](http://www.jdbi.org) is the most straight-forward way to use
    a relational database with Java.
-   [Freemarker](http://freemarker.sourceforge.net/) is a simple
    template system for more user-facing services.

Now that you've gotten the lay of the land, let's dig in!

* * * * *

## Setting Up Maven #maven

We recommend you use [Maven](http://maven.apache.org) for new Dropwizard
services. If you're a big
[Ant](http://ant.apache.org/)/[Ivy](http://ant.apache.org/ivy/),
[Buildr](http://buildr.apache.org/), [Gradle](http://www.gradle.org/),
[SBT](https://github.com/harrah/xsbt/wiki), or
[Gant](http://gant.codehaus.org/) fan, that’s cool, but we use Maven and
we'll be using Maven as we go through this example service. If you have
any questions about how Maven works, [Maven: The Complete
Reference](http://www.sonatype.com/books/mvnref-book/reference/) should
have what you’re looking for. (We’re assuming you know how to create a
new Maven project.)

Add the `dropwizard-core` library as a dependency:

``` xml
<dependencies>
    <dependency>
        <groupId>com.yammer.dropwizard</groupId>
        <artifactId>dropwizard-core</artifactId>
        <version>0.1.3</version>
    </dependency>
</dependencies>
```

Alright, that’s enough XML. We’ve got a Maven project set up now, and
it’s time to start writing real code.

* * * * *

## Creating A Configuration Class #configuration

Each Dropwizard service has its own subclass of the `Configuration`
class which specify environment-specific parameters. These parameters
are specified in a [YAML](http://www.yaml.org/) configuration file which
is deserialized to an instance of your service’s configuration class and
validated.

The service we’re building is a high-performance Hello World service,
and part of our requirements is that we need to be able to vary how it
says hello from environment to environment. We’ll need to specify at
least two things to begin with: a template for saying hello and a
default name to use in case the user doesn’t specify their name.

Here's what our configuration class will look like:

``` java
package com.example.helloworld;

import com.yammer.dropwizard.config.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

public class HelloWorldConfiguration extends Configuration {
    @NotEmpty
    private String template;

    @NotEmpty
    private String defaultName = "Stranger";

    public String getTemplate() {
        return template;
    }

    public String getDefaultName() {
        return defaultName;
    }
}
```

There's a lot going on here, so let’s unpack a bit of it.

When this class is deserialized from the YAML file, it will pull two
root-level fields from the YAML object: `template`, the template for our
Hello World saying, and `defaultName`, the default name to use. Both
`template` and `defaultName` are annotated with `@NotEmpty`, so if the
YAML configuration file has blank values for either or is missing
`template` entirely an informative exception will be thrown and your
service won’t start.

Our YAML file, then, will look like this:

``` yaml
template: Hello, %s!
defaultName: Stranger
```

Dropwizard has a *lot* more configuration parameters than that, but they
all have sane defaults so you can keep your configuration files small
and focused.

So save that YAML file as `hello-world.yml`, because we’ll be getting up
and running pretty soon and we’ll need it. Next up, we’re creating our
service class!

* * * * *

## Creating A Service Class #service

Combined with your project’s `Configuration` subclass, its `Service`
form the core of your Dropwizard service. The `Service` class pulls
together the various bundles and commands which provide basic
functionality. (More on that later.) For now, though, our
`HelloWorldService` looks like this:

``` java
package com.example.helloworld;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Environment;

public class HelloWorldService extends Service<HelloWorldConfiguration> {
    public static void main(String[] args) throws Exception {
        new HelloWorldService().run(args);
    }

    private HelloWorldService() {
        super("hello-world");
    }

    @Override
    protected void initialize(HelloWorldConfiguration configuration,
                              Environment environment) {
        // nothing to do yet
    }

}
```

As you can see, `HelloWorldService` is parameterized with the service’s
configuration type, `HelloWorldConfiguration`. `HelloWorldService`’s
constructor provides the service’s name: `hello-world`. Also, we’ve
added a `static` `main` method, which will be our service’s entry point.
Right now, we don’t have any functionality implemented, so our
`initialize` method is a little boring. Let’s fix that!

* * * * *

## Creating A Representation Class #representation

Before we can get into the nuts-and-bolts of our Hello World service, we
need to stop and think about our API. Luckily, our service needs to
conform to an industry standard, [RFC
1149](http://www.ietf.org/rfc/rfc1149.txt "This is a joke."), which
specifies the following JSON representation of a Hello World saying:

``` javascript
{
  "id": 1,
  "content": "Hello, stranger!"
}
```

The `id` field is a unique identifier for the saying, and `content` is
the textual representation of the saying. (Thankfully, this is a fairly
straight-forward industry standard.)

To model this representation, we'll create a representation class:

``` javascript
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
```

This is a pretty simple POJO, but there are a few things worth noting
here.

First, it’s immutable. This makes `Saying` instances *very* easy to
reason about multi-threaded environments as well as single-threaded
environments. Second, it uses the Java Bean standard for the `id` and
`content` properties. This allows Jackson to serialize it to the JSON we
need. The Jackson object mapping code will populate the `id` field of
the JSON object with the return value of `getId()`, likewise with
`content` and `getContent()`.

Now that we’ve got our representation class, it makes sense to start in
on the resource it represents.

* * * * *

## Creating A Resource Class #resource

Jersey resources are the meat-and-potatoes of a Dropwizard service. Each
resource class is associated with a URI template. For our service, we
need a resource which returns new `Saying` instances from the URI
`/hello-world`, so our resource class will look like this:

``` java
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
```

Finally, we’re in the thick of it! Let’s start from the top and work our
way down.

`HelloWorldResource` has two annotations: `@Path` and `@Produces`.
`@Path("/hello-world")` tells Jersey that this resource is accessible at
the URI `/hello-world`, and `@Produces(MediaType.APPLICATION_JSON)` lets
Jersey’s content negotiation code know that this resource produces
representations which are `application/json`.

`HelloWorldResource` takes two parameters for construction: the
`template` it uses to produce the saying and the `defaultName` used when
the user declines to tell us their name. An `AtomicLong` provides us
with a cheap, thread-safe way of generating unique(ish) IDs.

**Remember**: Resource classes are used by multiple threads
concurrently. In general, we recommend that resources be
stateless/immutable, but it’s important to keep the context in mind.

`sayHello(Optional<String>)` is the meat of this class, and it’s a
fairly simple method. The `@QueryParam("name")` tells Jersey to map the
`name` parameter from the query string to the `name` parameter in the
method. If the client sends a request to `/hello-world?name=Dougie`,
`sayHello` will be called with `Optional.of("Dougie")`; if there is no
`name` parameter in the query string, `sayHello` will be called with
`Option.absent()`. (Support for Guava's `Optional` is a little extra
sauce that Dropwizard adds to Jersey's existing functionality.)

Inside the `sayHello` method, we increment the counter, format the
template using `String.format(String, Object...)`, and return a new
`Saying` instance.

Because `sayHello` is annotated with `@Timed`, Dropwizard automatically
records the duration and rate of its invocations as a Metrics Timer.

Once `sayHello` has returned, Jersey takes the `Saying` instance and
looks for a provider class which can write `Saying` instances as
`application/json`. Dropwizard has one such provider built in which
allows for producing and consuming Java objects as JSON objects. The
provider writes out the JSON and the client receives a `200 OK` response
with a content type of `application/json`.

Before that will actually work, though, we need to go back to our
`HelloWorldService` and add this new resource class. In its `initialize`
method we can read the template and default name from the
`HelloWorldConfiguration` instance, create a new `HelloWorldService`
instance, and then add it to the service’s environment:

``` java
@Override
protected void initialize(HelloWorldConfiguration configuration,
                          Environment environment) {
    final String template = configuration.getTemplate();
    final String defaultName = configuration.getDefaultName();
    environment.addResource(new HelloWorldResource(template, defaultName));
}
```

When our service starts, we create a new instance of our resource class
with the parameters from the configuration file and hand it off to the
`Environment`, which acts like a registry of all the things your service
can do.

Before we go too far, we should add a health check for our service.

* * * * *

## Adding A Health Check #healthcheck

Health checks give you a way of adding small tests to your service to
allow you and your ops team to verify that your service is functioning
correctly in production. We **strongly** recommend that all of your
services have at least a minimal set of health checks. (We recommend
this so strongly, in fact, that Dropwizard will nag you should you
neglect to add a health check to your project.)

Since formatting strings is not likely to fail while a service is
running (unlike, say, a database connection pool), we’ll have to get a
little creative here. We’ll add a health check to make sure we can
actually format the provided template:

``` java
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
```

`TemplateHealthCheck` checks for two things: that the provided template
is actually a well-formed format string, and that the template actually
produces output with the given name.

If the string is not a well-formed format string (for example, someone
accidentally put `Hello, %s%` in the configuration file), then
`String.format(String, Object...)` will throw an
`IllegalFormatException` and the health check will implicitly fail. If
the rendered saying doesn’t include the test string, the health check
will explicitly fail by returning an unhealthy `Result`.

As with most things in Dropwizard, we create a new instance with the
appropriate parameters and add it to the `Environment`:

``` java
@Override
protected void initialize(HelloWorldConfiguration configuration,
                          Environment environment) {
    final String template = configuration.getTemplate();
    final String defaultName = configuration.getDefaultName();
    environment.addResource(new HelloWorldResource(template, defaultName));
    environment.addHealthCheck(new TemplateHealthCheck(template));
}
```

Now we're almost ready to go!

* * * * *

## Building Fat JARs #building

We recommend that you build your Dropwizard services as “fat” JAR
files—single `.jar` files which contain *all* of the `.class` files
required to run your service. This allows you to build a single
deployable artifact which you can copy from your staging environment to
your QA environment to your production environment without worrying
about differences in installed libraries. To start building our Hello
World service as a fat JAR, we need to configure a Maven plugin called
`maven-shade`. In your `pom.xml` file, add this:

``` xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>1.4</version>
    <configuration>
        <createDependencyReducedPom>true</createDependencyReducedPom>
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
        
```

This configures Maven to do a couple of things during its `package`
phase:

-   Produce a `pom.xml` file which doesn’t include dependencies for the
    libraries whose contents are included in the fat JAR.
-   Collate the various `META-INF/services` entries in the JARs instead
    of overwriting them. (Jersey doesn’t work without those.)
-   Set `com.example.helloworld.HelloWorldService` as the JAR’s
    `MainClass`.

Once you’ve got that configured, go into your project directory and run
`mvn package` (or run the `package` goal from your IDE). You should see
something like this:

```
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
```

**Congratulations!** You’ve built your first Dropwizard project! Now
it’s time to run it!

* * * * *

## Running Your Service #running

Now that you’ve built a JAR file, it’s time to run it.

In your project directory, run
`java -jar target/hello-world-0.0.1-SNAPSHOT.jar`. You should see
something like the following:

```
java -jar dropwizard-example-0.1.0-SNAPSHOT.jar <command> [arg1 arg2]

Commands
========

server: Starts an HTTP server running the service
-------------------------------------------------
usage: java -jar dropwizard-example-0.1.0-SNAPSHOT.jar server <config
            file>
 -h, --help   display usage information
```

Dropwizard takes the first command line argument and dispatches it to a
matching command. In this case, the only command available is `server`,
which runs your service as an HTTP server. The `server` command requires
a configuration file, so let’s go ahead and give it
[the one we previous saved](#configuration):
`java -jar target/hello-world-0.0.1-SNAPSHOT.jar server hello-world.yml`

You should see something like the following:

```
INFO  [2011-12-03 00:38:32,927] com.yammer.dropwizard.cli.ServerCommand: Starting hello-world
INFO  [2011-12-03 00:38:32,931] org.eclipse.jetty.server.Server: jetty-7.x.y-SNAPSHOT
INFO  [2011-12-03 00:38:32,936] org.eclipse.jetty.server.handler.ContextHandler: started o.e.j.s.ServletContextHandler{/,null}
INFO  [2011-12-03 00:38:32,999] com.sun.jersey.server.impl.application.WebApplicationImpl: Initiating Jersey application, version 'Jersey: 1.10 11/02/2011 03:53 PM'
INFO  [2011-12-03 00:38:33,041] com.yammer.dropwizard.config.Environment:

    GET     /hello-world (com.example.helloworld.resources.HelloWorldResource)

INFO  [2011-12-03 00:38:33,215] org.eclipse.jetty.server.handler.ContextHandler: started o.e.j.s.ServletContextHandler{/,null}
INFO  [2011-12-03 00:38:33,235] org.eclipse.jetty.server.AbstractConnector: Started BlockingChannelConnector@0.0.0.0:8080 STARTING
INFO  [2011-12-03 00:38:33,238] org.eclipse.jetty.server.AbstractConnector: Started SocketConnector@0.0.0.0:8081 STARTING
```

Your Dropwizard service is now listening on ports `8080` for service
requests and `8081` for administration requests. If you press \^C, the
service will shut down gracefully, first closing the server socket, then
allowing a few seconds for in-flight requests to be processed, then
shutting down the process itself.

But while it’s up, let’s give it a whirl! [Click here to say
hello!](http://localhost:8080/hello-world) [Click here to get even
friendlier!](http://localhost:8080/hello-world?name=Successful+Dropwizard+User)

So, we’re generating sayings. Awesome. But that’s not all your service
can do. One of the main reasons for using Dropwizard is the
out-of-the-box operational tools it provides, all of which can be found
[on the admin port](http://localhost:8081/).

If you click through to [metrics](http://localhost:8081/metrics), you
can see all of your service’s metrics represented as a JSON object.

The [threads](http://localhost:8081/threads) resource allows you to
quickly get a thread dump of all the threads running in that process.

The [healthcheck](http://localhost:8081/healthcheck) resource runs the
[`TemplateHealthCheck` instance we wrote](#healthcheck). You should see
something like this:

```
* deadlocks: OK
* template: OK
```

`template` here is the result of your `TemplateHealthCheck`, which
unsurprisingly passed. `deadlocks` is a built-in health check which
looks for deadlocked JVM threads and prints out a listing if any are
found.

* * * * *

## Next Steps #next

Well, congratulations. You’ve got a Hello World service ready for
production (except for the lack of tests) that’s capable of doing
15,000-20,000 requests per second. Hopefully you've gotten a feel for
how Dropwizard combines Jetty, Jersey, Jackson, and other stable, mature
libraries to provide a phenomenal platform for developing RESTful web
services.

There’s a lot more to Dropwizard than is covered here (commands,
bundles, servlets, advanced configuration, validation, HTTP clients,
database clients, templates, etc.), all of which is covered in
[by Dropwizard's manual.](manual.html)

<p><a class="btn" href="manual.html">Learn more about Dropwizard &raquo;</a></p>

<p><a class="btn" href="http://www.sonatype.com/books/mvnref-book/reference/">Learn more about Maven &raquo;</a></p>

<p><a class="btn" href="http://wiki.eclipse.org/Jetty">Learn more about Jetty &raquo;</a></p>

<p><a class="btn" href="http://jersey.java.net/nonav/documentation/latest/user-guide.html">Learn more about Jersey &raquo;</a></p>

<p><a class="btn" href="http://wiki.fasterxml.com/JacksonDocumentation">Learn more about Jackson &raquo;</a></p>

<p><a class="btn" href="http://code.google.com/p/snakeyaml/">Learn more about YAML &raquo;</a></p>