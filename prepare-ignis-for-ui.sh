#!/usr/bin/env bash

mvn clean install -pl  ignis-server-parent/ignis-server -am -P skipTests

./flyway-ignis h2 migrate
