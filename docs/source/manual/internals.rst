.. _man-internals:

####################
Dropwizard Internals
####################

You already read through the whole Dropwizard documentation? 
Congrats! Then you are ready to have a look into some nitty-gritty details of Dropwizard.  

Startup Sequence
================

`Application<T extends Configuration>` is the “Main” class of a dropwizard Application.

`application.run(args)` in the first method to be called on startup. Here is a simplified implementation:
```java
public void run(String... arguments) throws Exception {

  final Bootstrap<T> bootstrap = new Bootstrap<>(this);
  bootstrap.addCommand(new ServerCommand<>(this));
  bootstrap.addCommand(new CheckCommand<>(this));

  initialize(bootstrap); // -- implemented by us; should call:
    // 1. add bundles -- typically in Next:
          GuiceBundle - connect modules in `di` folder to Guice.
          SwaggerBundle - standard configurations
    // 2. add commands
          Currently: none
  
  // be called after initialize to give option to set a custom metric registry
  bootstrap.registerMetrics(); // start tracking some default jvm params…

  // for each cmd, configure parser w/ cmd
  final Cli cli = new Cli(new JarLocation(getClass()), bootstrap, our, err)
  cli.run(arguments); 
}
```

`Bootstrap` is the The pre-start (temp) application environment, containing everything required to bootstrap a Dropwizard command (simplified code):
```
Bootstrap(application: Application<T>) {
  val this.application = application
  val objectMapper = Jackson.newObjectMapper()
  val bundles = new ArrayList()
  val configuredBundles = new ArrayList()
  val commands = new ArrayList()
  val validatorFactory = Validators.newValidatorFactory();
  val metricRegistry = new MetricRegistry()
  val classLoader = Thread.currentThread().getContextClassLoader()
  val configurationFactory = new DefaultConfigurationFactoryFactory()
  val healthCheckRegistry = new HealthCheckRegistry()
 }
```

`Environment` is a longer lived object, holding Dropwizard’s Environment (not env. Such as dev or prod). It holds similar, but somewhat different set of properties than Bootsrap (simplified):
```
Environment {
  // from bootstrap
  val objectMapper
  val classLoader  
  val metricRegistry
  val healthCheckRegistry
  val validator = bootstrap.getValidatorFactory().getValidator()

  // extra:
  val bundles = new ArrayList()
  val configuredBundles = new ArrayList()


  // sub-environments:
  val servletEnvironment -- exposed via servlets() method 
  val jerseyEnvironment -- exposed via jersey() method 
  val adminEnvironment -- exposed via admin() method 

}
```

A Dropwizard `Bundle` is a reusable group of functionality (typically provided by the Dropwizard project), used to define blocks of an application’s behavior. 
For example, `AssetBundle` from the dropwizard-assets module provides a simple way to serve static assets from your application’s src/main/resources/assets directory as files available from /assets/* (or any other path) in your application.
`ConfiguredBundle` is a bundle that require a configuration provided by the `Configuration` object (implementing a relevant interface)

Properties such as database connection details should not be stored on the Environment; that is what your Configuration .yml file is for. 
Each logical environment (dev/test/staging/prod) - would have its own Configuration .yml - reflecting the differences between “server environments”.

## Commands


Commands are basic actions which Dropwizard runs based on the arguments provided on the command line. The built-in server command, for example, spins up an HTTP server and runs your application. Each Command subclass has a name and a set of command line options which Dropwizard will use to parse the given command line arguments.
The check command parses and validates the application's configuration.

If you will check again the first code snippet in this document - you will see creating these 2 commands are the first step in the bootstrapping process.

Another important command is db - allowing to execute various db actions


Similar to ConfiguredBundle, some commands require access to configuration parameters and should extend the ConfiguredCommand class, using your application’s Configuration class as its type parameter. 
In Next - there is currently no use of custom commands.

The CLI class

public Cli(location : JarLocation, bootstrap : Bootstrap<?>, 
           stdOut: OutputStream, stdErr: OutputStream) {
  This.stdout = stdOut; this.stdErr = stdErr;
  val commands = new TreeMap<>();
  val parser = buildParser(location);
  val bootstrap = bootstrap;
  for (command in bootstrap.commands) {
    addCommand(command)
  }
}

Cli is the command-line runner for Dropwizard application.
Initializing, and then running it - is the last step of the Bootstrapping process.

Run would just handle command lines args (--help, --version) or runs the configured commands.

When running the server command, e.g.
java -jar target/hello-world-0.0.1-SNAPSHOT.jar server hello-world.yml

Just to note 2 of our basic commands have ancestors:
class CheckCommand<T extends Configuration> extends ConfiguredCommand<T>

class ServerCommand<T extends Configuration> extends EnvironmentCommand<T>

The order of operations is therefore:
parse cmdline args, determine subcommand.
Run ConfiguredCommand, which get a parameter with the location of a YAML configuration file - parses and validates it.
CheckCommand.run() runs next, and does almost nothing: it logs "Configuration is OK"
Run EnvironmentCommand:
Create Environment 
Calls bootstrap.run(cfg, env) - run bundles with config. & env.
Bundles run in FIFO order.
Calls application.run(cfg, env) -- implemented by you
Now, ServerCommand.run() runs
Calls serverFactory.build(environment) - to configure Jetty and Jersey, with all relevant Dropwizard modules.
Starts Jetty.


Jetty Lifecycle
===============
If you have a component of your app that needs to know when Jetty is going to start, 
you can implement Managed as described in the dropwizard docs. 

If you have a component that needs to be signaled that Jetty has started 
(this happens after all Managed objects' start() methods are called), 
you can register with the env's lifecycle like:

.. code-block:: java

        env.lifecycle().addServerLifecycleListener(new ServerLifecycleListener() {
            @Override
            public void serverStarted(Server server) {
                      /// ... do things here ....
            }
        });
