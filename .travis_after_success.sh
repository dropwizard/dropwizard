#!/bin/bash

if [[ "${TRAVIS_JDK_VERSION}" != "openjdk8" ]]; then
    echo "Skipping after_success actions for JDK version \"${TRAVIS_JDK_VERSION}\""
    exit
fi

./mvnw test jacoco:report coveralls:report -P code-coverage -B -q
