#!/bin/bash

if [[ "${TRAVIS_JDK_VERSION}" != "oraclejdk8" ]]; then
    echo "Skipping after_success actions for JDK version ${TRAVIS_JDK_VERSION}"
    exit
fi

mvn -B cobertura:cobertura coveralls:report

# Do not deploy SNAPSHOT artifacts for PRs
if [[ "$TRAVIS_PULL_REQUEST" = "false" ]]; then
    mvn -B deploy --settings maven_deploy_settings.xml -Dmaven.test.skip=true -Dmaven.javadoc.skip=true -Dsource.skip=true -Dfindbugs.skip=true
else
    echo 'Pull request, skipping deploy.'
fi
