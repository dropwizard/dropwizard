#!/bin/bash
set -e
set -uxo pipefail

if [[ "${TRAVIS_JDK_VERSION}" == "openjdk8" ]]; then
    ./mvnw -V -B -ff -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -q coveralls:report
    exit $?
fi
