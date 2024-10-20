# Dropwizard Release Process

This document describes the technical aspects of the Dropwizard release process.

## Who is responsible for a release?

A release can be performed by any member of the Dropwizard project with the commit rights to the repository.
The person responsible for the release MUST seek an approval for it in the "dropwizard-committers"
mailing list beforehand.

## Performing a release

* Add the release notes to `docs/source/about/release-notes.rst` and set the release date;
* Add the link to the documentation to `docs/source/about/docs-index.rst` if it's a new major or minor version;
* Run `./mvnw release:prepare` in the master branch;
  * This will set the version of all Maven submodules and run the tests one last time before pushing the tagged commit which will trigger the release build on Travis CI;
  * Observe that all tests passed, there is no build errors and the corresponding git tag was created;
* Create the release notes in GitHub: [Creating releases](https://help.github.com/en/articles/creating-releases);
* The release will typically be available in the Maven Central repository 3-4 hours after the artifacts were pushed to Sonatype OSSRH (this may vary depending on the
indexing process).

## Making an announcement

After the release has been uploaded to the repository and the documentation has been updated, a release announcement
should be published in the "dropwizard-user" and "dropwizard-dev" mailing lists. There is no formal structure for
the announcement, but generally it should contain a short description of the release, the artifact coordinates in the
Maven Central, a link to documentation, a link to the release notes, and a link to the bug tracker.
