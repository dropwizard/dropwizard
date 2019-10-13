#!/bin/bash
set -e
set -uxo pipefail

./mvnw -V -B -ff deploy --settings 'ci/settings.xml' -Dmaven.test.skip=true
