#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

IGNIS_ERASER_HOME="${DIR}/.."
IGNIS_ERASER_LOG_DIR="${IGNIS_ERASER_HOME}/logs"

PID="${IGNIS_ERASER_HOME}/ignis-eraser-${USER}.pid"
LOG="${IGNIS_ERASER_LOG_DIR}"/ignis-eraser.log

usage() {
  echo "Usage: ignis-eraser.sh {start|stop|restart}"
}

start() {
  if [[ -f "${PID}" ]]; then
    if kill -0 $(cat ${PID}) > /dev/null 2>&1; then
      echo "Ignis Eraser running as process $(cat ${PID}). Stop it first."
      exit 1
    fi
   fi

   JAR_FILE=$(find "${IGNIS_ERASER_HOME}/lib" -maxdepth 1  -name "ignis-server-eraser-exec.jar")
#   JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=9005"
   JAVA_OPTS="$JAVA_OPTS -Xmx512m -Dspring.config.location=${IGNIS_ERASER_HOME}/conf/eraser.properties"

   ignisEraserScript="java ${JAVA_OPTS} -jar ${JAR_FILE}"

   echo "Starting Ignis Eraser. Logging to ${LOG}"
   mkdir -p "${IGNIS_ERASER_LOG_DIR}"
   nohup   ${ignisEraserScript} > "${IGNIS_ERASER_LOG_DIR}"/ignis-eraser.log 2>&1 &
   echo $! > ${PID}
}

stop() {
  if [[ -f ${PID} ]]; then
    TARGET_PID=$(cat ${PID})
    if kill -0 ${TARGET_PID} > /dev/null 2>&1; then
        echo "Stopping Ignis Eraser pid ${TARGET_PID}"
        kill ${TARGET_PID}
        sleep 1
        if kill -0 ${TARGET_PID} > /dev/null 2>&1; then
          echo "Ignis Eraser did not stop gracefully after 1 seconds: killing with kill -9"
          kill -9 ${TARGET_PID}
        fi
    else
        echo "No Ignis Eraser to stop"
    fi
    rm -f ${PID}
  else
    echo "No Ignis Eraser to stop"
  fi
}

case "$1" in
  start)
        start;
        ;;
  restart)
        stop;
        start;
        ;;
  stop)
        stop
        ;;
  *)
        usage
        ;;
esac