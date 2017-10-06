#!/bin/bash

if [[ "${TRAVIS_JDK_VERSION}" != "oraclejdk8" ]]; then
    echo "Skipping after_success actions for JDK version \"${TRAVIS_JDK_VERSION}\""
    exit
fi

./mvnw -B cobertura:cobertura coveralls:report

if [[ -n ${TRAVIS_TAG} ]]; then
    echo "Skipping deployment for tag \"${TRAVIS_TAG}\""
    exit
fi

if [[ ${TRAVIS_BRANCH} != 'master' ]]; then
    echo "Skipping deployment for branch \"${TRAVIS_BRANCH}\""
    exit
fi

if [[ "$TRAVIS_PULL_REQUEST" = "true" ]]; then
    echo "Skipping deployment for pull request"
    exit
fi

./mvnw -B deploy --settings maven_deploy_settings.xml -Dmaven.test.skip=true -Dfindbugs.skip=true
