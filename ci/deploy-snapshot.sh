#!/bin/bash
set -e
set -uxo pipefail

./mvnw -V -B -ff -XX:+TieredCompilation -XX:TieredStopAtLevel=1 deploy --settings 'ci/settings.xml' -Dmaven.test.skip=true
