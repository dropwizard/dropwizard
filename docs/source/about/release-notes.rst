.. _release-notes:

#############
Release Notes
#############

Please refer to `GitHub releases <https://github.com/dropwizard/dropwizard/releases>`__ for the most up-to-date release notes.

.. _rel-2.0.0:

v2.0.0: Dec 10, 2019
====================

* :ref:`upgrade-notes-dropwizard-2_0_x`
* `GitHub 2.0.0 milestone <https://github.com/dropwizard/dropwizard/pulls?page=1&q=is%3Apr+is%3Aclosed+milestone%3A2.0.0>`_
* Add TLS socket logging appender (`#2317 <https://github.com/dropwizard/dropwizard/pull/2317>`_)
* Add opt-in ``EmptyOptionalNoContentExceptionMapper`` for returning 204 responses on empty ``Optional`` responses (`#2350 <https://github.com/dropwizard/dropwizard/pull/2350>`_)
* Add configuration for excluding mime types and paths to gzip (`#2356 <https://github.com/dropwizard/dropwizard/pull/2356>`_)
* Support expirable log level configurations (`#2375 <https://github.com/dropwizard/dropwizard/pull/2375>`_)
* Add additional syslog logging facilities (`#2381 <https://github.com/dropwizard/dropwizard/pull/2381>`_)
* Add opt-in logging throttling via the ``messageRate`` config property (`#2384 <https://github.com/dropwizard/dropwizard/pull/2384>`_)
* Fix ``UUIDParams`` accepting input of incorrect length (`#2382 <https://github.com/dropwizard/dropwizard/pull/2382>`_)
* Fix usage ``@SelfValidating`` with ``@BeanParam`` (`#2334 <https://github.com/dropwizard/dropwizard/pull/2334>`_, `#2335 <https://github.com/dropwizard/dropwizard/issues/2335>`_)
* Fix resource endpoints injected via DI not being logged on startup (`#2389 <https://github.com/dropwizard/dropwizard/pull/2389>`_)
* Disable protocols less secure than TLS v1.2 by default (`#2417 <https://github.com/dropwizard/dropwizard/pull/2417>`_)
* Add ``totalSizeCap`` to file log appender (`#2502 <https://github.com/dropwizard/dropwizard/pull/2502>`_)
* Gzipped content encoded requests and responses are compatible with Servlet 3.1 and Async IO (`#2566 <https://github.com/dropwizard/dropwizard/pull/2566>`_)
* Retired use of deprecated Apache ``StrSubstitutor`` and ``StrLookup`` classes and replaced them with Apache's ``StringSubstitutor`` and ``StringLookup`` (`#2462 <https://github.com/dropwizard/dropwizard/pull/2462>`_)
* Deprecate ``Bundle`` in favor of ``ConfiguredBundle<T>`` (`#2516 <https://github.com/dropwizard/dropwizard/pull/2516>`_)
* Allow unknown JSON properties (i.e. disable ``DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES``) by default (`#2570 <https://github.com/dropwizard/dropwizard/pull/2570>`_)
* Deprecate ``*Param`` classes and will be removed in 3.0.0 (`#2637 <https://github.com/dropwizard/dropwizard/pull/2637>`_)
* Add data size class adhering to the correct SI and IEC prefixes (`#2686 <https://github.com/dropwizard/dropwizard/pull/2686>`_)
* Added PortDescriptor class and method in ServerLifeCycleListener to provide a list of PortDescriptors, detailing all listening information for the application (`#2711 <https://github.com/dropwizard/dropwizard/pull/2711>`_)
* Add support for proxy-protocol in http connector configuration (`#2709 <https://github.com/dropwizard/dropwizard/pull/2709>`_)
* Disable using ``X-Forwarded-*`` headers by default (`#2748 <https://github.com/dropwizard/dropwizard/pull/2748>`_)
* Fix typo by renaming ``ResilentSocketOutputStream`` to ``ResilientSocketOutputStream`` (`#2766 <https://github.com/dropwizard/dropwizard/pull/2766>`_)
* Adds an opt-in URI request logging filter factory (`UriFilterFactory`)  (`#2794 <https://github.com/dropwizard/dropwizard/pull/2795>`_)`
* Add support for configuring Jetty's cookie compliance (`#2812 <https://github.com/dropwizard/dropwizard/pull/2812>`_)
* Deprecate ``Authorizer.authorize(principal, role)`` in favor of ``Authorizer.authorize(principal, role, context)`` (`#2837 <https://github.com/dropwizard/dropwizard/pull/2837>`_)
* Fix undefined config environment variables with a default value causing an exception in strict mode (`#2801 <https://github.com/dropwizard/dropwizard/pull/2801>`_)
* Removed ``dropwizard-jdbi`` as official module and moved it into it's own project: `dropwizard-jdbi <https://github.com/dropwizard/dropwizard-jdbi>`_ (`#2922 <https://github.com/dropwizard/dropwizard/issues/2922>`_)
* Add ``@JsonProperty`` to AbstractServerFactory setters
* Add InjectValidatorBundle that enable context injection into validators
* Add JUnit 5 Example to Testing Clients (`#2367 <https://github.com/dropwizard/dropwizard/issues/2367>`_)
* Add TLS socket logging appender
* Add a ``JSONUnauthorizedHandler`` (`#2839 <https://github.com/dropwizard/dropwizard/issues/2839>`_)
* Add config settings for tasks and health check resources (`#3037 <https://github.com/dropwizard/dropwizard/issues/3037>`_)
* Add current class loader to javassist ClassPool
* Add mapping for Jetty alpn-boot to Java versions (`#2948 <https://github.com/dropwizard/dropwizard/issues/2948>`_)
* Add missing "to" in BaseConfigurationFactory exception messages (`#2869 <https://github.com/dropwizard/dropwizard/issues/2869>`_)
* Add new constructors to allow specifying a response content type for Task and PostBodyTask, keeping the default as text/plain;charset=UTF-8
* Add pip requirements file with Sphinx and dependencies
* Add possibility to disable logging bootstrap for ResourceTestRule (`#2338 <https://github.com/dropwizard/dropwizard/issues/2338>`_)
* Add safe Jackson deserializers to prevent a DoS attack (`#2511 <https://github.com/dropwizard/dropwizard/issues/2511>`_)
* Add support for PATCH request to ResourceTestRule client (`#2410 <https://github.com/dropwizard/dropwizard/issues/2410>`_)
* Add support for SLF4J markers to dropwizard-json-logging (`#2899 <https://github.com/dropwizard/dropwizard/issues/2899>`_)
* Add support for disabled metric attributes on ConsoleReporterFa… (`#2976 <https://github.com/dropwizard/dropwizard/issues/2976>`_)
* Add support for logging caller data in dropwizard-json-logging
* Add support for pathQuery json access log attribute
* Added support for independently client-specified JCE Providers for both keystore and truststore (`#2390 <https://github.com/dropwizard/dropwizard/issues/2390>`_)
* Addressed ThrottlingLoggingAppenderTest issues
* Adds a request logging url filter. Fixes `#2794 <https://github.com/dropwizard/dropwizard/issues/2794>`_
* Allow full customization of HttpClientBuilder (`#2864 <https://github.com/dropwizard/dropwizard/issues/2864>`_)
* Allow overriding ``ViewMessageBodyWriter#detectLocale()`` (`#2967 <https://github.com/dropwizard/dropwizard/issues/2967>`_)
* Allow reporting Metrics on stop (`#2558 <https://github.com/dropwizard/dropwizard/issues/2558>`_)
* Allow simple logger level config to support "OFF" (`#2819 <https://github.com/dropwizard/dropwizard/issues/2819>`_)
* Allow to disable logging bootstrap in DAOTest
* Allow to setNormalizeUri on HTTP client
* Appropriately log ssl params
* Avoid error message while signing artifacts
* Be more precise about use of Metered and Timed annotations
* Catch EofException at the jetty handler level
* Checkout all freemarker templates with lf line endings
* Compiler Warning Cleanup (`#2466 <https://github.com/dropwizard/dropwizard/issues/2466>`_)
* ConnectorProvider Not Set Silently
* Convert to lazy evaluation for json event creation (`#2506 <https://github.com/dropwizard/dropwizard/issues/2506>`_)
* Correctly log resource paths with relative path segments (`#2923 <https://github.com/dropwizard/dropwizard/issues/2923>`_)
* Default values allowed on strict undefined config env vars
* Dependency reorganization (`#2897 <https://github.com/dropwizard/dropwizard/issues/2897>`_)
* Deprecate ``*Param`` classes
* Disable Errorprone: EqualsGetClass check (`#2718 <https://github.com/dropwizard/dropwizard/issues/2718>`_)
* Disable ``FAIL_ON_UNKNOWN_PROPERTIES`` by default
* Document TeeFilter for JSON log format (`#2596 <https://github.com/dropwizard/dropwizard/issues/2596>`_)
* DropwizardTestSupport sets ConfigurationFactoryFactory too early (`#2551 <https://github.com/dropwizard/dropwizard/issues/2551>`_)
* Enable Jackson Afterburner only on Java 8 (`#2966 <https://github.com/dropwizard/dropwizard/issues/2966>`_)
* Ensure ``DropwizardResourceConfig#forTesting()`` is using a random port
* Exclude javax.el and jakarta.el-api, using glassfish jakarta.el instead (`#2750 <https://github.com/dropwizard/dropwizard/issues/2750>`_)
* Explicitly create BootstrapServiceRegistry in SessionFactoryFac… (`#2977 <https://github.com/dropwizard/dropwizard/issues/2977>`_)
* Extend from AbstractHandlerContainer instead of AbstractHandler (`#2460 <https://github.com/dropwizard/dropwizard/issues/2460>`_)
* Fix Incomplete TaskServletTest Method Stubbing To Avoid NullpointerException In Tests (`#3032 <https://github.com/dropwizard/dropwizard/issues/3032>`_)
* Fix Integration Testing Example (`#2364 <https://github.com/dropwizard/dropwizard/issues/2364>`_)
* Fix Jackson (fuzzy) enum handling (`#2599 <https://github.com/dropwizard/dropwizard/issues/2599>`_)
* Fix date formatting pattern in test (`#2585 <https://github.com/dropwizard/dropwizard/issues/2585>`_)
* Fix deprecation usage of argparse4j
* Fix errorpone warnings (`#2399 <https://github.com/dropwizard/dropwizard/issues/2399>`_)
* Fix escape signs and broken @see section (`#2331 <https://github.com/dropwizard/dropwizard/issues/2331>`_)
* Fix for InvalidKeyException: Illegal key size (`#2411 <https://github.com/dropwizard/dropwizard/issues/2411>`_, `#2408 <https://github.com/dropwizard/dropwizard/issues/2408>`_)
* Fix illegal reflection warning in DropwizardResourceConfig (`#2964 <https://github.com/dropwizard/dropwizard/issues/2964>`_)
* Fix incorrect reading of somaxconn for tcp backlog on linux (`#2430 <https://github.com/dropwizard/dropwizard/issues/2430>`_)
* Include default requestLog format string in documentation (`#2500 <https://github.com/dropwizard/dropwizard/issues/2500>`_, `#2526 <https://github.com/dropwizard/dropwizard/issues/2526>`_)
* Fix jersey attempting to resolve auth filter fields
* Fix shared metrics race with multiple environments
* Fix tests: Disable FAIL_ON_UNKNOWN_PROPERTIES
* Fixed flaky test in CachingAuthorizer (`#2683 <https://github.com/dropwizard/dropwizard/issues/2683>`_)
* Improve Dropwizard test support (`#2673 <https://github.com/dropwizard/dropwizard/issues/2673>`_)
* Improve validation message for min/max duration
* Include all Apache Tomcat JDBC ConnectionPool metrics (`#2475 <https://github.com/dropwizard/dropwizard/issues/2475>`_)
* Increases the values in the hibernate validator annotations to actual minimums
* Let async logs finish in throttling append test
* Make Duration, DataSize, and Size serializable (`#2975 <https://github.com/dropwizard/dropwizard/issues/2975>`_)
* Mark PermissiveEnumDeserializer as cacheable (`#2446 <https://github.com/dropwizard/dropwizard/issues/2446>`_)
* Merge pull request `#2316 <https://github.com/dropwizard/dropwizard/issues/2316>`_ from dropwizard/move-to-junit5
* Merge pull request `#2320 <https://github.com/dropwizard/dropwizard/issues/2320>`_ from nickbabcock/remove-prereq-
* Merge pull request `#2324 <https://github.com/dropwizard/dropwizard/issues/2324>`_ from nickbabcock/jersey-resolv
* Merge pull request `#2325 <https://github.com/dropwizard/dropwizard/issues/2325>`_ from xiaodong-xie/upgrade-liquibase
* Merge pull request `#2339 <https://github.com/dropwizard/dropwizard/issues/2339>`_ from nickbabcock/argparse4j
* Merge pull request `#2341 <https://github.com/dropwizard/dropwizard/issues/2341>`_ from nickbabcock/freemarker-attributes
* Merge pull request `#2342 <https://github.com/dropwizard/dropwizard/issues/2342>`_ from nickbabcock/env-metric-race
* Merge pull request `#2344 <https://github.com/dropwizard/dropwizard/issues/2344>`_ from manuel-hegner/feature/improve_self_validation
* Merge pull request `#2349 <https://github.com/dropwizard/dropwizard/issues/2349>`_ from nickbabcock/fix-javadoc-errors
* Merge pull request `#2404 <https://github.com/dropwizard/dropwizard/issues/2404>`_ from nickbabcock/cleanup-params-test
* Merge pull request `#2405 <https://github.com/dropwizard/dropwizard/issues/2405>`_ from nickbabcock/log-ssl
* Merge pull request `#2409 <https://github.com/dropwizard/dropwizard/issues/2409>`_ from nickbabcock/inclusive
* Merge pull request `#2414 <https://github.com/dropwizard/dropwizard/issues/2414>`_ from tsundberg/timed-and-meterd-cannot-be-used-at-the-same-time
* Merge pull request `#2448 <https://github.com/dropwizard/dropwizard/issues/2448>`_ from dropwizard/resource-config-random-port
* Merge pull request `#2487 <https://github.com/dropwizard/dropwizard/issues/2487>`_ from zmarois/patch-1
* Merge pull request `#2509 <https://github.com/dropwizard/dropwizard/issues/2509>`_ from mattnelson/json_uri_query
* Merge pull request `#2514 <https://github.com/dropwizard/dropwizard/issues/2514>`_ from bennyz/redundant-the
* Merge pull request `#2519 <https://github.com/dropwizard/dropwizard/issues/2519>`_ from dropwizard/dependency-updates
* Merge pull request `#2522 <https://github.com/dropwizard/dropwizard/issues/2522>`_ from alex-shpak/feature/inject-validator-2
* Merge pull request `#2541 <https://github.com/dropwizard/dropwizard/issues/2541>`_ from shail/eofExceptionIssue
* Merge pull request `#2549 <https://github.com/dropwizard/dropwizard/issues/2549>`_ from minisu/patch-3
* Merge pull request `#2573 <https://github.com/dropwizard/dropwizard/issues/2573>`_ from isaki/throttle_revisit
* Merge pull request `#2575 <https://github.com/dropwizard/dropwizard/issues/2575>`_ from isaki/cache_auth_test_fix
* Merge pull request `#2576 <https://github.com/dropwizard/dropwizard/issues/2576>`_ from sergioescala/removing_unnecessary_import
* Merge pull request `#2578 <https://github.com/dropwizard/dropwizard/issues/2578>`_ from nickbabcock/cve-suppress
* Merge pull request `#2600 <https://github.com/dropwizard/dropwizard/issues/2600>`_ from dropwizard/issue-2539
* Merge pull request `#2643 <https://github.com/dropwizard/dropwizard/issues/2643>`_ from nickbabcock/before-after
* Merge pull request `#2659 <https://github.com/dropwizard/dropwizard/issues/2659>`_ from dropwizard/errorprone-nullaway
* Merge pull request `#2665 <https://github.com/dropwizard/dropwizard/issues/2665>`_ from nickbabcock/sona-example
* Merge pull request `#2675 <https://github.com/dropwizard/dropwizard/issues/2675>`_ from dennyac/dropwizard-jersey-metrics-documentation
* Merge pull request `#2684 <https://github.com/dropwizard/dropwizard/issues/2684>`_ from nickbabcock/logging-docs
* Merge pull request `#2692 <https://github.com/dropwizard/dropwizard/issues/2692>`_ from FredDeschenes/2.0-release-notes-abstractbinder
* Merge pull request `#2693 <https://github.com/dropwizard/dropwizard/issues/2693>`_ from dropwizard/remove-checkstyle
* Merge pull request `#2703 <https://github.com/dropwizard/dropwizard/issues/2703>`_ from slivkamiro/feature/validation-query
* Merge pull request `#2722 <https://github.com/dropwizard/dropwizard/issues/2722>`_ from dropwizard/issue-2721
* Merge pull request `#2741 <https://github.com/dropwizard/dropwizard/issues/2741>`_ from davnicwil/specify-task-response-type
* Merge pull request `#2760 <https://github.com/dropwizard/dropwizard/issues/2760>`_ from dropwizard/issue-2759
* Merge pull request `#2764 <https://github.com/dropwizard/dropwizard/issues/2764>`_ from tristanbuckner/reset_closed_client
* Merge pull request `#2767 <https://github.com/dropwizard/dropwizard/issues/2767>`_ from nickbabcock/test-bind
* Merge pull request `#2775 <https://github.com/dropwizard/dropwizard/issues/2775>`_ from nickbabcock/remove-doc
* Merge pull request `#2786 <https://github.com/dropwizard/dropwizard/issues/2786>`_ from josephlbarnett/javassist-classpath
* Merge pull request `#2803 <https://github.com/dropwizard/dropwizard/issues/2803>`_ from koraytugay/patch-1
* Merge pull request `#2804 <https://github.com/dropwizard/dropwizard/issues/2804>`_ from stevenbenitez/fix/caching-authenticator-doc
* Merge pull request `#2805 <https://github.com/dropwizard/dropwizard/issues/2805>`_ from mzamani1/fix-conscrypt-docs
* Merge pull request `#2811 <https://github.com/dropwizard/dropwizard/issues/2811>`_ from cyberdelia/normalize-uri
* Merge pull request `#2854 <https://github.com/dropwizard/dropwizard/issues/2854>`_ from toadzky/fix-hibernate-validator-values-on-server-factory
* Merge pull request `#2874 <https://github.com/dropwizard/dropwizard/issues/2874>`_ from jamesalfei/master
* Merge pull request `#2883 <https://github.com/dropwizard/dropwizard/issues/2883>`_ from dropwizard/dependency-cleanup
* Merge pull request `#2919 <https://github.com/dropwizard/dropwizard/issues/2919>`_ from alexey-wg2/remove-duplicated-service-entry
* Merge pull request `#2940 <https://github.com/dropwizard/dropwizard/issues/2940>`_ from msymons/master
* Merge pull request `#2943 <https://github.com/dropwizard/dropwizard/issues/2943>`_ from gisripa/requestAttrs_json_logging
* Merge pull request `#3021 <https://github.com/dropwizard/dropwizard/issues/3021>`_ from cjhawley/patch-1
* Migrate jetty min data rates to Sizes
* Migrate tests to JUnit 5.4.0 (`#2493 <https://github.com/dropwizard/dropwizard/issues/2493>`_)
* Migrate to jetty-only gzip handler (`#2566 <https://github.com/dropwizard/dropwizard/issues/2566>`_)
* Move ResilientSocketOutputStream into io.dropwizard.logging (`#2925 <https://github.com/dropwizard/dropwizard/issues/2925>`_)
* Nested calls to ``@UnitOfWork`` methods cause inconsistent behaviour (`#2913 <https://github.com/dropwizard/dropwizard/issues/2913>`_)
* Only override ConfigurationSourceProvider if explicitly provided (`#2720 <https://github.com/dropwizard/dropwizard/issues/2720>`_)
* Overhaul logging resource endpoints
* Refactor inject validator bundle to use resourceContext directly
* Register HK2 AbstractBinder with Jersey (`#3000 <https://github.com/dropwizard/dropwizard/issues/3000>`_)
* Remove Guava (`#2400 <https://github.com/dropwizard/dropwizard/issues/2400>`_, `#2555 <https://github.com/dropwizard/dropwizard/issues/2555>`_)
* Remove metrics-ganglia completely (`#2310 <https://github.com/dropwizard/dropwizard/issues/2310>`_)
* Remove restrictions on generic type for ConfiguredBundle
* Replace InjectValidatorBundle with feature and register by default
* Replace JSON string asserts in MultipleContentTypeTest (`#3056 <https://github.com/dropwizard/dropwizard/issues/3056>`_)
* Replace ThrottlingAppenderWrapper with external version
* Replace livereload and Guard with sphinx-autobuild
* Replace remaining use of Hamcrest with AssertJ (`#2444 <https://github.com/dropwizard/dropwizard/issues/2444>`_)
* Request Uri event should not contain params in tests (`#2504 <https://github.com/dropwizard/dropwizard/issues/2504>`_)
* Return 404 for POST /admin/tasks (`#2627 <https://github.com/dropwizard/dropwizard/issues/2627>`_)
* Rework resource config test for resilient CI
* Rewrite of throttling logging appender testing (`#2458 <https://github.com/dropwizard/dropwizard/issues/2458>`_)
* Satisfy optional check before unwrap analyses (`#2644 <https://github.com/dropwizard/dropwizard/issues/2644>`_)
* Simplify SelfValidatingValidator (`#2413 <https://github.com/dropwizard/dropwizard/issues/2413>`_)
* Support URL encoded entry names in ``ResourceURL#isDirectory()`` (`#2674 <https://github.com/dropwizard/dropwizard/issues/2674>`_)
* Support configuration of exception details with JSON logging (`#2501 <https://github.com/dropwizard/dropwizard/issues/2501>`_)
* Support custom request executor in HttpClientBuilder (`#2959 <https://github.com/dropwizard/dropwizard/issues/2959>`_)
* Support dumping Jetty config on start/stop (`#2743 <https://github.com/dropwizard/dropwizard/issues/2743>`_)
* Support for requestAttributes in Json access log
* Support handling failed commands via ``Application#onFatalError(…`` (`#3020 <https://github.com/dropwizard/dropwizard/issues/3020>`_)
* Support nested JUnit 5 tests with ``DropwizardExtension`` (`#2924 <https://github.com/dropwizard/dropwizard/issues/2924>`_)
* Surround bootclasspath in quotes for special characters in user home
* Test deserializing config without JsonAutoDetect
* Test support cleanup on before exceptions
* UUID param to length check input
* Use AtomicReference in LogConfigurationTask for timer
* Use Dropwizard's CharStreams class in DefaultServerFactoryTest
* Use Java Stream API in DbDumpCommandTest (`#2326 <https://github.com/dropwizard/dropwizard/issues/2326>`_)
* Use commons-text native undef var detection (`#2829 <https://github.com/dropwizard/dropwizard/issues/2829>`_)
* Use correct property for Dropwizard versions in dropwizard-bom
* Use custom public and secret keyrings when signing
* Use instrumented thread factory (`#2649 <https://github.com/dropwizard/dropwizard/issues/2649>`_)
* Use strict illegal-access policy on Java 9 and later (`#2965 <https://github.com/dropwizard/dropwizard/issues/2965>`_)
* Allowing validation query to be null `#2702 <https://github.com/dropwizard/dropwizard/issues/2702>`_
* make it possible to created subclass of apache http builder (`#2958 <https://github.com/dropwizard/dropwizard/issues/2958>`_)
* Update JdbiFactory to use metrics' InstrumentedSqlLogger (`#2682 <https://github.com/dropwizard/dropwizard/issues/2682>`_)

