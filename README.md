Dropwizard
==========

*Dropwizard is a sneaky way of making fast Scala web services.*

It's a little bit of opinionated glue code which bangs together a set of libraries which have historically not sucked:

* [Jetty](http://www.eclipse.org/jetty/) for HTTP servin'.
* [Jersey](http://jersey.java.net/) for REST modelin'.
* [Fig](https://github.com/codahale/fig) for JSON configurin'.
* [Jerkson](https://github.com/codahale/jerkson)/[Jackson](http://jackson.codehaus.org) for JSON parsin' and generatin'.
* [Logula](https://github.com/codahale/logula)/[Log4j](http://logging.apache.org/log4j/1.2/) for loggin'.
* [Metrics](https://github.com/codahale/metrics) for figurin' out what your service is doing in production.

[Yammer](https://www.yammer.com)'s high-performance, low-latency, Scala services all use Dropwizard. In fact, Dropwizard
is really just a simple extraction of [Yammer](https://www.yammer.com)'s glue code.

For more information, check out the [wiki](https://github.com/codahale/dropwizard/wiki).
