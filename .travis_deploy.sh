#!/bin/bash
./mvnw -B -q deploy --settings maven_deploy_settings.xml -Dmaven.test.skip=true -Dfindbugs.skip=true