Version updates
---------------

* Bump bcprov-jdk15on to 1.64 (`#2642 <https://github.com/dropwizard/dropwizard/issues/2642>`_, `#2791 <https://github.com/dropwizard/dropwizard/issues/2791>`_, `#2917 <https://github.com/dropwizard/dropwizard/issues/2917>`_, `#2972 <https://github.com/dropwizard/dropwizard/issues/2972>`_)
* Bump byte-buddy to 1.10.4 (`#2611 <https://github.com/dropwizard/dropwizard/issues/2611>`_, `#2631 <https://github.com/dropwizard/dropwizard/issues/2631>`_, `#2707 <https://github.com/dropwizard/dropwizard/issues/2707>`_, `#2710 <https://github.com/dropwizard/dropwizard/issues/2710>`_, `#2782 <https://github.com/dropwizard/dropwizard/issues/2782>`_, `#2835 <https://github.com/dropwizard/dropwizard/issues/2835>`_, `#2849 <https://github.com/dropwizard/dropwizard/issues/2849>`_, `#2860 <https://github.com/dropwizard/dropwizard/issues/2860>`_, `#2876 <https://github.com/dropwizard/dropwizard/issues/2876>`_, `#2984 <https://github.com/dropwizard/dropwizard/issues/2984>`_, `#3018 <https://github.com/dropwizard/dropwizard/issues/3018>`_, `#3041 <https://github.com/dropwizard/dropwizard/issues/3041>`_)
* Bump caffeine to 2.8.0 (`#2661 <https://github.com/dropwizard/dropwizard/issues/2661>`_, `#2868 <https://github.com/dropwizard/dropwizard/issues/2868>`_)
* Bump checker-qual to 3.0.0 (`#2676 <https://github.com/dropwizard/dropwizard/issues/2676>`_, `#2728 <https://github.com/dropwizard/dropwizard/issues/2728>`_, `#2756 <https://github.com/dropwizard/dropwizard/issues/2756>`_, `#2790 <https://github.com/dropwizard/dropwizard/issues/2790>`_, `#2827 <https://github.com/dropwizard/dropwizard/issues/2827>`_, `#2865 <https://github.com/dropwizard/dropwizard/issues/2865>`_, `#2866 <https://github.com/dropwizard/dropwizard/issues/2866>`_, `#2894 <https://github.com/dropwizard/dropwizard/issues/2894>`_, `#2902 <https://github.com/dropwizard/dropwizard/issues/2902>`_, `#2955 <https://github.com/dropwizard/dropwizard/issues/2955>`_, `#3048 <https://github.com/dropwizard/dropwizard/issues/3048>`_, `#3012 <https://github.com/dropwizard/dropwizard/issues/3012>`_)
* Bump classmate to 1.5.1 (`#2708 <https://github.com/dropwizard/dropwizard/issues/2708>`_, `#2985 <https://github.com/dropwizard/dropwizard/issues/2985>`_)
* Bump commons-lang3 to 3.9 (`#2732 <https://github.com/dropwizard/dropwizard/issues/2732>`_)
* Bump commons-text to 1.8 (`#2828 <https://github.com/dropwizard/dropwizard/issues/2828>`_, `#2905 <https://github.com/dropwizard/dropwizard/issues/2905>`_)
* Bump Mustache compiler to 0.9.6 (`#2616 <https://github.com/dropwizard/dropwizard/issues/2616>`_)
* Bump Errorprone to 2.3.4 (`#3046 <https://github.com/dropwizard/dropwizard/issues/3046>`_, `#3047 <https://github.com/dropwizard/dropwizard/issues/3047>`_)
* Bump Freemarker to 2.3.29 (`#2887 <https://github.com/dropwizard/dropwizard/issues/2887>`_)
* Bump Guava to 28.1-jre (`#2472 <https://github.com/dropwizard/dropwizard/issues/2472>`_, `#2688 <https://github.com/dropwizard/dropwizard/issues/2688>`_, `#2798 <https://github.com/dropwizard/dropwizard/issues/2798>`_, `#2900 <https://github.com/dropwizard/dropwizard/issues/2900>`_)
* Bump hibernate-core to 5.4.10.Final (`#2706 <https://github.com/dropwizard/dropwizard/issues/2706>`_, `#2785 <https://github.com/dropwizard/dropwizard/issues/2785>`_, `#2863 <https://github.com/dropwizard/dropwizard/issues/2863>`_, `#2952 <https://github.com/dropwizard/dropwizard/issues/2952>`_, `#2993 <https://github.com/dropwizard/dropwizard/issues/2993>`_, `#3007 <https://github.com/dropwizard/dropwizard/issues/3007>`_, `#3026 <https://github.com/dropwizard/dropwizard/issues/3026>`_, `#3052 <https://github.com/dropwizard/dropwizard/issues/3052>`_)
* Bump hibernate-validator to 6.1.0.Final (`#2629 <https://github.com/dropwizard/dropwizard/issues/2629>`_, `#2662 <https://github.com/dropwizard/dropwizard/issues/2662>`_, `#2705 <https://github.com/dropwizard/dropwizard/issues/2705>`_, `#2802 <https://github.com/dropwizard/dropwizard/issues/2802>`_, `#3003 <https://github.com/dropwizard/dropwizard/issues/3003>`_)
* Bump Apache HttpClient to 4.5.10 (`#2615 <https://github.com/dropwizard/dropwizard/issues/2615>`_, `#2715 <https://github.com/dropwizard/dropwizard/issues/2715>`_, `#2799 <https://github.com/dropwizard/dropwizard/issues/2799>`_, `#2914 <https://github.com/dropwizard/dropwizard/issues/2914>`_)
* Bump Jackson to 2.10.0 (`#2393 <https://github.com/dropwizard/dropwizard/issues/2393>`_, `#2777 <https://github.com/dropwizard/dropwizard/issues/2777>`_, `#2826 <https://github.com/dropwizard/dropwizard/issues/2826>`_, `#2870 <https://github.com/dropwizard/dropwizard/issues/2870>`_, `#3019 <https://github.com/dropwizard/dropwizard/issues/3019>`_, `#2944 <https://github.com/dropwizard/dropwizard/issues/2944>`_)
* Bump jakarta.el to 3.0.3 (`#2912 <https://github.com/dropwizard/dropwizard/issues/2912>`_)
* Bump javassist to 3.26.0-GA (`#2738 <https://github.com/dropwizard/dropwizard/issues/2738>`_, `#2961 <https://github.com/dropwizard/dropwizard/issues/2961>`_)
* Bump JAXB API to 2.3.1 (`#2608 <https://github.com/dropwizard/dropwizard/issues/2608>`_)
* Bump JDBI3 to 3.11.1 (`#2369 <https://github.com/dropwizard/dropwizard/issues/2369>`_, `#2451 <https://github.com/dropwizard/dropwizard/issues/2451>`_, `#2546 <https://github.com/dropwizard/dropwizard/issues/2546>`_, `#2731 <https://github.com/dropwizard/dropwizard/issues/2731>`_, `#2726 <https://github.com/dropwizard/dropwizard/issues/2726>`_, `#2744 <https://github.com/dropwizard/dropwizard/issues/2744>`_, `#2754 <https://github.com/dropwizard/dropwizard/issues/2754>`_, `#2762 <https://github.com/dropwizard/dropwizard/issues/2762>`_, `#2855 <https://github.com/dropwizard/dropwizard/issues/2855>`_, `#2872 <https://github.com/dropwizard/dropwizard/issues/2872>`_, `#2907 <https://github.com/dropwizard/dropwizard/issues/2907>`_, `#2929 <https://github.com/dropwizard/dropwizard/issues/2929>`_, `#3027 <https://github.com/dropwizard/dropwizard/issues/3027>`_, `#3030 <https://github.com/dropwizard/dropwizard/issues/3030>`_)
* Bump Jersey to 2.29.1 (`#2395 <https://github.com/dropwizard/dropwizard/issues/2395>`_, `#2613 <https://github.com/dropwizard/dropwizard/issues/2613>`_, `#2813 <https://github.com/dropwizard/dropwizard/issues/2813>`_, `#2916 <https://github.com/dropwizard/dropwizard/issues/2916>`_)
* Bump Jetty to 9.4.24.v20191120 (`#2346 <https://github.com/dropwizard/dropwizard/issues/2346>`_, `#2657 <https://github.com/dropwizard/dropwizard/issues/2657>`_, `#2734 <https://github.com/dropwizard/dropwizard/issues/2734>`_, `#2740 <https://github.com/dropwizard/dropwizard/issues/2740>`_, `#2752 <https://github.com/dropwizard/dropwizard/issues/2752>`_, `#2800 <https://github.com/dropwizard/dropwizard/issues/2800>`_, `#2879 <https://github.com/dropwizard/dropwizard/issues/2879>`_, `#2956 <https://github.com/dropwizard/dropwizard/issues/2956>`_, `#2997 <https://github.com/dropwizard/dropwizard/issues/2997>`_, `#3031 <https://github.com/dropwizard/dropwizard/issues/3031>`_, `#3033 <https://github.com/dropwizard/dropwizard/issues/3033>`_)
* Bump alpn-boot to v8.1.13.v20181017 (`#2547 <https://github.com/dropwizard/dropwizard/issues/2547>`_, `#2340 <https://github.com/dropwizard/dropwizard/issues/2340>`_)
* Bump Joda-Time to 2.10.5 (`#2772 <https://github.com/dropwizard/dropwizard/issues/2772>`_, `#2831 <https://github.com/dropwizard/dropwizard/issues/2831>`_, `#2937 <https://github.com/dropwizard/dropwizard/issues/2937>`_, `#2998 <https://github.com/dropwizard/dropwizard/issues/2998>`_)
* Bump Liquibase to 3.8.2 (`#2386 <https://github.com/dropwizard/dropwizard/issues/2386>`_, `#2621 <https://github.com/dropwizard/dropwizard/issues/2621>`_, `#2845 <https://github.com/dropwizard/dropwizard/issues/2845>`_, `#2890 <https://github.com/dropwizard/dropwizard/issues/2890>`_, `#3016 <https://github.com/dropwizard/dropwizard/issues/3016>`_, `#3038 <https://github.com/dropwizard/dropwizard/issues/3038>`_)
* Bump logback-throttling-appender to 1.1.0 (`#2928 <https://github.com/dropwizard/dropwizard/issues/2928>`_)
* Bump Dropwizard Metrics to 4.1.2 (`#2761 <https://github.com/dropwizard/dropwizard/issues/2761>`_, `#2986 <https://github.com/dropwizard/dropwizard/issues/2986>`_, `#3055 <https://github.com/dropwizard/dropwizard/issues/3055>`_)
* Bump Objenesis to 3.1 (`#2968 <https://github.com/dropwizard/dropwizard/issues/2968>`_)
* Bump SLF4J to 1.7.29 (`#2652 <https://github.com/dropwizard/dropwizard/issues/2652>`_, `#2873 <https://github.com/dropwizard/dropwizard/issues/2873>`_, `#2877 <https://github.com/dropwizard/dropwizard/issues/2877>`_, `#3009 <https://github.com/dropwizard/dropwizard/issues/3009>`_)
* Bump tomcat-jdbc to 9.0.29 (`#2636 <https://github.com/dropwizard/dropwizard/issues/2636>`_, `#2700 <https://github.com/dropwizard/dropwizard/issues/2700>`_, `#2733 <https://github.com/dropwizard/dropwizard/issues/2733>`_, `#2776 <https://github.com/dropwizard/dropwizard/issues/2776>`_, `#2793 <https://github.com/dropwizard/dropwizard/issues/2793>`_, `#2838 <https://github.com/dropwizard/dropwizard/issues/2838>`_, `#2885 <https://github.com/dropwizard/dropwizard/issues/2885>`_, `#2979 <https://github.com/dropwizard/dropwizard/issues/2979>`_, `#2935 <https://github.com/dropwizard/dropwizard/issues/2935>`_, `#3034 <https://github.com/dropwizard/dropwizard/issues/3034>`_)
* Upgrade dependencies (`#2445 <https://github.com/dropwizard/dropwizard/issues/2445>`_, `#2473 <https://github.com/dropwizard/dropwizard/issues/2473>`_, `#2537 <https://github.com/dropwizard/dropwizard/issues/2537>`_, `#2565 <https://github.com/dropwizard/dropwizard/issues/2565>`_)

