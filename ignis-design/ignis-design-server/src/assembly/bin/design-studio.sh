#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

DESIGN_STUDIO_HOME="${DIR}/.."
DESIGN_STUDIO_LOG_DIR="${DESIGN_STUDIO_HOME}/logs"

if [[ "x${DESIGN_STUDIO_PID_DIR}" == "x" ]]; then
  DESIGN_STUDIO_PID_DIR=/tmp
fi

PID="${DESIGN_STUDIO_PID_DIR}/design-studio-${USER}.pid"
LOG="${DESIGN_STUDIO_LOG_DIR}"/design-studio.log

usage() {
  echo "Usage: design-studio.sh {start|stop|restart|status}"
}

start() {
  if [[ -f "${PID}" ]]; then
    if kill -0 $(cat ${PID}) > /dev/null 2>&1; then
      echo "Design Studio running as process $(cat ${PID}). Stop it first."
      exit 1
    fi
   fi

   JAR_FILE=$(find "${DESIGN_STUDIO_HOME}/lib" -maxdepth 1  -name "ignis-design-server-*.jar")
   JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
   JAVA_OPTS="$JAVA_OPTS -Xmx512m -Dspring.config.location=${DESIGN_STUDIO_HOME}/conf/application.properties"

   designStudioScript="java ${JAVA_OPTS} -jar ${JAR_FILE}"

   echo "Starting Design Studio. Logging to ${LOG}"
   mkdir -p "${DESIGN_STUDIO_LOG_DIR}"
   nohup   ${designStudioScript} > "${DESIGN_STUDIO_LOG_DIR}"/design-studio.log 2>&1 &
   echo $! > ${PID}
}

status() {
  if [[ -f "${PID}" ]]; then
    TARGET_PID=$(cat ${PID})
    if ps -p "${TARGET_PID}" > /dev/null
    then
       echo "Design Studio | Up | Pid ${TARGET_PID}"
    else
       echo "Design Studio | Down"
    fi
  else
    echo "Design Studio | Up | Pid ${TARGET_PID}"
  fi
}

stop() {
  if [[ -f ${PID} ]]; then
    TARGET_PID=$(cat ${PID})
    if kill -0 ${TARGET_PID} > /dev/null 2>&1; then
        echo "Stopping Design Studio pid ${TARGET_PID}"
        kill ${TARGET_PID}
        sleep 1
        if kill -0 ${TARGET_PID} > /dev/null 2>&1; then
          echo "Design Studio did not stop gracefully after 1 seconds: killing with kill -9"
          kill -9 ${TARGET_PID}
        fi
    else
        echo "No Design Studio to stop"
    fi
    rm -f ${PID}
  else
    echo "No Design Studio to stop"
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