.. _upgrade-notes-dropwizard-2_1_x:

##################################
Upgrade Notes for Dropwizard 2.1.x
##################################

Executor Service Metrics
========================

In Dropwizard 2.0.x, metric names for an instrumented ExecutorService
created by ``Environment.executorService(String)`` contained a format
string (i.e. ``-%d``). In DropWizard 2.1.x this has been fixed so
metric names contain the portion of the nameFormat without the
format string. Additionally, nameFormat must contain a format
String, returning to the behavior from pre-2.0.x releases which
used Guava's ThreadFactoryBuilder.

See https://github.com/dropwizard/dropwizard/issues/3139 for more
details on the issue and the fix.