* Bump JUnit 5 to 5.5.2 (`#2347 <https://github.com/dropwizard/dropwizard/issues/2347>`_, `#2604 <https://github.com/dropwizard/dropwizard/issues/2604>`_, `#2635 <https://github.com/dropwizard/dropwizard/issues/2635>`_, `#2651 <https://github.com/dropwizard/dropwizard/issues/2651>`_, `#2697 <https://github.com/dropwizard/dropwizard/issues/2697>`_, `#2698 <https://github.com/dropwizard/dropwizard/issues/2698>`_, `#2724 <https://github.com/dropwizard/dropwizard/issues/2724>`_, `#2727 <https://github.com/dropwizard/dropwizard/issues/2727>`_, `#2822 <https://github.com/dropwizard/dropwizard/issues/2822>`_, `#2842 <https://github.com/dropwizard/dropwizard/issues/2842>`_, `#2848 <https://github.com/dropwizard/dropwizard/issues/2848>`_, `#2850 <https://github.com/dropwizard/dropwizard/issues/2850>`_, `#2910 <https://github.com/dropwizard/dropwizard/issues/2910>`_, `#2911 <https://github.com/dropwizard/dropwizard/issues/2911>`_)
* Bump Mockito to 3.2.0 (`#2630 <https://github.com/dropwizard/dropwizard/issues/2630>`_, `#2654 <https://github.com/dropwizard/dropwizard/issues/2654>`_, `#2680 <https://github.com/dropwizard/dropwizard/issues/2680>`_, `#2695 <https://github.com/dropwizard/dropwizard/issues/2695>`_, `#2725 <https://github.com/dropwizard/dropwizard/issues/2725>`_, `#2730 <https://github.com/dropwizard/dropwizard/issues/2730>`_, `#2784 <https://github.com/dropwizard/dropwizard/issues/2784>`_, `#2834 <https://github.com/dropwizard/dropwizard/issues/2834>`_, `#2957 <https://github.com/dropwizard/dropwizard/issues/2957>`_, `#3044 <https://github.com/dropwizard/dropwizard/issues/3044>`_)
* Bump assertj-core to 3.14.0 (`#2648 <https://github.com/dropwizard/dropwizard/issues/2648>`_, `#2666 <https://github.com/dropwizard/dropwizard/issues/2666>`_, `#2696 <https://github.com/dropwizard/dropwizard/issues/2696>`_, `#2861 <https://github.com/dropwizard/dropwizard/issues/2861>`_, `#2862 <https://github.com/dropwizard/dropwizard/issues/2862>`_, `#2867 <https://github.com/dropwizard/dropwizard/issues/2867>`_, `#3004 <https://github.com/dropwizard/dropwizard/issues/3004>`_)
* Bump H2 to 1.4.200 (`#2660 <https://github.com/dropwizard/dropwizard/issues/2660>`_, `#2694 <https://github.com/dropwizard/dropwizard/issues/2694>`_, `#2983 <https://github.com/dropwizard/dropwizard/issues/2983>`_)
* Bump hsqldb to 2.5.0 (`#2788 <https://github.com/dropwizard/dropwizard/issues/2788>`_)

* Bump Octokit to 4.14.0 (`#2607 <https://github.com/dropwizard/dropwizard/issues/2607>`_, `#2716 <https://github.com/dropwizard/dropwizard/issues/2716>`_)
* Bump Sphinx to 2.2.2 (`#2328 <https://github.com/dropwizard/dropwizard/issues/2328>`_, `#2606 <https://github.com/dropwizard/dropwizard/issues/2606>`_, `#2632 <https://github.com/dropwizard/dropwizard/issues/2632>`_, `#2689 <https://github.com/dropwizard/dropwizard/issues/2689>`_, `#2712 <https://github.com/dropwizard/dropwizard/issues/2712>`_, `#2729 <https://github.com/dropwizard/dropwizard/issues/2729>`_, `#2789 <https://github.com/dropwizard/dropwizard/issues/2789>`_, `#2796 <https://github.com/dropwizard/dropwizard/issues/2796>`_, `#2810 <https://github.com/dropwizard/dropwizard/issues/2810>`_, `#2886 <https://github.com/dropwizard/dropwizard/issues/2886>`_, `#3002 <https://github.com/dropwizard/dropwizard/issues/3002>`_, `#3049 <https://github.com/dropwizard/dropwizard/issues/3049>`_)


.. _rel-1.3.16:

v1.3.16: Oct 20, 2019
=====================

* Upgrade to Jackson 2.9.10.20191020 to address CVE-2019-16942, CVE-2019-16943, and CVE-2019-17531 (`#2988 <https://github.com/dropwizard/dropwizard/pull/2988>`_)


.. _rel-1.3.15:

v1.3.15: Sep 25, 2019
=====================

* Upgrade to Jackson 2.9.10 to address multiple security issues (`#2939 <https://github.com/dropwizard/dropwizard/pull/2939>`_)


.. _rel-1.3.14:

v1.3.14: Aug 7, 2019
====================

* Upgrade to Jackson 2.9.9.20190807 to address multiple security issues (`#2871 <https://github.com/dropwizard/dropwizard/pull/2871>`_)


.. _rel-1.3.13:

v1.3.13: July 16, 2019
======================

* Upgrade to Jackson Databind 2.9.9.1 to address `CVE-2019-12086 <https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2019-12086>`_ (`#2825 <https://github.com/dropwizard/dropwizard/pull/2825>`_)
* Add a ``JSONUnauthorizedHandler`` (`#2841 <https://github.com/dropwizard/dropwizard/pull/2841>`_)


.. _rel-1.3.12:

v1.3.12: May 25, 2019
=====================

* Upgrade to Jackson 2.9.9 to address `CVE-2019-12086 <https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2019-12086>`_ (`#2779 <https://github.com/dropwizard/dropwizard/pull/2779>`_)


.. _rel-1.3.11:

v1.3.11: May 9, 2019
====================

* Upgrade Jetty to 9.4.18.v20190429


.. _rel-1.3.10:

v1.3.10: Apr 29, 2019
=====================

* Upgrade Jetty to 9.4.17.v20190418
* Upgrade commons-lang3 to 3.8.1 to make BOM compatible with Java 11 (`#2679 <https://github.com/dropwizard/dropwizard/pull/2679>`_)


.. _rel-1.3.9:

v1.3.9: Feb 24, 2019
====================

