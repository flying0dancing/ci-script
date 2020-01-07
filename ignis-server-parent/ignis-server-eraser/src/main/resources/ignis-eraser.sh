#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

IGNIS_ERASER_HOME="${DIR}"

JAR_FILE="ignis-server-eraser-exec.jar"
JAVA_OPTS="-Xmx512m -Dspring.config.location=${IGNIS_ERASER_HOME}/application.properties"

ignisEraserScript="java -jar ${JAR_FILE} ${JAVA_OPTS}"

echo "Starting Ignis Eraser..."
${ignisEraserScript}
