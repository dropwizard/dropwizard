#!/bin/bash
set -e
set -uxo pipefail

if [[ "${TRAVIS_JDK_VERSION}" == "openjdk8" ]]; then
    ./mvnw test jacoco:report coveralls:report -P code-coverage -B -q
    exit $?
fi