Dropwizard
==========
[![Build](https://github.com/dropwizard/dropwizard/workflows/Java%20CI/badge.svg)](https://github.com/dropwizard/dropwizard/actions?query=workflow%3A%22Java+CI%22)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=dropwizard_dropwizard&metric=alert_status)](https://sonarcloud.io/dashboard?id=dropwizard_dropwizard)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.dropwizard/dropwizard-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.dropwizard/dropwizard-core/)
[![Javadocs](https://javadoc.io/badge/io.dropwizard/dropwizard-project.svg?color=brightgreen)](https://javadoc.io/doc/io.dropwizard/dropwizard-project)
[![Documentation Status](https://readthedocs.org/projects/dropwizard/badge/?version=stable)](https://www.dropwizard.io/en/stable/?badge=stable)
[![Maintainability](https://api.codeclimate.com/v1/badges/11a16ea08c8b5499e2b9/maintainability)](https://codeclimate.com/github/dropwizard/dropwizard/maintainability)
[![Reproducible Builds](https://img.shields.io/badge/Reproducible_Builds-ok-green?labelColor=blue)](https://github.com/jvm-repo-rebuild/reproducible-central#io.dropwizard:dropwizard-core)
[![Contribute with Gitpod](https://img.shields.io/badge/Contribute%20with-Gitpod-908a85?logo=gitpod)](https://gitpod.io/#https://github.com/dropwizard/dropwizard)

*Dropwizard is a sneaky way of making fast Java web applications.*

It's a little bit of opinionated glue code which bangs together a set of libraries which have
historically not sucked:

* [Jetty](http://www.eclipse.org/jetty/) for HTTP servin'.
* [Jersey](https://jersey.github.io/) for REST modelin'.
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

Sponsors
--------

Dropwizard is generously supported by some companies with licenses and free accounts for their products.

### JetBrains

![JetBrains](docs/source/about/jetbrains.png)

[JetBrains](https://www.jetbrains.com/) supports our open source project by sponsoring some [All Products Packs](https://www.jetbrains.com/products.html) within their [Free Open Source License](https://www.jetbrains.com/buy/opensource/) program.
