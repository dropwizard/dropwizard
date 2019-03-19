#!/bin/bash
set -e
set -uxo pipefail

# Decrypt and import signing key
openssl aes-256-cbc -K $encrypted_9f91a893d467_key -iv $encrypted_9f91a893d467_iv -in ci/pubring.gpg.enc -out ci/pubring.gpg -d
openssl aes-256-cbc -K $encrypted_9f91a893d468_key -iv $encrypted_9f91a893d468_iv -in ci/secring.gpg.enc -out ci/secring.gpg -d

./mvnw -B deploy --settings 'ci/settings.xml' -DperformRelease=true -Dmaven.test.skip=true

# Documentation
./mvnw -B site site:stage --settings 'ci/settings.xml' -Dmaven.test.skip=true

DOCS_VERSION=$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout)
mkdir gh-pages
mv target/staging gh-pages/"${DOCS_VERSION}"
