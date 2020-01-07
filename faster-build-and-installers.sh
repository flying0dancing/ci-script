#!/usr/bin/env bash

mvn -T1C clean install -pl !ignis-server-installer/ignis-server-izpack-installer \
  -pl !ignis-platform-tools-installer/ignis-platform-tools-izpack-installer \
  -P skipFunctionalTests \
  $@

mvn clean verify -pl ignis-server-installer/ignis-server-izpack-installer \
  -pl ignis-platform-tools-installer/ignis-platform-tools-izpack-installer \
  -pl ignis-functional-test -P local-functional-tests