* Fix NPE when requesting /admin/tasks (`#2626 <https://github.com/dropwizard/dropwizard/pull/2626>`_, `#2627 <https://github.com/dropwizard/dropwizard/pull/2627>`_)
* Remove prerequisites from archetype-generated POM (`#2320 <https://github.com/dropwizard/dropwizard/pull/2320>`_)
* Upgrade to Jackson 2.9.8, addressing various CVEs (`#2591 <https://github.com/dropwizard/dropwizard/pull/2591>`_)
* Upgrade JDBI3 to 3.5.1 (`#2593 <https://github.com/dropwizard/dropwizard/pull/2593>`_)
* Upgrade Dropwizard Metrics to 4.0.5 (`#2594 <https://github.com/dropwizard/dropwizard/pull/2594>`_)
* Upgrade Jetty to 9.4.14.v20181114 (`#2592 <https://github.com/dropwizard/dropwizard/pull/2592>`_)
* Update dependencies to latest patch versions (`#2628 <https://github.com/dropwizard/dropwizard/pull/2628>`_)
  * Joda-Time 2.10.1
  * Apache HttpClient 4.5.7
  * Apache Tomcat JDBC Pool: 9.0.14
  * Hibernate ORM 5.2.18.Final
  * Liquibase 3.6.3
  * Freemarker 2.3.28
  * Mustache 0.9.6
  * Javassist 3.24.1-GA
  * Classmate 1.4.0
  * HSQLDB 2.4.1
  * Mockito 2.24.0
* Upgrade to SLF4J 1.7.26 (`CVE-2018-8088 <https://nvd.nist.gov/vuln/detail/CVE-2018-8088>`_)
* Upgrade to Tomcat JDBC Connection Pool 9.0.16
* Upgrade to Hibernate Validator 5.4.3.Final


.. _rel-1.3.8:

v1.3.8: Jan 2, 2019
===================

* Fix CVE-2018-10237 by upgrading Guava to 24.1.1 (`#2587 <https://github.com/dropwizard/dropwizard/pull/2587>`_)


.. _rel-1.3.7:

v1.3.7: Oct 2, 2018
===================

* Fix incorrect reading of ``somaxconn`` for TCP backlog on Linux (`#2430 <https://github.com/dropwizard/dropwizard/pull/2430>`_)

.. _rel-1.3.6:

v1.3.6: Oct 1, 2018
===================

* Fix a DoS attack vulnerability in Jackson: `FasterXML/jackson-databind#2141 <https://github.com/FasterXML/jackson-databind/issues/2141>`_ (`#2511 <https://github.com/dropwizard/dropwizard/pull/2512>`_)

.. _rel-1.3.5:

v1.3.5: Jun 25, 2018
====================

* Upgrade to Jetty 9.4.11.v20180605 to address `various security issues <http://dev.eclipse.org/mhonarc/lists/jetty-announce/msg00123.html>`__

.. _rel-1.2.8:

v1.2.8: Jun 25, 2018
====================

* Upgrade to Jetty 9.4.11.v20180605 to address `various security issues <http://dev.eclipse.org/mhonarc/lists/jetty-announce/msg00123.html>`__

.. _rel-1.1.8:

v1.1.8: Jun 25, 2018
====================

* Upgrade to Jetty 9.4.11.v20180605 to address `various security issues <http://dev.eclipse.org/mhonarc/lists/jetty-announce/msg00123.html>`__

.. _rel-1.3.4:

v1.3.4: Jun 14, 2018
====================

* Upgrade to Jackson 2.9.6 to fix CVE-2018-12022 and CVE-2018-12023 (`#2392 <https://github.com/dropwizard/dropwizard/issues/2392>`_, `#2393 <https://github.com/dropwizard/dropwizard/pull/2393>`_)
* Upgrade to Liquibase 3.6.1 (`#2385 <https://github.com/dropwizard/dropwizard/issues/2385>`_, `#2386 <https://github.com/dropwizard/dropwizard/pull/2386>`_)

.. _rel-1.2.7:

v1.2.7: Jun 14, 2018
====================

* Upgrade to Jackson 2.9.6 to fix CVE-2018-12022 and CVE-2018-12023 (`#2392 <https://github.com/dropwizard/dropwizard/issues/2392>`_, `#2393 <https://github.com/dropwizard/dropwizard/pull/2393>`_)

.. _rel-1.3.3:

v1.3.3: Jun 6, 2018
===================

* Fix Jersey attempting to resolve auth filter fields `#2324 <https://github.com/dropwizard/dropwizard/pull/2324>`_
* Upgrade to JUnit5 5.2.0 `#2347 <https://github.com/dropwizard/dropwizard/pull/2347>`_
* Upgrade to Jdbi3 3.2.1 `#2369 <https://github.com/dropwizard/dropwizard/pull/2369>`_
* Upgrade Liquibase from 3.5.5 to 3.6.0 `#2325 <https://github.com/dropwizard/dropwizard/pull/2325>`_

.. _rel-1.3.2:

v1.3.2: May 11, 2018
====================

* Upgrade Jetty to 9.4.10.v20180503 `#2346 <https://github.com/dropwizard/dropwizard/pull/2346>`_
* Add possibility to disable logging bootstrap for ResourceTestRule `#2333 <https://github.com/dropwizard/dropwizard/pull/2333>`_

.. _rel-1.2.6:

v1.2.6: May 11, 2018
====================

* Upgrade Jetty to 9.4.10.v20180503 `#2346 <https://github.com/dropwizard/dropwizard/pull/2346>`_
* Add possibility to disable logging bootstrap for ResourceTestRule `#2333 <https://github.com/dropwizard/dropwizard/pull/2333>`_

.. _rel-1.3.1:

v1.3.1: Apr 4, 2018
===================

* Upgrade to Jackson 2.9.5 (`CVE-2018-7489 <https://nvd.nist.gov/vuln/detail/CVE-2018-7489>`_)

.. _rel-1.2.5:

v1.2.5: Apr 4, 2018
===================

* Upgrade to Jackson 2.9.5 (`CVE-2018-7489 <https://nvd.nist.gov/vuln/detail/CVE-2018-7489>`_)

.. _rel-1.3.0:

v1.3.0: Mar 14, 2018
====================

* Add "dropwizard-jdbi3" module `#2243 <https://github.com/dropwizard/dropwizard/pull/2243>`_, `#2247 <https://github.com/dropwizard/dropwizard/pull/2247>`_
* Add Dropwizard testing module for JUnit 5 `#2166 <https://github.com/dropwizard/dropwizard/pull/2166>`_
* Support for building and running Dropwizard on JDK9 `#2197 <https://github.com/dropwizard/dropwizard/pull/2197>`_
* Support for running Dropwizard with native SSL via Conscrypt `#2230 <https://github.com/dropwizard/dropwizard/pull/2230>`_
* Add support for JSON logs in Dropwizard `#2232 <https://github.com/dropwizard/dropwizard/pull/2232>`_
* Add a TCP and UDP log appenders to Dropwizard `#2291 <https://github.com/dropwizard/dropwizard/pull/2291>`_
* Add support for providing a custom logging layout during logging bootstrap `#2260 <https://github.com/dropwizard/dropwizard/pull/2260>`_
* Add context path to logged endpoints `#2254 <https://github.com/dropwizard/dropwizard/pull/2254>`_
* Support multiple extensions for views (breaking change) `#2213 <https://github.com/dropwizard/dropwizard/pull/2213>`_
* Enable auto escaping of strings in Freemarker templates `#2251 <https://github.com/dropwizard/dropwizard/pull/2251>`_
* Allow dynamic constraint validation messages `#2246 <https://github.com/dropwizard/dropwizard/pull/2246>`_
* Add the ``@SelfValidation`` annotation as a powerful alternative to ``@ValidationMethod`` `#2150 <https://github.com/dropwizard/dropwizard/pull/2150>`_
* Set a minimal duration for ``DatasourceFactory.maxWaitForConnection()`` `#2130 <https://github.com/dropwizard/dropwizard/pull/2130>`_
* Migrate deprecated classes from commons-lang to commons-text `#2208 <https://github.com/dropwizard/dropwizard/pull/2208>`_
* Support for setting the ``immediateFlush`` option for file logging `#2193 <https://github.com/dropwizard/dropwizard/pull/2193>`_
* Use ``InstrumentedQueuedThreadPool`` for admin endpoint `#2186 <https://github.com/dropwizard/dropwizard/pull/2186>`_
* Add support for configuring ``ServiceUnavailableRetryStrategy`` for HTTP clients `#2185 <https://github.com/dropwizard/dropwizard/pull/2185>`_
* Add possibility to configure Jetty's ``minRequestDataRate`` `#2184 <https://github.com/dropwizard/dropwizard/pull/2184>`_
* Add exclusive mode to ``@MinDuration`` and ``@MaxDuration`` annotations `#2167 <https://github.com/dropwizard/dropwizard/pull/2167>`_
* Strip the ``Content-Length`` header after decompressing HTTP requests `#2271 <https://github.com/dropwizard/dropwizard/pull/2271>`_
* Add support for providing a custom layout during logging bootstrap `#2260 <https://github.com/dropwizard/dropwizard/pull/2260>`_
* Add support for PATCH request to Jersey test client `#2288 <https://github.com/dropwizard/dropwizard/pull/2288>`_
* Add configuration option to ``EventJsonLayoutBaseFactory`` to flatten MDC `#2293 <https://github.com/dropwizard/dropwizard/pull/2293>`_
* Allow to use custom security provider in HTTP client `#2299 <https://github.com/dropwizard/dropwizard/pull/2299>`_
* Make ``ignoreExceptionOnPreLoad`` on ``PoolProperties`` configurable `#2300 <https://github.com/dropwizard/dropwizard/pull/2300>`_
* Allow lazy initialization of resources in ``ResourceTestRule`` `#2304 <https://github.com/dropwizard/dropwizard/pull/2304>`_
* Make sure Jersey test client uses Dropwizard's ``ObjectMapper`` `#2277 <https://github.com/dropwizard/dropwizard/pull/2277>`_
* Allow customizing Hibernate Configuration in ``DAOTest`` `#2301 <https://github.com/dropwizard/dropwizard/pull/2301>`_
* Upgrade to Apache Commons Lang3 3.7
* Upgrade to Apache Commons Text 1.2
* Upgrade to Apache HttpClient 4.5.5
* Upgrade to Apache Tomcat JDBC 9.0.5
* Upgrade to Argparse4j 0.8.1
* Upgrade to AssertJ 3.9.1
* Upgrade to Dropwizard Metrics 4.0.2
* Upgrade to Error Prone 2.2.0
* Upgrade to Guava 24.0-jre
* Upgrade to Hibernate 5.2.15.Final
* Upgrade to Jackson 2.9.4
* Upgrade Jadira to 7.0.0-rc1 `#2272 <https://github.com/dropwizard/dropwizard/pull/2272>`_
* Upgrade to Jdbi 3.1.0 `#2289 <https://github.com/dropwizard/dropwizard/pull/2289>`_
* Upgrade to JUnit 5.0.3
* Upgrade to Mockito 2.15.0
* Upgrade to NullAway 0.3.2

.. _rel-1.2.4:

v1.2.4: Feb 23, 2018
====================

* Upgrade Jackson to 2.9.4 in 1.2.* to address a CVE `#2269 <https://github.com/dropwizard/dropwizard/pull/2269>`_

.. _rel-1.1.7:

v1.1.7: Feb 23, 2018
====================

* Upgrade to Jackson 2.8.11 to address `CVE <https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2017-17485>`_ `#2270 <https://github.com/dropwizard/dropwizard/pull/2270>`_

.. _rel-1.2.3:

v1.2.3: Jan 24, 2018
====================

* Enable auto escaping of strings in Freemarker templates `#2251 <https://github.com/dropwizard/dropwizard/pull/2251>`_

.. _rel-1.2.2:

v1.2.2: Nov 27, 2017
====================

* Don't shut down asynchronous executor in Jersey #2221
* Add possibility to possibility to extend DropwizardApacheConnector #2220

.. _rel-1.2.1:

v1.2.1: Nov 22, 2017
====================

* Correctly set up SO_LINGER for the HTTP connector `#2176 <https://github.com/dropwizard/dropwizard/pull/2176>`_
* Support fromString in FuzzyEnumParamConverter `#2161 <https://github.com/dropwizard/dropwizard/pull/2161>`_
* Upgrade to Hibernate 5.2.12.Final to address `HHH-11996 <https://hibernate.atlassian.net/browse/HHH-11996>`_, `#2206 <https://github.com/dropwizard/dropwizard/issues/2206>`_
* Upgrade to Freemaker 2.3.27-incubating

.. _rel-1.1.6:

v1.1.6: Nov 2, 2017
===================

* Support fromString in FuzzyEnumParamConverter `#2161 <https://github.com/dropwizard/dropwizard/pull/2161>`_

.. _rel-1.1.5:

v1.1.5: Oct 17, 2017
====================

* Correctly set up SO_LINGER for the HTTP connector `#2176 <https://github.com/dropwizard/dropwizard/pull/2176>`_

.. _rel-1.2.0:

v1.2.0: Oct 6 2017
==================

`Complete changelog on GitHub <https://github.com/dropwizard/dropwizard/milestone/25?closed=1>`__

* Support configuring FileAppender#bufferSize `#1951 <https://github.com/dropwizard/dropwizard/pull/1951>`_
* Improve error handling of `@FormParam` resources `#1982 <https://github.com/dropwizard/dropwizard/pull/1982>`_
* Add JDBC interceptors through configuration `#2030 <https://github.com/dropwizard/dropwizard/pull/2030>`_
* Support Dropwizard applications without logback `#1900 <https://github.com/dropwizard/dropwizard/pull/1900>`_
* Replace deprecated SizeAndTimeBasedFNATP with SizeAndTimeBasedRollingPolicy `#2010 <https://github.com/dropwizard/dropwizard/pull/2010>`_
* Decrease allowable tomcat jdbc validation interval to 50ms `#2051 <https://github.com/dropwizard/dropwizard/pull/2051>`_
* Add support for setting several cipher suites for HTTP/2 `#2119 <https://github.com/dropwizard/dropwizard/pull/2119>`_
* Remove Dropwizard's Jackson dependency on Logback `#2112 <https://github.com/dropwizard/dropwizard/pull/2112>`_
* Handle badly formed "Accept-Language" headers `#2103 <https://github.com/dropwizard/dropwizard/pull/2103>`_
* Use LoadingCache in CachingAuthorizer `#2096 <https://github.com/dropwizard/dropwizard/pull/2096>`_
* Client NTLM Authentication `#2091 <https://github.com/dropwizard/dropwizard/pull/2091>`_
* Add optional Jersey filters `#1948 <https://github.com/dropwizard/dropwizard/pull/1948>`_
* Upgrade to Apache commons-lang3 3.6
* Upgrade to AssertJ 3.8.0
* Upgrade to classmate 1.3.4
* Upgrade to Guava 23.1
* Upgrade to H2 1.4.196
* Upgrade to Hibernate 5.2.11.Final
* Upgrade to Hibernate Validator 5.4.1.Final
* Upgrade to HSQLDB 2.4.0
* Upgrade to Jackson 2.9.1
* Upgrade to Jetty 9.4.7.v20170914
* Upgrade to JMH 1.19
* Upgrade to Joda-Time 2.9.9
* Upgrade to Logback 1.2.3
* Upgrade to Metrics 3.2.5
* Upgrade to Mockito 2.10.0
* Upgrade to Mustache.java 0.9.5
* Upgrade to Objenesis 2.6
* Upgrade to SLF4J 1.7.25
* Upgrade to tomcat-jdbc 8.5.23

