#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

. "${DIR}"/../../conf/env.sh

historyServer() {
  commandToRun=$1
  ssh ${SSH_OPTS} "${JOB_HISTORY_SERVER_HOST}" bash -c "'
    . ${APP_HOME}/conf/env.sh
    ${HADOOP_PREFIX}/sbin/mr-jobhistory-daemon.sh ${commandToRun} historyserver
  '" 2>&1 | sed "s/^/${JOB_HISTORY_SERVER_HOST}: /"
}

startNameNodeDistributed() {
  if [[ "$1" == "upgrade" ]]; then
    "${HADOOP_PREFIX}/sbin/hadoop-daemons.sh" --hostnames "${HDFS_NAME_NODE_HOST}"  start namenode -rollingUpgrade started
  elif [[ "$1" == "rollback" ]]; then
    "${HADOOP_PREFIX}/sbin/hadoop-daemons.sh" --hostnames "${HDFS_NAME_NODE_HOST}"  start namenode -rollingUpgrade rollback
  else
    "${HADOOP_PREFIX}/sbin/hadoop-daemons.sh" --hostnames "${HDFS_NAME_NODE_HOST}"  start namenode
  fi
}

startNameNodeStandalone() {
  if [[ "$1" == "upgrade" ]]; then
    "${HADOOP_PREFIX}/sbin/hadoop-daemon.sh" start namenode -rollingUpgrade started
  elif [[ "$1" == "rollback" ]]; then
    "${HADOOP_PREFIX}/sbin/hadoop-daemon.sh" start namenode -rollingUpgrade rollback
  else
    "${HADOOP_PREFIX}/sbin/hadoop-daemon.sh" start namenode
  fi
}

startDataNodesDistributed() {
  if [[ "$1" == "rollback" ]]; then
    "${HADOOP_PREFIX}/sbin/hadoop-daemons.sh" --hostnames "${HDFS_DATA_NODE_HOSTS}" start datanode -rollback
  else
    "${HADOOP_PREFIX}/sbin/hadoop-daemons.sh" --hostnames "${HDFS_DATA_NODE_HOSTS}" start datanode
  fi
}

startDataNodesStandalone() {
  if [[ "$1" == "rollback" ]]; then
    "${HADOOP_PREFIX}/sbin/hadoop-daemon.sh" start datanode -rollback
  else
    "${HADOOP_PREFIX}/sbin/hadoop-daemon.sh" start datanode
  fi
}

start() {
  if [[ "${DISTRIBUTED}" == "true" ]]; then
    startNameNodeDistributed $1

    ssh ${SSH_OPTS} "${HDFS_NAME_NODE_HOST}"  bash -c "'
      ${HADOOP_PREFIX}/bin/hdfs dfsadmin -safemode leave
    '" 2>&1 | sed "s/^/${HDFS_NAME_NODE_HOST}: /"
    "${HADOOP_PREFIX}/sbin/hadoop-daemons.sh" --hostnames "${HDFS_SNAME_NODE_HOST}" start secondarynamenode
    "${HADOOP_PREFIX}/sbin/yarn-daemons.sh" --hostnames "${YARN_RESOURCE_MANAGER_HOST}" start resourcemanager
    historyServer start

    "${APP_HOME}/bin/ext/check-services.sh" -m  hdfsMaster -c

    startDataNodesDistributed $1

    "${APP_HOME}/bin/ext/check-services.sh" -m yarnMaster -c
    "${HADOOP_PREFIX}/sbin/yarn-daemons.sh" --hostnames "${YARN_NODE_MANAGER_HOSTS}" start nodemanager
  else
    startNameNodeStandalone $1

    "${HADOOP_PREFIX}/bin/hdfs" dfsadmin -safemode leave
    "${HADOOP_PREFIX}/sbin/hadoop-daemon.sh" start secondarynamenode
    "${HADOOP_PREFIX}/sbin/yarn-daemon.sh" start resourcemanager
    "${HADOOP_PREFIX}/sbin/mr-jobhistory-daemon.sh" start historyserver

    startDataNodesStandalone $1

    "${HADOOP_PREFIX}/sbin/yarn-daemon.sh" start nodemanager
  fi
}

stop() {
  if [[ "${DISTRIBUTED}" == "true" ]]; then
    "${HADOOP_PREFIX}/sbin/yarn-daemons.sh" --hostnames "${YARN_NODE_MANAGER_HOSTS}" stop nodemanager
    "${HADOOP_PREFIX}/sbin/hadoop-daemons.sh" --hostnames "${HDFS_DATA_NODE_HOSTS}" stop datanode
    historyServer stop
    "${HADOOP_PREFIX}/sbin/yarn-daemons.sh" --hostnames "${YARN_RESOURCE_MANAGER_HOST}" stop resourcemanager
    "${HADOOP_PREFIX}/sbin/hadoop-daemons.sh" --hostnames "${HDFS_SNAME_NODE_HOST}" stop secondarynamenode
    "${HADOOP_PREFIX}/sbin/hadoop-daemons.sh" --hostnames "${HDFS_NAME_NODE_HOST}"  stop namenode
  else
    "${HADOOP_PREFIX}/sbin/yarn-daemon.sh" stop nodemanager
    "${HADOOP_PREFIX}/sbin/hadoop-daemon.sh" stop datanode
    "${HADOOP_PREFIX}/sbin/mr-jobhistory-daemon.sh" stop historyserver
    "${HADOOP_PREFIX}/sbin/yarn-daemon.sh" stop resourcemanager
    "${HADOOP_PREFIX}/sbin/hadoop-daemon.sh" stop secondarynamenode
    "${HADOOP_PREFIX}/sbin/hadoop-daemon.sh" stop namenode
  fi
}

usage() {
  echo "Usage: hadoop.sh start [--upgrade|--rollback]|stop|status"
}

case "$1" in
  start)
    case "$2" in
      --upgrade)
        start upgrade;
        ;;
      --rollback)
        start rollback;
        ;;
      *)
        start;
        ;;
    esac
    ;;
  stop)
    stop;
    ;;
  status)
    "${APP_HOME}/bin/ext/check-services.sh" -m hadoop;
    ;;
  *)
    usage;
    ;;
esac