Dropwizard
==========
[![Build Status](https://travis-ci.org/dropwizard/dropwizard.svg?branch=master)](https://travis-ci.org/dropwizard/dropwizard)
[![Coverage Status](https://img.shields.io/coveralls/dropwizard/dropwizard.svg)](https://coveralls.io/r/dropwizard/dropwizard)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.dropwizard/dropwizard-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.dropwizard/dropwizard-core/)
[![Javadoc](https://javadoc-emblem.rhcloud.com/doc/io.dropwizard/dropwizard-core/badge.svg)](http://www.javadoc.io/doc/io.dropwizard/dropwizard-core)

*Dropwizard is a sneaky way of making fast Java web applications.*

It's a little bit of opinionated glue code which bangs together a set of libraries which have
historically not sucked:

* [Jetty](http://www.eclipse.org/jetty/) for HTTP servin'.
* [Jersey](http://jersey.java.net/) for REST modelin'.
* [Jackson](https://github.com/FasterXML/jackson) for JSON parsin' and generatin'.
* [Logback](http://logback.qos.ch/) for loggin'.
* [Hibernate Validator](http://hibernate.org/validator/) for validatin'.
* [Metrics](http://metrics.dropwizard.io) for figurin' out what your application is doin' in production.
* [JDBI](http://www.jdbi.org) and [Hibernate](http://www.hibernate.org/orm/) for databasin'.
* [Liquibase](http://www.liquibase.org/) for migratin'.

Read more at [dropwizard.io](http://www.dropwizard.io).

Want to contribute to Dropwizard?
---
Before working on the code, if you plan to contribute changes, please read the following [CONTRIBUTING](CONTRIBUTING.md) document.

Need help or found an issue?
---
When reporting an issue through the [issue tracker](https://github.com/dropwizard/dropwizard/issues?state=open)
on GitHub or sending an email to the
[Dropwizard User Google Group](https://groups.google.com/forum/#!forum/dropwizard-user)
mailing list, please use the following guidelines:

* Check existing issues to see if it has been addressed already
* The version of Dropwizard you are using
* A short description of the issue you are experiencing and the expected outcome
* Description of how someone else can reproduce the problem
* Paste error output or logs in your issue or in a Gist. If pasting them in the GitHub
issue, wrap it in three backticks: ```  so that it renders nicely
* Write a unit test to show the issue!
