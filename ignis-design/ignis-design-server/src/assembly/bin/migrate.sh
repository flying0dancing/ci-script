#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

APP_HOME="${DIR}/.."
FLYWAY_HOME="${APP_HOME}/lib/flyway-5.0.7"
log="${APP_HOME}/logs/migrate.log"

echo "Running migrations"
echo "  logging into ${log}"
"${FLYWAY_HOME}"/flyway migrate >> "${log}" 2>&1
