v0.1.2: TBD
===================

* All Jersey resource methods annotated with `@Timed`, `@Metered`, or `@ExceptionMetered` are now
  instrumented via `metrics-jersey`.
* Now licensed under Apache License 2.0.


v0.1.1: Dec 28 2011
===================

* Fixed `ManagedCommand` to provide access to the `Environment`, among other things.
* Made `JerseyClient`'s thread pool managed.
* Improved ease of use for `Duration` and `Size` configuration parameters.
* Upgraded to Mockito 1.9.0.
* Upgraded to Jetty 7.6.0.RC2.
* Removed single-arg constructors for `ConfiguredCommand`.
* Added `Log`, a simple front-end for logging.


v0.1.0: Dec 21 2011
===================

* Initial release
