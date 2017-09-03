.. _man-internals:

####################
Dropwizard Internals
####################

You already read through the whole Dropwizard documentation? 
Congrats! Then you are ready to have a look into some nitty-gritty details of Dropwizard.  

Startup Sequence
================

``Application<T extends Configuration>`` is the “Main” class of a dropwizard Application.

``application.run(args)`` is the first method to be called on startup - Here is a simplified code snippet of its implementation:

.. code-block:: java

	public void run(String... arguments) throws Exception {

	  final Bootstrap<T> bootstrap = new Bootstrap<>(this);
	  bootstrap.addCommand(new ServerCommand<>(this));
	  bootstrap.addCommand(new CheckCommand<>(this));

	  initialize(bootstrap); // -- implemented by you; it should call:
		// 1. add bundles (typically being used)
		// 2. add commands (if any)
	  
	  // Should be called after `initialize` to give an opportunity to set a custom metric registry
	  bootstrap.registerMetrics(); // start tracking some default jvm params…

	  // for each cmd, configure parser w/ cmd
	  final Cli cli = new Cli(new JarLocation(getClass()), bootstrap, our, err)
	  cli.run(arguments); 
	}

``Bootstrap`` is the the pre-start (temp) application environment, containing everything required to bootstrap a Dropwizard command. Here is a simplified code snippet to illustrate its structure:

.. code-block:: java

	Bootstrap(application: Application<T>) {
	  this.application = application;
	  this.objectMapper = Jackson.newObjectMapper();
	  this.bundles = new ArrayList<>();
	  this.configuredBundles = new ArrayList<>();
	  this.commands = new ArrayList<>();
	  this.validatorFactory = Validators.newValidatorFactory();
	  this.metricRegistry = new MetricRegistry();
	  this.classLoader = Thread.currentThread().getContextClassLoader();
	  this.onfigurationFactory = new DefaultConfigurationFactoryFactory<>();
	  this.healthCheckRegistry = new HealthCheckRegistry();
	}

``Environment`` is a longer-lived object, holding Dropwizard’s Environment (not env. Such as dev or prod). It holds a similar, but somewhat different set of properties than the Bootsrap object - here is a simplified code snippet to illustrate that:

.. code-block:: java

	Environment (...) {
	  // from bootstrap
	  this.objectMapper = ...
	  this.classLoader = ...  
	  this.metricRegistry = ...
	  this.healthCheckRegistry = ...
	  this.validator = bootstrap.getValidatorFactory().getValidator()

	  // extra:
	  this.bundles = new ArrayList<>();
	  this.configuredBundles = new ArrayList<>();

	  // sub-environments:
	  this.servletEnvironment = ... // -- exposed via the servlets() method 
	  this.jerseyEnvironment = ... // -- exposed via the jersey() method 
	  this.adminEnvironment = ... // -- exposed via the admin() method 

	}

A Dropwizard ``Bundle`` is a reusable group of functionality (sometimes provided by the Dropwizard project itself), used to define blocks of an application’s behavior. 
For example, ``AssetBundle`` from the dropwizard-assets module provides a simple way to serve static assets from your application’s src/main/resources/assets directory as files available from /assets/* (or any other path) in your application.

A ``ConfiguredBundle`` is a bundle that require a configuration provided by the ``Configuration`` object (implementing a relevant interface)

Properties such as database connection details should not be stored on the Environment; that is what your Configuration .yml file is for. 
Each logical environment (dev/test/staging/prod) - would have its own Configuration .yml - reflecting the differences between different “server environments”.

Commands
********

``Command`` objects are basic actions, which Dropwizard runs based on the arguments provided on the command line. The built-in ``server`` command, for example, spins up an HTTP server and runs your application. Each Command subclass has a name and a set of command line options which Dropwizard will use to parse the given command line arguments.
The ``check`` command parses and validates the application's configuration.

If you will check again the first code snippet in this document - you will see creating these two commands, is the first step in the bootstrapping process.

Another important command is ``db`` - allowing executing various db actions, see :ref:`_man-migrations`

Similar to ``ConfiguredBundle``, some commands require access to configuration parameters and should extend the ``ConfiguredCommand`` class, using your application’s ``Configuration`` class as its type parameter. 


The CLI class
*************

Let us begin with a simplified version of the constructor:

.. code-block:: java

	public Cli(location : JarLocation, bootstrap : Bootstrap<?>, 
			   stdOut: OutputStream, stdErr: OutputStream) {
	  this.stdout = stdOut; this.stdErr = stdErr;
	  this.commands = new TreeMap<>();
	  this.parser = buildParser(location);
	  this.bootstrap = bootstrap;
	  for (command in bootstrap.commands) {
		addCommand(command)
	  }
	}

Cli is the command-line runner for Dropwizard application.
Initializing, and then running it - is the last step of the Bootstrapping process.

Run would just handle commandline args (--help, --version) or runs the configured commands.
E.g. - When running the ``server`` command:

.. code-block:: 

  java -jar target/hello-world-0.0.1-SNAPSHOT.jar server hello-world.yml

Just note the two basic commands are built of a parent, and a sub-class:

.. code-block:: java

  class CheckCommand<T extends Configuration> extends ConfiguredCommand<T>
  class ServerCommand<T extends Configuration> extends EnvironmentCommand<T>

The order of operations is therefore:

1. Parse cmdline args, determine subcommand.
2. Run ``ConfiguredCommand``, which get a parameter with the location of a YAML configuration file - parses and validates it.
3. ``CheckCommand.run()`` runs next, and does almost nothing: it logs ``"Configuration is OK"``
4. Run ``EnvironmentCommand``:

  a) Create ``Environment`` 
  b) Calls ``bootstrap.run(cfg, env)`` - run bundles with config. & env.
  c) Bundles run in FIFO order.
  d) Calls ``application.run(cfg, env)`` -- implemented by you
  
6. Now, ``ServerCommand.run()`` runs

  a) Calls ``serverFactory.build(environment)`` - to configure Jetty and Jersey, with all relevant Dropwizard modules.
  b) Starts Jetty.


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
