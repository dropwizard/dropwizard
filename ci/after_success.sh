#!/bin/bash
set -e
set -uxo pipefail

if [[ "${TRAVIS_JDK_VERSION}" == "openjdk8" ]]; then
    ./mvnw coveralls:report -B -q
    exit $?
fi