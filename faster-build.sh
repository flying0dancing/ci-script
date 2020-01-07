#!/usr/bin/env bash

if [ ! -f ignis-server-installer/ignis-server-izpack-installer/target/installer/ignis-server-1.0.0-SNAPSHOT/lib/__spark_libs__.zip ]; then
    echo "Spark libs not found, this can be due to ignis-server installer not being built"
    exit 1
fi

mvn -T1C clean install -pl !ignis-server-installer/ignis-server-izpack-installer \
  -pl !ignis-platform-tools-installer/ignis-platform-tools-izpack-installer \
  -P skipFunctionalTests \
  $@

mvn package -pl ignis-server -P skipTests

mvn clean verify -pl ignis-functional-test -P local-functional-tests