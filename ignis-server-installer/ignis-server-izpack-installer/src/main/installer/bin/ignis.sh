#!/usr/bin/env bash

FLYWAY_HOME="%{FLYWAY_HOME}"
IGNIS_HOME="%{IGNIS_HOME}"

PID_FILE="%{tmp.path}/ignis-server.pid"
LOG="%{IGNIS_LOG_PATH}"
DATASET_PATH="%{dataset.source.location.localPath}"

JAVA="%{JAVA_HOME}"/bin/java
JAR_FILE="%{IGNIS_SERVER_JAR}"
#JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8787"
JAVA_OPTS="$JAVA_OPTS -Xmx%{java.mem.max} -Dspring.config.location=%{IGNIS_PROPS_PATH}"
STOP_TIMEOUT_SEC=${STOP_TIMEOUT_SEC:-5}

usage() {
  echo "Usage: ignis.sh {start|stop|restart|status}"
}

start() {
  if [[ -f "${PID_FILE}" ]]; then
    if kill -0 $(cat "${PID_FILE}") > /dev/null 2>&1; then
      echo "Cannot start Ignis Server as it's already running with pid $(cat "${PID_FILE}")"
      exit 1
    fi
  fi
  flywayScript="${FLYWAY_HOME}/flyway migrate"
  ignisScript="${JAVA} ${JAVA_OPTS} -jar ${JAR_FILE}"

  echo "Starting Ignis Server"
  echo "  Logging to ${LOG}"
  mkdir -p "${DATASET_PATH}" || true
  mkdir -p "$(dirname ${LOG})" || true

  echo "$(date --iso-8601=seconds) INFO Running database migrations" >> "${LOG}"
  ${flywayScript} >> "${LOG}"
  echo >> "${LOG}"
  nohup ${ignisScript} > /dev/null 2>&1 &
  echo $! > "${PID_FILE}"
}

stop() {
  if [[ -f "${PID_FILE}" ]]; then
    TARGET_PID=$(cat "${PID_FILE}")
    if kill -0 "${TARGET_PID}" > /dev/null 2>&1; then
        echo "Stopping Ignis Server with pid ${TARGET_PID}. Waiting ${STOP_TIMEOUT_SEC} seconds..."
        kill "${TARGET_PID}"
        sleep ${STOP_TIMEOUT_SEC}
        if kill -0 "${TARGET_PID}" > /dev/null 2>&1; then
          echo "Force stopping Ignis Server"
          kill -9 "${TARGET_PID}"
        fi
    else
        echo "Ignis Server is not running"
    fi
    rm -f "${PID_FILE}"
  else
    echo "Ignis Server is not running"
  fi
}

status() {
  if [[ -f "${PID_FILE}" ]]; then
    TARGET_PID=$(cat "${PID_FILE}")
    if ps -p "${TARGET_PID}" > /dev/null
    then
       echo "Ignis Server is running with pid "${TARGET_PID}""
    else
       echo "Ignis Server with pid "${TARGET_PID}" is not running anymore"
    fi
  else
    echo "Ignis Server is not running"
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
  status)
        status
        ;;
  *)
        usage
        ;;
esac