#!/bin/bash
# Only update coveralls after the JDK8 build.
if [[ "${TRAVIS_JDK_VERSION}" == "oraclejdk8" ]]; then
    echo "Running Coveralls for JDK version: ${TRAVIS_JDK_VERSION}"
    mvn cobertura:cobertura coveralls:report
else
    echo "Skipping coveralls for JDK version: ${TRAVIS_JDK_VERSION}"
fi

# Only deploy SNAPSHOT artifacts after the JDK8 build
if [[ "$TRAVIS_PULL_REQUEST" = "false" -a "${TRAVIS_JDK_VERSION}" == "oraclejdk8" ]]; then
    mvn deploy --settings maven_deploy_settings.xml
else
    echo 'Pull request, skipping deploy.'
fi
