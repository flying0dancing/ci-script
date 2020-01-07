#!/usr/bin/env bash

mvn clean install -pl  ignis-design/ignis-design-server -am -P skipTests

./flyway-design-studio h2 migrate