.. _rel-1.1.4:

v1.1.4: Aug 24 2017
===================

`Complete changelog on GitHub <https://github.com/dropwizard/dropwizard/milestone/31?closed=1>`__

* Upgrade to Jackson 2.8.10 `#2120 <https://github.com/dropwizard/dropwizard/issues/2120>`_

.. _rel-1.1.3:

v1.1.3: Jul 31 2017
===================

`Complete changelog on GitHub <https://github.com/dropwizard/dropwizard/milestone/30?closed=1>`__

* Handle badly formed 'Accept-Language' headers `#2097 <https://github.com/dropwizard/dropwizard/issues/2097>`_
* Upgrade to Jetty 9.4.6.v20170531 to address `CVE-2017-9735 <https://nvd.nist.gov/vuln/detail/CVE-2017-9735>`_ `#2113 <https://github.com/dropwizard/dropwizard/issues/2113>`_

.. _rel-1.1.2:

v1.1.2 June 27 2017
===================

`Complete changelog on GitHub <https://github.com/dropwizard/dropwizard/milestone/28?closed=1>`__

* Updated Jackson to 2.8.9. Fixes a security `vulnerability <https://github.com/FasterXML/jackson-databind/issues/1599>`_ with default typing `#2086 <https://github.com/dropwizard/dropwizard/issues/2086>`_
* Use the correct `JsonFactory` in JSON configuration parsing `#2046 <https://github.com/dropwizard/dropwizard/issues/2046>`_
* Support of extending of `DBIFactory` `#2067 <https://github.com/dropwizard/dropwizard/issues/2067>`_
* Add time zone to Java 8 datetime mappers `#2069 <https://github.com/dropwizard/dropwizard/issues/2069>`_

.. _rel-1.0.8:

v1.0.8 June 27 2017
===================

`Complete changelog on GitHub <https://github.com/dropwizard/dropwizard/milestone/29?closed=1>`__

* Updated Jackson to 2.7.9.1. Fixes a security `vulnerability <https://github.com/FasterXML/jackson-databind/issues/1599>`_ with default typing `#2087 <https://github.com/dropwizard/dropwizard/issues/2087>`_

.. _rel-1.1.1:

v1.1.1 May 19 2017
===================

`Complete changelog on GitHub <https://github.com/dropwizard/dropwizard/milestone/27?closed=1>`__

* Set the console logging context after a reset `#1973 <https://github.com/dropwizard/dropwizard/pull/1973>`_
* Set logging context for file appenders before setting the buffer size `#1975 <https://github.com/dropwizard/dropwizard/pull/1975>`_
* Remove javax.el from jersey-bean-validation `#1976 <https://github.com/dropwizard/dropwizard/pull/1976>`_
* Exclude duplicated JTA 1.1 from dropwizard-hibernate dependencies `#1977 <https://github.com/dropwizard/dropwizard/pull/1977>`_
* Add missing @UnwrapValidatedValue annotations `#1993 <https://github.com/dropwizard/dropwizard/pull/1993>`_
* Fix HttpSessionListener.sessionDestroyed is not being called `#2032 <https://github.com/dropwizard/dropwizard/pull/2032>`_
* Add flag to make ThreadNameFilter optional `#2014 <https://github.com/dropwizard/dropwizard/pull/2014>`_

.. _rel-1.1.0:

v1.1.0: Mar 21 2017
===================

`Complete changelog on GitHub <https://github.com/dropwizard/dropwizard/milestone/15?closed=1>`__

* Upgraded to Hibernate ORM 5.2.7, introducing a series of deprecations and API changes in preparation for Hibernate ORM 6 `#1871 <https://github.com/dropwizard/dropwizard/pull/1871>`_
* Add runtime certificate reload via admin task `#1799 <https://github.com/dropwizard/dropwizard/pull/1799>`_
* List available tasks lexically via admin task `#1939 <https://github.com/dropwizard/dropwizard/pull/1939>`_
* Add support for optional resource protection `#1931 <https://github.com/dropwizard/dropwizard/pull/1931>`_
* Invalid enum request parameters result in 400 response with possible choices `#1734 <https://github.com/dropwizard/dropwizard/pull/1734>`_
* Enum request parameters are deserialized in the same fuzzy manner, as the request body `#1734 <https://github.com/dropwizard/dropwizard/pull/1734>`_
* Request parameter name displayed in response to parse failure `#1734 <https://github.com/dropwizard/dropwizard/pull/1734>`_
* Add ``DurationParam`` as a possible request parameter `#1734 <https://github.com/dropwizard/dropwizard/pull/1734>`_
* Add ``SizeParam`` as a possible request parameter `#1751 <https://github.com/dropwizard/dropwizard/pull/1751>`_
* Allow overriding of a default ``ExceptionMapper`` without re-registering all other defaults `#1768 <https://github.com/dropwizard/dropwizard/pull/1768>`_
* Allow overriding of default ``JsonProvider`` `#1788 <https://github.com/dropwizard/dropwizard/pull/1788>`_
* Finer-grain control of exception behaviour in view renderers `#1820 <https://github.com/dropwizard/dropwizard/pull/1820>`_
* Default ``WebApplicationException`` handler preserves exception HTTP headers `#1912 <https://github.com/dropwizard/dropwizard/pull/1912>`_
* JerseyClientBuilder can create rx-capable client `#1721 <https://github.com/dropwizard/dropwizard/pull/1721>`_
* Configurable response for empty ``Optional`` return values `#1784 <https://github.com/dropwizard/dropwizard/pull/1784>`_
* Add web test container agnostic way of invoking requests in ``ResourceTestRule`` `#1778 <https://github.com/dropwizard/dropwizard/pull/1778>`_
* Remove OptionalValidatedValueUnwrapper `#1583 <https://github.com/dropwizard/dropwizard/pull/1583>`_
* Allow constraints to be applied to type `#1586 <https://github.com/dropwizard/dropwizard/pull/1586>`_
* Use LoadingCache in CachingAuthenticator `#1615 <https://github.com/dropwizard/dropwizard/pull/1615>`_
* Switch cert and peer validation to false by default `#1855 <https://github.com/dropwizard/dropwizard/pull/1855>`_
* Introduce CachingAuthorizer `#1639 <https://github.com/dropwizard/dropwizard/pull/1639>`_
* Enhance logging of registered endpoints `#1804 <https://github.com/dropwizard/dropwizard/pull/1804>`_
* Flush loggers on command exit instead of destroying logging `#1947 <https://github.com/dropwizard/dropwizard/pull/1947>`_
* Add support for neverBlock on AsyncAppenders `#1917 <https://github.com/dropwizard/dropwizard/pull/1917>`_
* Allow to disable caching of Mustache views `#1289 <https://github.com/dropwizard/dropwizard/issues/1289>`_
* Add the ``httpCompliance`` option to the HTTP configuration `#1825 <https://github.com/dropwizard/dropwizard/pull/1825>`_
* Add the ``blockingTimeout`` option to the HTTP configuration `#1795 <https://github.com/dropwizard/dropwizard/pull/1795>`_
* Make ``GZipHandler`` sync-flush configurable `#1685 <https://github.com/dropwizard/dropwizard/pull/1685>`_
* Add ``min`` and ``mins`` as valid ``Duration`` abbreviations `#1833 <https://github.com/dropwizard/dropwizard/pull/1833>`_
* Register Jackson parameter-names modules `#1908 <https://github.com/dropwizard/dropwizard/pull/1908>`_
* Native Jackson deserialization of enums when Jackson annotations are present `#1909 <https://github.com/dropwizard/dropwizard/pull/1909>`_
* Add ``JsonConfigurationFactory`` for first-class support of the JSON configuration `#1897 <https://github.com/dropwizard/dropwizard/pull/1897>`_
* Support disabled and enabled attributes for metrics `#1957 <https://github.com/dropwizard/dropwizard/pull/1957>`_
* Support ``@UnitOfWork`` in sub-resources `#1959 <https://github.com/dropwizard/dropwizard/pull/1959>`_
* Upgraded to Jackson 2.8.7
* Upgraded to Hibernate Validator 5.3.4.Final
* Upgraded to Hibernate ORM 5.2.8.Final
* Upgraded to Jetty 9.4.2.v20170220
* Upgraded to tomcat-jdbc 8.5.9
* Upgraded to Objenesis 2.5.1
* Upgraded to AssertJ 3.6.2
* Upgraded to classmate 1.3.3
* Upgraded to Metrics 3.2.2 `#1970 <https://github.com/dropwizard/dropwizard/pull/1970>`_
* Upgraded to Mustache 0.9.4 `#1766 <https://github.com/dropwizard/dropwizard/pull/1766>`_
* Upgraded to Mockito 2.7.12
* Upgraded to Liquibase 3.5.3
* Upgraded to Logback 1.2.1 `#1918 <https://github.com/dropwizard/dropwizard/pull/1927>`_
* Upgraded to JDBI 2.78
* Upgraded to Jersey 2.25.1
* Upgraded to javassist 3.21.0-GA
* Upgraded to Guava 21.0
* Upgraded to SLF4J 1.7.24
* Upgraded to H2 1.4.193
* Upgraded to Joda-Time 2.9.7
* Upgraded to commons-lang3 3.5
* Upgraded to Apache HTTP Client 4.5.3
* Upgraded to Jadira Usertype Core 6.0.1.GA

.. _rel-1.0.7:

v1.0.7 Mar 20 2017
==================

`Complete changelog on GitHub <https://github.com/dropwizard/dropwizard/milestone/26?closed=1>`__

* Upgrade to Metrics 3.1.4 `#1969 <https://github.com/dropwizard/dropwizard/pull/1969>`_

.. _rel-1.0.6:

v1.0.6 Jan 30 2017
==================

`Complete changelog on GitHub <https://github.com/dropwizard/dropwizard/milestone/23?closed=1>`__

* Switch cert and peer validation to false by default `#1855 <https://github.com/dropwizard/dropwizard/pull/1855>`_
* Add a JUnit rule for testing database interactions `#1905 <https://github.com/dropwizard/dropwizard/pull/1905>`_

.. _rel-1.0.5:

v1.0.5 Nov 18 2016
==================

`Complete changelog on GitHub <https://github.com/dropwizard/dropwizard/milestone/22?closed=1>`__

* Fix request logs with request parameter in layout pattern `#1828 <https://github.com/dropwizard/dropwizard/pull/1828>`_

.. _rel-1.0.4:

v1.0.4 Nov 14 2016
==================

`Complete changelog on GitHub <https://github.com/dropwizard/dropwizard/milestone/21?closed=1>`__

* Upgraded to Jersey 2.23.2 `#1808 <https://github.com/dropwizard/dropwizard/pull/1808>`_
* Brought back support for request logging with ``logback-classic`` `#1813 <https://github.com/dropwizard/dropwizard/pull/1813>`_

.. _rel-1.0.3:

v1.0.3: Oct 28 2016
===================

`Complete changelog on GitHub <https://github.com/dropwizard/dropwizard/milestone/20?closed=1>`__

* Fix support maxFileSize and archivedFileCount `#1660 <https://github.com/dropwizard/dropwizard/pull/1660>`_
* Upgraded to Jackson 2.7.8 `#1755 <https://github.com/dropwizard/dropwizard/pull/1755>`_
* Upgraded to Mustache 0.9.4 `#1766 <https://github.com/dropwizard/dropwizard/pull/1766>`_
* Prefer use of assertj's java8 exception assertions `#1753 <https://github.com/dropwizard/dropwizard/pull/1753>`_

.. _rel-1.0.2:

v1.0.2: Sep 23 2016
===================

`Complete changelog on GitHub <https://github.com/dropwizard/dropwizard/milestone/19?closed=1>`__

* Fix absence of request logs in Dropwizard 1.0.1 `#1737 <https://github.com/dropwizard/dropwizard/pull/1737>`_

.. _rel-1.0.1:

v1.0.1: Sep 21 2016
===================

`Complete changelog on GitHub <https://github.com/dropwizard/dropwizard/milestone/17?closed=1>`__

* Allow use of custom HostnameVerifier on clients `#1664 <https://github.com/dropwizard/dropwizard/pull/1664>`_
* Allow to configure failing on unknown properties in the Dropwizard configuration `#1677 <https://github.com/dropwizard/dropwizard/pull/1677>`_
* Fix request attribute-related race condition in Logback request logging `#1678 <https://github.com/dropwizard/dropwizard/pull/1678>`_
* Log Jetty initialized SSLContext not the Default `#1698 <https://github.com/dropwizard/dropwizard/pull/1698>`_
* Fix NPE of non-resource sub-resource methods `#1718 <https://github.com/dropwizard/dropwizard/pull/1718>`_

.. _rel-1.0.0:

v1.0.0: Jul 26 2016
===================

`Complete changelog on GitHub <https://github.com/dropwizard/dropwizard/milestone/9?closed=1>`__

