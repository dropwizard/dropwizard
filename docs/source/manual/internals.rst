.. _man-internals:

####################
Dropwizard Internals
####################

You already read through the whole Dropwizard documentation? 
Congrats! Then you are ready to have a look into some nitty-gritty details of Dropwizard.  

Startup Sequence
================

Below you find the startup sequence of a Dropwizard Application: 

#. Application.run(args)

   #. new Bootstrap
   #. bootstrap.addCommand(new ServerCommand)
   #. bootstrap.addCommand(new CheckCommand)
   #. initialize(bootstrap) (implemented by your Application)
   
      #. bootstrap.addBundle(bundle)
      
         #. bundle.initialize(bootstrap)
         
      #. bootstrap.addCommand(cmd)
      
         #. cmd.initialize()
         
   #. new Cli(bootstrap and other params)
   
      #. for each cmd in bootstrap.getCommands()
      
         #. configure parser w/ cmd
         
   #. cli.run()
   
      #. is help flag on cmdline?  if so, print usage
      #. parse cmdline args, determine subcommand  (rest of these notes are specific to ServerCommand)
      #. command.run(bootstrap, namespace) (implementation in ConfiguredCommand)
      
         #. parse configuration
         #. setup logging
         
      #. command.run(bootstrap, namespace, cfg) (implementation in EnvironmentCommand)
      
         #. create Environment
         #. bootstrap.run(cfg, env)
         
            #. for each Bundle: bundle.run()
            #. for each ConfiguredBundle: bundle.run()
            
         #. application.run(cfg, env) (implemented by your Application)
        
   #. command.run(env, namespace, cfg) (implemented by ServerCommand)
   
      #. starts Jetty
      

On Bundles
==========

Running bundles happens in FIFO order (ConfiguredBundles are always run after Bundles).

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