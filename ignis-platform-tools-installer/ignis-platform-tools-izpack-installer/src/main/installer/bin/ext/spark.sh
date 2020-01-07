#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

. "${DIR}"/../../conf/env.sh

usage() {
   echo "Usage: spark.sh start|stop|status"
}

daemon() {
   commandToRun=$1
   if [[ "${DISTRIBUTED}" == "true" ]]; then
      ssh ${SSH_OPTS} ${HDFS_NAME_NODE_HOST} bash -c "'
         . ${APP_HOME}/conf/env.sh
         ${HADOOP_HOME}/bin/hdfs dfs -mkdir -p /spark/app-logs
         ${HADOOP_HOME}/bin/hdfs dfs -rm -r .sparkStaging > /dev/null 2>&1
      '" 2>&1 | sed "s/^/${HDFS_NAME_NODE_HOST}: /"

      ssh ${SSH_OPTS} ${SPARK_HISTORY_SERVER_HOST} bash -c "'
         . ${APP_HOME}/conf/env.sh
         ${SPARK_HOME}/sbin/${commandToRun}-history-server.sh
      '" 2>&1 | sed "s/^/${SPARK_HISTORY_SERVER_HOST}: /"
   else
      ${HADOOP_HOME}/bin/hdfs dfs -mkdir -p /spark/app-logs
      ${HADOOP_HOME}/bin/hdfs dfs -rm -r .sparkStaging > /dev/null 2>&1
      ${SPARK_HOME}/sbin/${commandToRun}-history-server.sh
   fi
}

case "$1" in
  start)
     daemon start;
     ;;
  stop)
     daemon stop;
     ;;
  status)
     ${APP_HOME}/bin/ext/check-services.sh -m spark;
     ;;
  *)
     usage;
     ;;
esac