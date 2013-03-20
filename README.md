Dropwizard
==========

*Dropwizard is a sneaky way of making fast Java or Scala web services.*

It's a little bit of opinionated glue code which bangs together a set of libraries which have
historically not sucked:

* [Jetty](http://www.eclipse.org/jetty/) for HTTP servin'.
* [Jersey](http://jersey.java.net/) for REST modelin'.
* [Jackson](http://jackson.codehaus.org) for JSON parsin' and generatin'.
* [Logback](http://logback.qos.ch/) for loggin'.
* [Hibernate Validator](http://www.hibernate.org/subprojects/validator.html) for validatin'.
* [Metrics](https://github.com/codahale/metrics) for figurin' out what your service is doin' in
  production.
* [SnakeYAML](http://code.google.com/p/snakeyaml/) for YAML parsin' and configuratin'.

[Yammer](https://www.yammer.com)'s high-performance, low-latency, Java and Scala services all use
Dropwizard. In fact, Dropwizard is really just a simple extraction of
[Yammer](https://www.yammer.com)'s glue code.

Read more at [dropwizard.codahale.com](http://dropwizard.codahale.com).
