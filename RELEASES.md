# Dropwizard Release Process

This document describes the technical aspects of the Dropwizard release process.

## Who is responsible for a release?

A release can be performed by any member of the Dropwizard project with the commit rights to the repository.
The person responsible for the release MUST seek an approval for it in the `dropwizard-committers`
mailing list beforehand.

## Prerequisites for a new maintainer

* Register an account in the Sonatype's [Jira](https://issues.sonatype.org)
* Create a Jira ticket asking to obtain the right permissions to push artifacts belonging
to the `io.dropwizard` group.
* Write an email to @joschi or @carlo-rtr, so they can approve the request in the Jira ticket.
* Generate a gpg key with an email linked to your Github/Sonatype account
`gpg --gen-key`
* Distribute the key
`gpg --keyserver hkp://pgp.mit.edu --send-keys XXXX` # XXXX - the code of the generated key
* Put your Sonatype credentials to your Maven settings file (`~/.m2/settings.xml`)
```xml
<server>
    <id>sonatype-nexus-staging</id>
    <username>alice</username>
    <password>correcthorsebatterystaple</password>
</server>
```

## Performing a release

* Edit `docs/source/about/release-notes.rst` and set the release date;
* Edit `docs/source/about/docs-index.rst` and set the link to the release docs;
* Run `mvn release:prepare` in the master branch;
* Observe that all tests passed, there is no build errors and the corresponding git tag was created;
* Run `mvn release:perform -Dgoals=deploy`;
* Login at Sonatype's OSS Nexus `https://oss.sonatype.org`;
* Click "Staging repositories";
* Find the `io.dropwizard` group, and click on the close button on the top bar;
* Wait while the Nexus server will perform some checks that artifacts were uploaded correctly;
* Click the refresh button;
* Select `io.dropwizard` again, and hit the release button on the top bar;
* Normally the release will be available in the Maven Central repository in 3-4 hours (this may vary depending on the
indexing process).
* Publish the documentation. Run the script `prepare_docs.sh` and verify it completed successfully.
Push the changes to the remote `gh-pages` branch.

## Making an announcement

After the release has been uploaded to the repository and the documentation has been updated, a release announcement
should be published in the `dropwizard-user` and `dropwizard-dev` mailing lists. There is no formal structure for
the announcement, but generally it should contain a short description of the release, the artifact coordinates in the
Maven Central, a link to documentation, a link to the release notes, and a link to the bug tracker.