* Using Java 8 as baseline
* ``dropwizard-java8`` bundle merged into mainline `#1365 <https://github.com/dropwizard/dropwizard/issues/1365>`_
* HTTP/2 and server push support `#1349 <https://github.com/dropwizard/dropwizard/issues/1349>`_
* ``dropwizard-spdy`` module is removed in favor of ``dropwizard-http2`` `#1330 <https://github.com/dropwizard/dropwizard/pull/1330>`_
* Switching to ``logback-access`` for HTTP request logging `#1415 <https://github.com/dropwizard/dropwizard/pull/1415>`_
* Support for validating return values in JAX-RS resources `#1251 <https://github.com/dropwizard/dropwizard/pull/1251>`_
* Consistent handling null entities in JAX-RS resources `#1251 <https://github.com/dropwizard/dropwizard/pull/1251>`_
* Support for validating bean members in JAX-RS resources `#1572 <https://github.com/dropwizard/dropwizard/pull/1572>`_
* Returning an HTTP 500 error for entities that can't be serialized `#1347 <https://github.com/dropwizard/dropwizard/pull/1347>`_
* Support serialisation of lazy loaded POJOs in Hibernate `#1466 <https://github.com/dropwizard/dropwizard/pull/1466>`_
* Support fallback to the ``toString`` method during deserializing enum values from JSON  `#1340 <https://github.com/dropwizard/dropwizard/pull/1340>`_
* Support for setting default headers in Apache HTTP client `#1354 <https://github.com/dropwizard/dropwizard/pull/1354>`_
* Printing help once on invalid command line arguments `#1376 <https://github.com/dropwizard/dropwizard/pull/1376>`_
* Support for case insensitive and all single letter ``SizeUnit`` suffixes `#1380 <https://github.com/dropwizard/dropwizard/pull/1380>`_
* Added a development profile to the build `#1364 <https://github.com/dropwizard/dropwizard/issues/1364>`_
* All the default exception mappers in ``ResourceTestRule`` are registered by default `#1387 <https://github.com/dropwizard/dropwizard/pull/1387>`_
* Allow DB minSize and initialSize to be zero for lazy connections `#1517 <https://github.com/dropwizard/dropwizard/pull/1517>`_
* Ability to provide own ``RequestLogFactory`` `#1290 <https://github.com/dropwizard/dropwizard/pull/1290>`_
* Support for authentication by polymorphic principals `#1392 <https://github.com/dropwizard/dropwizard/pull/1392>`_
* Support for configuring Jetty's ``inheritedChannel`` option `#1410 <https://github.com/dropwizard/dropwizard/pull/1410>`_
* Support for using ``DropwizardAppRule`` at the suite level `#1411 <https://github.com/dropwizard/dropwizard/pull/1411>`_
* Support for adding multiple ``MigrationBundles`` `#1430 <https://github.com/dropwizard/dropwizard/pull/1430>`_
* Support for obtaining server context paths in the ``Application.run`` method `#1503 <https://github.com/dropwizard/dropwizard/pull/1503>`_
* Support for unlimited log files for file appender `#1549 <https://github.com/dropwizard/dropwizard/pull/1549>`_
* Support for log file names determined by logging policy `#1561 <https://github.com/dropwizard/dropwizard/pull/1561>`_
* Default Graphite reporter port changed from 8080 to 2003 `#1538 <https://github.com/dropwizard/dropwizard/pull/1538>`_
* Upgraded to Apache HTTP Client 4.5.2
* Upgraded to argparse4j 0.7.0
* Upgraded to Guava 19.0
* Upgraded to H2 1.4.192
* Upgraded to Hibernate 5.1.0 `#1429 <https://github.com/dropwizard/dropwizard/pull/1429>`_
* Upgraded to Hibernate Validator 5.2.4.Final
* Upgraded to HSQLDB 2.3.4
* Upgraded to Jadira Usertype Core 5.0.0.GA
* Upgraded to Jackson 2.7.6
* Upgraded to JDBI 2.73 `#1358 <https://github.com/dropwizard/dropwizard/pull/1358>`_
* Upgraded to Jersey 2.23.1
* Upgraded to Jetty 9.3.9.v20160517 `#1330 <https://github.com/dropwizard/dropwizard/pull/1330>`_
* Upgraded to JMH 1.12
* Upgraded to Joda-Time 2.9.4
* Upgraded to Liquibase 3.5.1
* Upgraded to liquibase-slf4j 2.0.0
* Upgraded to Logback 1.1.7
* Upgraded to Mustache 0.9.2
* Upgraded to SLF4J 1.7.21
* Upgraded to tomcat-jdbc 8.5.3
* Upgraded to Objenesis 2.3
* Upgraded to AssertJ 3.4.1
* Upgraded to Mockito 2.0.54-beta

.. _rel-0.9.2:

v0.9.2: Jan 20 2016
===================

`Complete changelog on GitHub <https://github.com/dropwizard/dropwizard/milestone/14?closed=1>`__

* Support `@UnitOfWork` annotation outside of Jersey resources `#1361 <https://github.com/dropwizard/dropwizard/issues/1361>`_

.. _rel-0.9.1:

v0.9.1: Nov 3 2015
==================

`Complete changelog on GitHub <https://github.com/dropwizard/dropwizard/milestone/13?closed=1>`__

* Add ``ConfigurationSourceProvider`` for reading resources from classpath `#1314 <https://github.com/dropwizard/dropwizard/issues/1314>`_
* Add ``@UnwrapValidatedValue`` annotation to `BaseReporterFactory.frequency` `#1308 <https://github.com/dropwizard/dropwizard/issues/1308>`_, `#1309 <https://github.com/dropwizard/dropwizard/issues/1309>`_
* Fix serialization of default configuration for ``DataSourceFactory`` by deprecating ``PooledDataSourceFactory#getHealthCheckValidationQuery()`` and ``PooledDataSourceFactory#getHealthCheckValidationTimeout()`` `#1321 <https://github.com/dropwizard/dropwizard/issues/1321>`_, `#1322 <https://github.com/dropwizard/dropwizard/pull/1322>`_
* Treat ``null`` values in JAX-RS resource method parameters of type ``Optional<T>`` as absent value after conversion `#1323 <https://github.com/dropwizard/dropwizard/pull/1323>`_

.. _rel-0.9.0:

v0.9.0: Oct 28 2015
===================

`Complete changelog on GitHub <https://github.com/dropwizard/dropwizard/milestone/8?closed=1>`__

* Various documentation fixes and improvements
* New filter-based authorization & authentication `#952 <https://github.com/dropwizard/dropwizard/pull/952>`_, `#1023 <https://github.com/dropwizard/dropwizard/pull/1023>`_, `#1114 <https://github.com/dropwizard/dropwizard/pull/1114>`_, `#1162 <https://github.com/dropwizard/dropwizard/pull/1162>`_, `#1241 <https://github.com/dropwizard/dropwizard/pull/1241>`_
* Fixed a security bug in ``CachingAuthenticator`` with caching results of failed authentication attempts `#1082 <https://github.com/dropwizard/dropwizard/pull/1082>`_
* Correct handling misconfigured context paths in ``ServerFactory`` `#785 <https://github.com/dropwizard/dropwizard/pull/785>`_
* Logging context paths during application startup `#994 <https://github.com/dropwizard/dropwizard/pull/994>`_, `#1072 <https://github.com/dropwizard/dropwizard/pull/1072>`_
* Support for `Jersey Bean Validation <https://jersey.github.io/documentation/latest/bean-validation.html>`_ `#842 <https://github.com/dropwizard/dropwizard/pull/842>`_
* Returning descriptive constraint violation messages `#1039 <https://github.com/dropwizard/dropwizard/pull/1039>`_,
* Trace logging of failed constraint violations `#992 <https://github.com/dropwizard/dropwizard/pull/992>`_
* Returning correct HTTP status codes for constraint violations `#993 <https://github.com/dropwizard/dropwizard/pull/993>`_
* Fixed possible XSS in constraint violations `#892 <https://github.com/dropwizard/dropwizard/issues/892>`_
* Support for including caller data in appenders `#995 <https://github.com/dropwizard/dropwizard/pull/995>`_
* Support for defining custom logging factories (e.g. native Logback) `#996 <https://github.com/dropwizard/dropwizard/pull/996>`_
* Support for defining the maximum log file size in ``FileAppenderFactory``. `#1000 <https://github.com/dropwizard/dropwizard/pull/1000>`_
* Support for fixed window rolling policy in ``FileAppenderFactory`` `#1218 <https://github.com/dropwizard/dropwizard/pull/1218>`_
* Support for individual logger appenders `#1092 <https://github.com/dropwizard/dropwizard/pull/1092>`_
* Support for disabling logger additivity `#1215 <https://github.com/dropwizard/dropwizard/pull/1215>`_
* Sorting endpoints in the application startup log `#1002 <https://github.com/dropwizard/dropwizard/pull/1002>`_
* Dynamic DNS resolution in the Graphite metric reporter `#1004 <https://github.com/dropwizard/dropwizard/pull/1004>`_
* Support for defining a custom ``MetricRegistry`` during bootstrap (e.g. with HdrHistogram) `#1015 <https://github.com/dropwizard/dropwizard/pull/1015>`_
* Support for defining a custom ``ObjectMapper`` during bootstrap. `#1112 <https://github.com/dropwizard/dropwizard/pull/1112>`_
* Added facility to plug-in custom DB connection pools (e.g. HikariCP) `#1030 <https://github.com/dropwizard/dropwizard/pull/1030>`_
* Support for setting a custom DB pool connection validator `#1113 <https://github.com/dropwizard/dropwizard/pull/1113>`_
* Support for enabling of removing abandoned DB pool connections `#1264 <https://github.com/dropwizard/dropwizard/pull/1264>`_
* Support for credentials in a DB data source URL `#1260 <https://github.com/dropwizard/dropwizard/pull/1260>`_
* Support for simultaneous work of several Hibernate bundles `#1276 <https://github.com/dropwizard/dropwizard/pull/1276>`_
* HTTP(S) proxy support for Dropwizard HTTP client `#657 <https://github.com/dropwizard/dropwizard/pull/657>`_
* Support external configuration of TLS properties for Dropwizard HTTP client `#1224 <https://github.com/dropwizard/dropwizard/pull/1224>`_
* Support for not accepting GZIP-compressed responses in HTTP clients `#1270 <https://github.com/dropwizard/dropwizard/pull/1270>`_
* Support for setting a custom redirect strategy in HTTP clients `#1281 <https://github.com/dropwizard/dropwizard/pull/1281>`_
* Apache and Jersey clients are now managed by the application environment `#1061 <https://github.com/dropwizard/dropwizard/pull/1061>`_
* Support for request-scoped configuration for Jersey client  `#939 <https://github.com/dropwizard/dropwizard/pull/939>`_
* Respecting Jackson feature for deserializing enums using ``toString`` `#1104 <https://github.com/dropwizard/dropwizard/pull/1104>`_
* Support for passing explicit ``Configuration`` via test rules `#1131 <https://github.com/dropwizard/dropwizard/pull/1131>`_
* On view template error, return a generic error page instead of template not found `#1178 <https://github.com/dropwizard/dropwizard/pull/1178>`_
* In some cases an instance of Jersey HTTP client could be abruptly closed during the application lifetime `#1232 <https://github.com/dropwizard/dropwizard/pull/1232>`_
* Improved build time build by running tests in parallel `#1032 <https://github.com/dropwizard/dropwizard/pull/1032>`_
* Added JMH benchmarks  `#990 <https://github.com/dropwizard/dropwizard/pull/990>`_
* Allow customization of Hibernate ``SessionFactory`` `#1182 <https://github.com/dropwizard/dropwizard/issue/1182>`_
* Removed javax.el-2.x in favour of javax.el-3.0
* Upgraded to argparse4j 0.6.0
* Upgrade to AssertJ 2.2.0
* Upgraded to JDBI 2.63.1
* Upgraded to Apache HTTP Client 4.5.1
* Upgraded to Dropwizard Metrics 3.1.2
* Upgraded to Freemarker 2.3.23
* Upgraded to H2 1.4.190
* Upgraded to Hibernate 4.3.11.Final
* Upgraded to Jackson 2.6.3
* Upgraded to Jadira Usertype Core 4.0.0.GA
* Upgraded to Jersey 2.22.1
* Upgraded to Jetty 9.2.13.v20150730
* Upgraded to Joda-Time 2.9
* Upgraded to JSR305 annotations 3.0.1
* Upgraded to Hibernate Validator 5.2.2.Final
* Upgraded to Jetty ALPN boot 7.1.3.v20150130
* Upgraded to Jetty SetUID support 1.0.3
* Upgraded to Liquibase 3.4.1
* Upgraded to Logback 1.1.3
* Upgraded to Metrics 3.1.2
* Upgraded to Mockito 1.10.19
* Upgraded to SLF4J 1.7.12
* Upgraded to commons-lang3 3.4
* Upgraded to tomcat-jdbc 8.0.28

.. _rel-0.8.5:

v0.8.5: Nov 3 2015
==================

`Complete changelog on GitHub <https://github.com/dropwizard/dropwizard/milestone/12?closed=1>`__

* Treat ``null`` values in JAX-RS resource method parameters of type ``Optional<T>`` as absent value after conversion `#1323 <https://github.com/dropwizard/dropwizard/pull/1323>`_

.. _rel-0.8.4:

v0.8.4: Aug 26 2015
===================

* Upgrade to Apache HTTP Client 4.5
* Upgrade to Jersey 2.21
* Fixed user-agent shadowing in Jersey HTTP Client `#1198 <https://github.com/dropwizard/dropwizard/pull/1198>`_

.. _rel-0.8.3:

v0.8.3: Aug 24 2015
===================

`Complete changelog on GitHub <https://github.com/dropwizard/dropwizard/milestone/11?closed=1>`__

* Fixed an issue with closing the HTTP client connection pool after a full GC `#1160 <https://github.com/dropwizard/dropwizard/pull/1160>`_

.. _rel-0.8.2:

v0.8.2: Jul 6 2015
==================

`Complete changelog on GitHub <https://github.com/dropwizard/dropwizard/milestone/10?closed=1>`__

* Support for request-scoped configuration for Jersey client `#1137 <https://github.com/dropwizard/dropwizard/pull/1137>`_
* Upgraded to Jersey 2.19 `#1143 <https://github.com/dropwizard/dropwizard/pull/1143>`_

.. _rel-0.8.1:

v0.8.1: Apr 7 2015
==================

`Complete changelog on GitHub <https://github.com/dropwizard/dropwizard/milestone/7?closed=1>`__

