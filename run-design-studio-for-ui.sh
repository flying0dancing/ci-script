#!/usr/bin/env bash

WORKING_DIR=$PWD
CONFIG_LOCATION="$WORKING_DIR/ignis-design/ignis-design-server/src/test/resources/"
SPRING_PROFILE="dev"
echo "Running design studio with profile $SPRING_PROFILE and config in $CONFIG_LOCATION"

mvn -f ignis-design/ignis-design-server/pom.xml spring-boot:run -Dspring.profiles.active="$SPRING_PROFILE" -Dspring.config.location="$CONFIG_LOCATION"
