#!/usr/bin/env bash

mvn clean install -pl \
\!ignis-ui,\
\!ignis-design\\ignis-design-ui,\
\!ignis-server-installer\\ignis-server-izpack-installer,\
\!ignis-platform-tools-installer\\ignis-platform-tools-izpack-installer,\
\!ignis-functional-test,\
\!ignis-phoenix-client \
$@