* Fixed transaction committing lifecycle for ``@UnitOfWork``  (#850, #915)
* Fixed noisy Logback messages on startup (#902)
* Ability to use providers in TestRule, allows testing of auth & views (#513, #922)
* Custom ExceptionMapper not invoked when Hibernate rollback (#949)
* Support for setting a time bound on DBI and Hibernate health checks
* Default configuration for views
* Ensure that JerseyRequest scoped ClientConfig gets propagated to HttpUriRequest
* More example tests
* Fixed security issue where info is leaked during validation of unauthenticated resources(#768)

.. _rel-0.8.0:

v0.8.0: Mar 5 2015
==================

`Complete changelog on GitHub <https://github.com/dropwizard/dropwizard/milestone/5?closed=1>`__

* Migrated ``dropwizard-spdy`` from NPN to ALPN
* Dropped support for deprecated SPDY/2 in ``dropwizard-spdy``
* Upgrade to argparse4j 0.4.4
* Upgrade to commons-lang3 3.3.2
* Upgrade to Guava 18.0
* Upgrade to H2 1.4.185
* Upgrade to Hibernate 4.3.5.Final
* Upgrade to Hibernate Validator 5.1.3.Final
* Upgrade to Jackson 2.5.1
* Upgrade to JDBI 2.59
* Upgrade to Jersey 2.16
* Upgrade to Jetty 9.2.9.v20150224
* Upgrade to Joda-Time 2.7
* Upgrade to Liquibase 3.3.2
* Upgrade to Mustache 0.8.16
* Upgrade to SLF4J 1.7.10
* Upgrade to tomcat-jdbc 8.0.18
* Upgrade to JSR305 annotations 3.0.0
* Upgrade to Junit 4.12
* Upgrade to AssertJ 1.7.1
* Upgrade to Mockito 1.10.17
* Support for range headers
* Ability to use Apache client configuration for Jersey client
* Warning when maximum pool size and unbounded queues are combined
* Fixed connection leak in CloseableLiquibase
* Support ScheduledExecutorService with daemon thread
* Improved DropwizardAppRule
* Better connection pool metrics
* Removed final modifier from Application#run
* Fixed gzip encoding to support Jersey 2.x
* Configuration to toggle regex [in/ex]clusion for Metrics
* Configuration to disable default exception mappers
* Configuration support for disabling chunked encoding
* Documentation fixes and upgrades


.. _rel-0.7.1:

v0.7.1: Jun 18 2014
===================

`Complete changelog on GitHub <https://github.com/dropwizard/dropwizard/milestone/6?closed=1>`__

* Added instrumentation to ``Task``, using metrics annotations.
* Added ability to blacklist SSL cipher suites.
* Added ``@PATCH`` annotation for Jersey resource methods to indicate use of the HTTP ``PATCH`` method.
* Added support for configurable request retry behavior for ``HttpClientBuilder`` and ``JerseyClientBuilder``.
* Added facility to get the admin HTTP port in ``DropwizardAppTestRule``.
* Added ``ScanningHibernateBundle``, which scans packages for entities, instead of requiring you to add them individually.
* Added facility to invalidate credentials from the ``CachingAuthenticator`` that match a specified ``Predicate``.
* Added a CI build profile for JDK 8 to ensure that Dropwizard builds against the latest version of the JDK.
* Added ``--catalog`` and ``--schema`` options to Liquibase.
* Added ``stackTracePrefix`` configuration option to ``SyslogAppenderFactory`` to configure the pattern prepended to each line in the stack-trace sent to syslog. Defaults to the TAB character, "\t". Note: this is different from the bang prepended to text logs (such as "console", and "file"), as syslog has different conventions for multi-line messages.
* Added ability to validate ``Optional`` values using validation annotations. Such values require the ``@UnwrapValidatedValue`` annotation, in addition to the validations you wish to use.
* Added facility to configure the ``User-Agent`` for ``HttpClient``. Configurable via the ``userAgent`` configuration option.
* Added configurable ``AllowedMethodsFilter``. Configure allowed HTTP methods for both the application and admin connectors with ``allowedMethods``.
* Added support for specifying a ``CredentialProvider`` for HTTP clients.
* Fixed silently overriding Servlets or ServletFilters; registering a duplicate will now emit a warning.
* Fixed ``SyslogAppenderFactory`` failing when the application name contains a PCRE reserved character (e.g. ``/`` or ``$``).
* Fixed regression causing JMX reporting of metrics to not be enabled by default.
* Fixed transitive dependencies on log4j and extraneous sl4j backends bleeding in to projects. Dropwizard will now enforce that only Logback and slf4j-logback are used everywhere.
* Fixed clients disconnecting before the request has been fully received causing a "500 Internal Server Error" to be generated for the request log. Such situations will now correctly generate a "400 Bad Request", as the request is malformed. Clients will never see these responses, but they matter for logging and metrics that were previously considering this situation as a server error.
* Fixed ``DiscoverableSubtypeResolver`` using the system ``ClassLoader``, instead of the local one.
* Fixed regression causing Liquibase ``--dump`` to fail to dump the database.
* Fixed the CSV metrics reporter failing when the output directory doesn't exist. It will now attempt to create the directory on startup.
* Fixed global frequency for metrics reporters being permanently overridden by the default frequency for individual reporters.
* Fixed tests failing on Windows due to platform-specific line separators.
* Changed ``DropwizardAppTestRule`` so that it no longer requires a configuration path to operate. When no path is specified, it will now use the applications' default configuration.
* Changed ``Bootstrap`` so that ``getMetricsFactory()`` may now be overridden to provide a custom instance to the framework to use.
* Upgraded to Guava 17.0
  Note: this addresses a bug with BloomFilters that is incompatible with pre-17.0 BloomFilters.
* Upgraded to Jackson 2.3.3
* Upgraded to Apache HttpClient 4.3.4
* Upgraded to Metrics 3.0.2
* Upgraded to Logback 1.1.2
* Upgraded to h2 1.4.178
* Upgraded to JDBI 2.55
* Upgraded to Hibernate 4.3.5 Final
* Upgraded to Hibernate Validator 5.1.1 Final
* Upgraded to Mustache 0.8.15

.. _rel-0.7.0:

v0.7.0: Apr 04 2014
===================

`Complete changelog on GitHub <https://github.com/dropwizard/dropwizard/milestone/4?closed=1>`__

* Upgraded to Java 7.
* Moved to the ``io.dropwizard`` group ID and namespace.
* Extracted out a number of reusable libraries: ``dropwizard-configuration``,
  ``dropwizard-jackson``, ``dropwizard-jersey``, ``dropwizard-jetty``, ``dropwizard-lifecycle``,
  ``dropwizard-logging``, ``dropwizard-servlets``, ``dropwizard-util``, ``dropwizard-validation``.
* Extracted out various elements of ``Environment`` to separate classes: ``JerseyEnvironment``,
  ``LifecycleEnvironment``, etc.
* Extracted out ``dropwizard-views-freemarker`` and ``dropwizard-views-mustache``.
  ``dropwizard-views`` just provides infrastructure now.
* Renamed ``Service`` to ``Application``.
* Added ``dropwizard-forms``, which provides support for multipart MIME entities.
* Added ``dropwizard-spdy``.
* Added ``AppenderFactory``, allowing for arbitrary logging appenders for application and request
  logs.
* Added ``ConnectorFactory``, allowing for arbitrary Jetty connectors.
* Added ``ServerFactory``, with multi- and single-connector implementations.
* Added ``ReporterFactory``, for metrics reporters, with Graphite and Ganglia implementations.
* Added ``ConfigurationSourceProvider`` to allow loading configuration files from sources other than
  the filesystem.
* Added setuid support. Configure the user/group to run as and soft/hard open file limits in the
  ``ServerFactory``. To bind to privileged ports (e.g. 80), enable ``startsAsRoot`` and set ``user``
  and ``group``, then start your application as the root user.
* Added builders for managed executors.
* Added a default ``check`` command, which loads and validates the service configuration.
* Added support for the Jersey HTTP client to ``dropwizard-client``.
* Added Jackson Afterburner support.
* Added support for ``deflate``-encoded requests and responses.
* Added support for HTTP Sessions. Add the annotated parameter to your resource method:
  ``@Session HttpSession session`` to have the session context injected.
* Added support for a "flash" message to be propagated across requests. Add the annotated parameter
  to your resource method: ``@Session Flash message`` to have any existing flash message injected.
* Added support for deserializing Java ``enums`` with fuzzy matching rules (i.e., whitespace
  stripping, ``-``/``_`` equivalence, case insensitivity, etc.).
* Added ``HibernateBundle#configure(Configuration)`` for customization of Hibernate configuration.
* Added support for Joda Time ``DateTime`` arguments and results when using JDBI.
* Added configuration option to include Exception stack-traces when logging to syslog. Stack traces
  are now excluded by default.
* Added the application name and PID (if detectable) to the beginning of syslog messages, as is the
  convention.
* Added ``--migrations`` command-line option to ``migrate`` command to supply the migrations
  file explicitly.
* Validation errors are now returned as ``application/json`` responses.
* Simplified ``AsyncRequestLog``; now standardized on Jetty 9 NCSA format.
* Renamed ``DatabaseConfiguration`` to ``DataSourceFactory``, and ``ConfigurationStrategy`` to
  ``DatabaseConfiguration``.
* Changed logging to be asynchronous. Messages are now buffered and batched in-memory before being
  delivered to the configured appender(s).
* Changed handling of runtime configuration errors. Will no longer display an Exception stack-trace
  and will present a more useful description of the problem, including suggestions when appropriate.
* Changed error handling to depend more heavily on Jersey exception mapping.
* Changed ``dropwizard-db`` to use ``tomcat-jdbc`` instead of ``tomcat-dbcp``.
* Changed default formatting when logging nested Exceptions to display the root-cause first.
* Replaced ``ResourceTest`` with ``ResourceTestRule``, a JUnit ``TestRule``.
* Dropped Scala support.
* Dropped ``ManagedSessionFactory``.
* Dropped ``ObjectMapperFactory``; use ``ObjectMapper`` instead.
* Dropped ``Validator``; use ``javax.validation.Validator`` instead.
* Fixed a shutdown bug in ``dropwizard-migrations``.
* Fixed formatting of "Caused by" lines not being prefixed when logging nested Exceptions.
* Fixed not all available Jersey endpoints were being logged at startup.
* Upgraded to argparse4j 0.4.3.
* Upgraded to Guava 16.0.1.
* Upgraded to Hibernate Validator 5.0.2.
* Upgraded to Jackson 2.3.1.
* Upgraded to JDBI 2.53.
* Upgraded to Jetty 9.0.7.
* Upgraded to Liquibase 3.1.1.
* Upgraded to Logback 1.1.1.
* Upgraded to Metrics 3.0.1.
* Upgraded to Mustache 0.8.14.
* Upgraded to SLF4J 1.7.6.
* Upgraded to Jersey 1.18.
* Upgraded to Apache HttpClient 4.3.2.
* Upgraded to tomcat-jdbc 7.0.50.
* Upgraded to Hibernate 4.3.1.Final.

.. _rel-0.6.2:

v0.6.2: Mar 18 2013
===================

* Added support for non-UTF8 views.
* Fixed an NPE for services in the root package.
* Fixed exception handling in ``TaskServlet``.
* Upgraded to Slf4j 1.7.4.
* Upgraded to Jetty 8.1.10.
* Upgraded to Jersey 1.17.1.
* Upgraded to Jackson 2.1.4.
* Upgraded to Logback 1.0.10.
* Upgraded to Hibernate 4.1.9.
* Upgraded to Hibernate Validator 4.3.1.
* Upgraded to tomcat-dbcp 7.0.37.
* Upgraded to Mustache.java 0.8.10.
* Upgraded to Apache HttpClient 4.2.3.
* Upgraded to Jackson 2.1.3.
* Upgraded to argparse4j 0.4.0.
* Upgraded to Guava 14.0.1.
* Upgraded to Joda Time 2.2.
* Added ``retries`` to ``HttpClientConfiguration``.
* Fixed log formatting for extended stack traces, also now using extended stack traces as the
  default.
* Upgraded to FEST Assert 2.0M10.

.. _rel-0.6.1:

v0.6.1: Nov 28 2012
===================

* Fixed incorrect latencies in request logs on Linux.
* Added ability to register multiple ``ServerLifecycleListener`` instances.

.. _rel-0.6.0:

v0.6.0: Nov 26 2012
===================

* Added Hibernate support in ``dropwizard-hibernate``.
* Added Liquibase migrations in ``dropwizard-migrations``.
* Renamed ``http.acceptorThreadCount`` to ``http.acceptorThreads``.
* Renamed ``ssl.keyStorePath`` to ``ssl.keyStore``.
* Dropped ``JerseyClient``. Use Jersey's ``Client`` class instead.
* Moved JDBI support to ``dropwizard-jdbi``.
* Dropped ``Database``. Use JDBI's ``DBI`` class instead.
* Dropped the ``Json`` class. Use ``ObjectMapperFactory`` and ``ObjectMapper`` instead.
* Decoupled JDBI support from tomcat-dbcp.
* Added group support to ``Validator``.
* Moved CLI support to argparse4j.
* Fixed testing support for ``Optional`` resource method parameters.
* Fixed Freemarker support to use its internal encoding map.
* Added property support to ``ResourceTest``.
* Fixed JDBI metrics support for raw SQL queries.
* Dropped Hamcrest matchers in favor of FEST assertions in ``dropwizard-testing``.
* Split ``Environment`` into ``Bootstrap`` and ``Environment``, and broke configuration of each into
  ``Service``'s ``#initialize(Bootstrap)`` and ``#run(Configuration, Environment)``.
* Combined ``AbstractService`` and ``Service``.
* Trimmed down ``ScalaService``, so be sure to add ``ScalaBundle``.
* Added support for using ``JerseyClientFactory`` without an ``Environment``.
* Dropped Jerkson in favor of Jackson's Scala module.
* Added ``Optional`` support for JDBI.
* Fixed bug in stopping ``AsyncRequestLog``.
* Added ``UUIDParam``.
* Upgraded to Metrics 2.2.0.
* Upgraded to Jetty 8.1.8.
* Upgraded to Mockito 1.9.5.
* Upgraded to tomcat-dbcp 7.0.33.
* Upgraded to Mustache 0.8.8.
* Upgraded to Jersey 1.15.
* Upgraded to Apache HttpClient 4.2.2.
* Upgraded to JDBI 2.41.
* Upgraded to Logback 1.0.7 and SLF4J 1.7.2.
* Upgraded to Guava 13.0.1.
* Upgraded to Jackson 2.1.1.
* Added support for Joda Time.

.. note:: Upgrading to 0.6.0 will require changing your code. First, your ``Service`` subclass will
          need to implement both ``#initialize(Bootstrap<T>)`` **and**
          ``#run(T, Environment)``. What used to be in ``initialize`` should be moved to ``run``.
          Second, your representation classes need to be migrated to Jackson 2. For the most part,
          this is just changing imports to ``com.fasterxml.jackson.annotation.*``, but there are
          `some subtler changes in functionality <http://wiki.fasterxml.com/JacksonUpgradeFrom19To20>`_.
          Finally, references to 0.5.x's ``Json``, ``JerseyClient``, or ``JDBI`` classes should be
          changed to Jackon's ``ObjectMapper``, Jersey's ``Client``, and JDBI's ``DBI``
          respectively.

.. _rel-0.5.1:

v0.5.1: Aug 06 2012
===================

* Fixed logging of managed objects.
* Fixed default file logging configuration.
* Added FEST-Assert as a ``dropwizard-testing`` dependency.
* Added support for Mustache templates (``*.mustache``) to ``dropwizard-views``.
* Added support for arbitrary view renderers.
* Fixed command-line overrides when no configuration file is present.
* Added support for arbitrary ``DnsResolver`` implementations in ``HttpClientFactory``.
* Upgraded to Guava 13.0 final.
* Fixed task path bugs.
* Upgraded to Metrics 2.1.3.
* Added ``JerseyClientConfiguration#compressRequestEntity`` for disabling the compression of request
  entities.
* Added ``Environment#scanPackagesForResourcesAndProviders`` for automatically detecting Jersey
  providers and resources.
* Added ``Environment#setSessionHandler``.

.. _rel-0.5.0:

v0.5.0: Jul 30 2012
===================

* Upgraded to JDBI 2.38.1.
* Upgraded to Jackson 1.9.9.
* Upgraded to Jersey 1.13.
* Upgraded to Guava 13.0-rc2.
* Upgraded to HttpClient 4.2.1.
* Upgraded to tomcat-dbcp 7.0.29.
* Upgraded to Jetty 8.1.5.
* Improved ``AssetServlet``:

  * More accurate ``Last-Modified-At`` timestamps.
  * More general asset specification.
  * Default filename is now configurable.

* Improved ``JacksonMessageBodyProvider``:

  * Now based on Jackson's JAX-RS support.
  * Doesn't read or write types annotated with ``@JsonIgnoreType``.

* Added ``@MinSize``, ``@MaxSize``, and ``@SizeRange`` validations.
* Added ``@MinDuration``, ``@MaxDuration``, and ``@DurationRange`` validations.
* Fixed race conditions in Logback initialization routines.
* Fixed ``TaskServlet`` problems with custom context paths.
* Added ``jersey-text-framework-core`` as an explicit dependency of ``dropwizard-testing``. This
  helps out some non-Maven build frameworks with bugs in dependency processing.
* Added ``addProvider`` to ``JerseyClientFactory``.
* Fixed ``NullPointerException`` problems with anonymous health check classes.
* Added support for serializing/deserializing ``ByteBuffer`` instances as JSON.
* Added ``supportedProtocols`` to SSL configuration, and disabled SSLv2 by default.
* Added support for ``Optional<Integer>`` query parameters and others.
* Removed ``jersey-freemarker`` dependency from ``dropwizard-views``.
* Fixed missing thread contexts in logging statements.
* Made the configuration file argument for the ``server`` command optional.
* Added support for disabling log rotation.
* Added support for arbitrary KeyStore types.
* Added ``Log.forThisClass()``.
* Made explicit service names optional.

.. _rel-0.4.4:

v0.4.4: Jul 24 2012
===================

* Added support for ``@JsonIgnoreType`` to ``JacksonMessageBodyProvider``.

.. _rel-0.4.3:

v0.4.3: Jun 22 2012
===================

* Re-enable immediate flushing for file and console logging appenders.

.. _rel-0.4.2:

v0.4.2: Jun 20 2012
===================

* Fixed ``JsonProcessingExceptionMapper``. Now returns human-readable error messages for malformed
  or invalid JSON as a ``400 Bad Request``. Also handles problems with JSON generation and object
  mapping in a developer-friendly way.

.. _rel-0.4.1:

v0.4.1: Jun 19 2012
===================

* Fixed type parameter resolution in for subclasses of subclasses of ``ConfiguredCommand``.
* Upgraded to Jackson 1.9.7.
* Upgraded to Logback 1.0.6, with asynchronous logging.
* Upgraded to Hibernate Validator 4.3.0.
* Upgraded to JDBI 2.34.
* Upgraded to Jetty 8.1.4.
* Added ``logging.console.format``, ``logging.file.format``, and ``logging.syslog.format``
  parameters for custom log formats.
* Extended ``ResourceTest`` to allow for enabling/disabling specific Jersey features.
* Made ``Configuration`` serializable as JSON.
* Stopped lumping command-line options in a group in ``Command``.
* Fixed ``java.util.logging`` level changes.
* Upgraded to Apache HttpClient 4.2.
* Improved performance of ``AssetServlet``.
* Added ``withBundle`` to ``ScalaService`` to enable bundle mix-ins.
* Upgraded to SLF4J 1.6.6.
* Enabled configuration-parameterized Jersey containers.
* Upgraded to Jackson Guava 1.9.1, with support for ``Optional``.
* Fixed error message in ``AssetBundle``.
* Fixed ``WebApplicationException``s being thrown by ``JerseyClient``.

.. _rel-0.4.0:

v0.4.0: May 1 2012
==================

`Complete changelog on GitHub <https://github.com/dropwizard/dropwizard/milestone/3?closed=1>`__

* Switched logging from Log4j__ to Logback__.

  * Deprecated ``Log#fatal`` methods.
  * Deprecated Log4j usage.
  * Removed Log4j JSON support.
  * Switched file logging to a time-based rotation system with optional GZIP and ZIP compression.
  * Replaced ``logging.file.filenamePattern`` with ``logging.file.currentLogFilename`` and
    ``logging.file.archivedLogFilenamePattern``.
  * Replaced ``logging.file.retainedFileCount`` with ``logging.file.archivedFileCount``.
  * Moved request logging to use a Logback-backed, time-based rotation system with optional GZIP
    and ZIP compression. ``http.requestLog`` now has ``console``, ``file``, and ``syslog``
    sections.

* Fixed validation errors for logging configuration.
* Added ``ResourceTest#addProvider(Class<?>)``.
* Added ``ETag`` and ``Last-Modified`` support to ``AssetServlet``.
* Fixed ``off`` logging levels conflicting with YAML's helpfulness.
* Improved ``Optional`` support for some JDBC drivers.
* Added ``ResourceTest#getJson()``.
* Upgraded to Jackson 1.9.6.
* Improved syslog logging.
* Fixed template paths for views.
* Upgraded to Guava 12.0.
* Added support for deserializing ``CacheBuilderSpec`` instances from JSON/YAML.
* Switched ``AssetsBundle`` and servlet to using cache builder specs.
* Switched ``CachingAuthenticator`` to using cache builder specs.
* Malformed JSON request entities now produce a ``400 Bad Request`` instead of a
  ``500 Server Error`` response.
* Added ``connectionTimeout``, ``maxConnectionsPerRoute``, and ``keepAlive`` to
  ``HttpClientConfiguration``.
* Added support for using Guava's ``HostAndPort`` in configuration properties.
* Upgraded to tomcat-dbcp 7.0.27.
* Upgraded to JDBI 2.33.2.
* Upgraded to HttpClient 4.1.3.
* Upgraded to Metrics 2.1.2.
* Upgraded to Jetty 8.1.3.
* Added SSL support.

.. __: http://logging.apache.org/log4j/1.2/
.. __: http://logback.qos.ch/


.. _rel-0.3.1:

v0.3.1: Mar 15 2012
===================

* Fixed debug logging levels for ``Log``.

.. _rel-0.3.0:

v0.3.0: Mar 13 2012
===================

`Complete changelog on GitHub <https://github.com/dropwizard/dropwizard/milestone/1?closed=1>`__

* Upgraded to JDBI 2.31.3.
* Upgraded to Jackson 1.9.5.
* Upgraded to Jetty 8.1.2. (Jetty 9 is now the experimental branch. Jetty 8 is just Jetty 7 with
  Servlet 3.0 support.)
* Dropped ``dropwizard-templates`` and added ``dropwizard-views`` instead.
* Added ``AbstractParam#getMediaType()``.
* Fixed potential encoding bug in parsing YAML files.
* Fixed a ``NullPointerException`` when getting logging levels via JMX.
* Dropped support for ``@BearerToken`` and added ``dropwizard-auth`` instead.
* Added ``@CacheControl`` for resource methods.
* Added ``AbstractService#getJson()`` for full Jackson customization.
* Fixed formatting of configuration file parsing errors.
* ``ThreadNameFilter`` is now added by default. The thread names Jetty worker threads are set to the
  method and URI of the HTTP request they are currently processing.
* Added command-line overriding of configuration parameters via system properties. For example,
  ``-Ddw.http.port=8090`` will override the configuration file to set ``http.port`` to ``8090``.
* Removed ``ManagedCommand``. It was rarely used and confusing.
* If ``http.adminPort`` is the same as ``http.port``, the admin servlet will be hosted under
  ``/admin``. This allows Dropwizard applications to be deployed to environments like Heroku, which
  require applications to open a single port.
* Added ``http.adminUsername`` and ``http.adminPassword`` to allow for Basic HTTP Authentication
  for the admin servlet.
* Upgraded to `Metrics 2.1.1 <http://metrics.codahale.com/about/release-notes/#v2-1-1-mar-13-2012>`_.

.. _rel-0.2.1:

v0.2.1: Feb 24 2012
===================

* Added ``logging.console.timeZone`` and ``logging.file.timeZone`` to control the time zone of
  the timestamps in the logs. Defaults to UTC.
* Upgraded to Jetty 7.6.1.
* Upgraded to Jersey 1.12.
* Upgraded to Guava 11.0.2.
* Upgraded to SnakeYAML 1.10.
* Upgraded to tomcat-dbcp 7.0.26.
* Upgraded to Metrics 2.0.3.

.. _rel-0.2.0:

v0.2.0: Feb 15 2012
===================

* Switched to using ``jackson-datatype-guava`` for JSON serialization/deserialization of Guava
  types.
* Use ``InstrumentedQueuedThreadPool`` from ``metrics-jetty``.
* Upgraded to Jackson 1.9.4.
* Upgraded to Jetty 7.6.0 final.
* Upgraded to tomcat-dbcp 7.0.25.
* Improved fool-proofing for ``Service`` vs. ``ScalaService``.
* Switched to using Jackson for configuration file parsing. SnakeYAML is used to parse YAML
  configuration files to a JSON intermediary form, then Jackson is used to map that to your
  ``Configuration`` subclass and its fields. Configuration files which don't end in ``.yaml`` or
  ``.yml`` are treated as JSON.
* Rewrote ``Json`` to no longer be a singleton.
* Converted ``JsonHelpers`` in ``dropwizard-testing`` to use normalized JSON strings to compare
  JSON.
* Collapsed ``DatabaseConfiguration``. It's no longer a map of connection names to configuration
  objects.
* Changed ``Database`` to use the validation query in ``DatabaseConfiguration`` for its ``#ping()``
  method.
* Changed many ``HttpConfiguration`` defaults to match Jetty's defaults.
* Upgraded to JDBI 2.31.2.
* Fixed JAR locations in the CLI usage screens.
* Upgraded to Metrics 2.0.2.
* Added support for all servlet listener types.
* Added ``Log#setLevel(Level)``.
* Added ``Service#getJerseyContainer``, which allows services to fully customize the Jersey
  container instance.
* Added the ``http.contextParameters`` configuration parameter.

.. _rel-0.1.3:

v0.1.3: Jan 19 2012
===================

* Upgraded to Guava 11.0.1.
* Fixed logging in ``ServerCommand``. For the last time.
* Switched to using the instrumented connectors from ``metrics-jetty``. This allows for much
  lower-level metrics about your service, including whether or not your thread pools are overloaded.
* Added FindBugs to the build process.
* Added ``ResourceTest`` to ``dropwizard-testing``, which uses the Jersey Test Framework to provide
  full testing of resources.
* Upgraded to Jetty 7.6.0.RC4.
* Decoupled URIs and resource paths in ``AssetServlet`` and ``AssetsBundle``.
* Added ``rootPath`` to ``Configuration``. It allows you to serve Jersey assets off a specific path
  (e.g., ``/resources/*`` vs ``/*``).
* ``AssetServlet`` now looks for ``index.htm`` when handling requests for the root URI.
* Upgraded to Metrics 2.0.0-RC0.

.. _rel-0.1.2:

v0.1.2: Jan 07 2012
===================

* All Jersey resource methods annotated with ``@Timed``, ``@Metered``, or ``@ExceptionMetered`` are
  now instrumented via ``metrics-jersey``.
* Now licensed under Apache License 2.0.
* Upgraded to Jetty 7.6.0.RC3.
* Upgraded to Metrics 2.0.0-BETA19.
* Fixed logging in ``ServerCommand``.
* Made ``ServerCommand#run()`` non-``final``.


.. _rel-0.1.1:

v0.1.1: Dec 28 2011
===================

* Fixed ``ManagedCommand`` to provide access to the ``Environment``, among other things.
* Made ``JerseyClient``'s thread pool managed.
* Improved ease of use for ``Duration`` and ``Size`` configuration parameters.
* Upgraded to Mockito 1.9.0.
* Upgraded to Jetty 7.6.0.RC2.
* Removed single-arg constructors for ``ConfiguredCommand``.
* Added ``Log``, a simple front-end for logging.

.. _rel-0.1.0:


v0.1.0: Dec 21 2011
===================

* Initial release
