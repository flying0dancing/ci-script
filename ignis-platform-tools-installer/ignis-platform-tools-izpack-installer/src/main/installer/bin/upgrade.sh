#!/usr/bin/env bash

# Script to upgrade from pre-2.2.1 version of platform-tools (Hadoop version 2.7.3) to 2.2.1 (Hadoop version 2.8.3)

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

ACTION=$1

PREVIOUS_PLATFORM_TOOLS_DIR=$2
PREVIOUS_HADOOP_VERSION=2.7.3
PREVIOUS_HADOOP_DIR=${PREVIOUS_PLATFORM_TOOLS_DIR}/hadoop-${PREVIOUS_HADOOP_VERSION}

UPGRADED_PLATFORM_TOOLS_DIR=${DIR}/..
UPGRADED_HADOOP_VERSION=2.8.3
UPGRADED_HADOOP_DIR=${UPGRADED_PLATFORM_TOOLS_DIR}/hadoop-${UPGRADED_HADOOP_VERSION}

usage() {
  echo "Usage: upgrade.sh (upgrade|finalize|rollback) PREVIOUS_PLATFORM_TOOLS_DIR"
}

logMessage() {
	echo -e "upgrade.sh  $(date +'%Y/%m/%d %H:%M:%S')\t$@"
}

logError() {
	logMessage $@ 1>&2
}

retry() {
  ATTEMPTS=$1

  if [[ ! "$ATTEMPTS" -gt 0 ]]; then
    logError "First argument should be greater than zero"
    return 1
  fi

  logMessage "$ATTEMPTS attempts remaining"

  ${@:2}
  RESULT=$?

  if [[ "$RESULT" -eq 0 ]]; then
    return ${RESULT}
  elif [[ "$ATTEMPTS" -gt 1 ]]; then
    sleep 5
    retry $((ATTEMPTS - 1)) ${@:2}
  else
    logError "Failed all attempts"
    return 1
  fi
}

checkService() {
  service=$1
  status=$2
  directory=$3

  logMessage "Checking service $service is $status"
  if [[ "$status" == "down" ]]; then
    ${directory}/bin/daemon.sh status ${service} | grep '|Up\||Down' | grep -qv '|Up'
  elif [[ "$status" == "up" ]]; then
    ${directory}/bin/daemon.sh status ${service} | grep '|Up\||Down' | grep -qv '|Down'
  else
    logError "Invalid status $status"
    return 1
  fi
}

startPreviousNameNode() {
  logMessage "Starting previous Hadoop version $PREVIOUS_HADOOP_VERSION name node"

  . ${PREVIOUS_PLATFORM_TOOLS_DIR}/conf/env.sh

  if [[ "${DISTRIBUTED}" == "true" ]]; then
    "${PREVIOUS_HADOOP_DIR}/sbin/hadoop-daemons.sh" --hostnames "${HDFS_NAME_NODE_HOST}"  start namenode $@
    "${PREVIOUS_HADOOP_DIR}/sbin/hadoop-daemons.sh" --hostnames "${HDFS_SNAME_NODE_HOST}" start secondarynamenode
  else
    "${PREVIOUS_HADOOP_DIR}/sbin/hadoop-daemon.sh" start namenode $@
    "${PREVIOUS_HADOOP_DIR}/sbin/hadoop-daemon.sh" start secondarynamenode
  fi

  retry 5 ${PREVIOUS_PLATFORM_TOOLS_DIR}/bin/daemon.sh status hadoop | grep 'NameNode' | grep -q '|Up'

  if [[ $? -eq 0 ]]; then
    logMessage "Hadoop version $PREVIOUS_HADOOP_VERSION name node started"
  else
    logError "Failed to start previous version $PREVIOUS_HADOOP_VERSION name node"
    exit 1
  fi
}

startPreviousDataNodes() {
  logMessage "Starting previous Hadoop version $PREVIOUS_HADOOP_VERSION data nodes"

  . ${PREVIOUS_PLATFORM_TOOLS_DIR}/conf/env.sh

  if [[ "${DISTRIBUTED}" == "true" ]]; then
    "${PREVIOUS_HADOOP_DIR}/sbin/hadoop-daemons.sh" --hostnames "${HDFS_DATA_NODE_HOSTS}" start datanode $@
  else
    "${PREVIOUS_HADOOP_DIR}/sbin/hadoop-daemon.sh" start datanode $@
  fi

  retry 5 ${PREVIOUS_PLATFORM_TOOLS_DIR}/bin/daemon.sh status hadoop | grep 'DataNode' | grep -q '|Up'

  if [[ $? -eq 0 ]]; then
    logMessage "Hadoop version $PREVIOUS_HADOOP_VERSION data nodes started"
  else
    logError "Failed to start previous version $PREVIOUS_HADOOP_VERSION data nodes"
    exit 1
  fi
}

stopPreviousService() {
  logMessage "Stopping previous service: $1"

  ${PREVIOUS_PLATFORM_TOOLS_DIR}/bin/daemon.sh stop $1
  retry 5 checkService $1 down ${PREVIOUS_PLATFORM_TOOLS_DIR}

  if [[ $? -eq 0 ]]; then
    logMessage "Previous service $1 is stopped"
  else
    logError "Failed to stop previous service: $1"
    exit 1
  fi
}

startUpgradedService() {
  logMessage "Starting upgraded service: $1"

  ${UPGRADED_PLATFORM_TOOLS_DIR}/bin/daemon.sh start $1 ${@:2}
  retry 5 checkService $1 up ${UPGRADED_PLATFORM_TOOLS_DIR}

  if [[ $? -eq 0 ]]; then
    logMessage "Upgraded service $1 is started"
  else
    logError "Failed to start upgraded service: $1"
    exit 1
  fi
}

stopUpgradedService() {
  logMessage "Stopping upgraded service: $1"

  ${UPGRADED_PLATFORM_TOOLS_DIR}/bin/daemon.sh stop $1
  retry 5 checkService $1 down ${UPGRADED_PLATFORM_TOOLS_DIR}

  if [[ $? -eq 0 ]]; then
    logMessage "Upgraded service $1 is stopped"
  else
    logError "Failed to stop upgraded service: $1"
    exit 1
  fi
}

rollingUpgradeInProgress() {
  logMessage "Checking if rolling upgrade in progress"
  ${PREVIOUS_HADOOP_DIR}/bin/hdfs dfsadmin -rollingUpgrade query | grep -qi 'Proceed with rolling upgrade'
}

prepareUpgrade() {
  logMessage "Checking Hadoop $PREVIOUS_HADOOP_VERSION is started"
  checkService hadoop up ${PREVIOUS_PLATFORM_TOOLS_DIR}
  if [[ ! $? -eq 0 ]]; then
    logError "Hadoop $PREVIOUS_HADOOP_VERSION not started, unable to proceed with upgrade"
    exit 1
  fi

  if rollingUpgradeInProgress; then
    logError "Rolling upgrade already in progress, taking no further action"
    exit 1
  else
    stopPreviousService spark
    stopPreviousService phoenix
    stopPreviousService hbase

    logMessage "Preparing upgrade"
    ${PREVIOUS_HADOOP_DIR}/bin/hdfs dfsadmin -safemode enter
    ${PREVIOUS_HADOOP_DIR}/bin/hdfs dfsadmin -rollingUpgrade prepare

    if retry 5 rollingUpgradeInProgress; then
      logMessage "Rolling upgrade started, stopping previous Hadoop version $PREVIOUS_HADOOP_VERSION"
      stopPreviousService hadoop
      stopPreviousService zookeeper
    else
      logError "Unable to prepare rolling upgrade"
      exit 1
    fi
  fi
}

startUpgrade() {
  logMessage "Starting upgraded platform-tool services"

  startUpgradedService zookeeper
  startUpgradedService hadoop --upgrade
  startUpgradedService hbase
  startUpgradedService phoenix
  startUpgradedService spark
}

finalizeUpgrade() {
  logMessage "After finalizing it will be impossible to rollback to the previous version of Hadoop"
  read -p "Are you sure? (yes/no) " confirm

  if [[ ! "$confirm" == "yes" ]]; then
    logMessage "Cancelling"
    exit
  fi

  logMessage "Checking Hadoop $UPGRADED_HADOOP_VERSION is started"
  checkService hadoop up ${UPGRADED_PLATFORM_TOOLS_DIR}
  if [[ ! $? -eq 0 ]]; then
    logError "Hadoop $UPGRADED_HADOOP_VERSION not started, unable to finalize upgrade"
    exit 1
  fi

  if rollingUpgradeInProgress; then
    logMessage "Finalizing upgrade"
    ${UPGRADED_HADOOP_DIR}/bin/hdfs dfsadmin -rollingUpgrade finalize
  else
    logError "Rolling upgrade not in progress, taking no further action"
    exit 1
  fi
}

rollbackUpgrade() {
  logMessage "Rolling back will result in losing any data since upgrade was started"
  read -p "Are you sure? (yes/no) " confirm

  if [[ ! "$confirm" == "yes" ]]; then
    logMessage "Cancelling"
    exit
  fi

  logMessage "Checking Hadoop $UPGRADED_HADOOP_VERSION is started"
  checkService hadoop up ${UPGRADED_PLATFORM_TOOLS_DIR}
  if [[ ! $? -eq 0 ]]; then
    logError "Hadoop $UPGRADED_HADOOP_VERSION not started, unable to finalize upgrade"
    exit 1
  fi

  if rollingUpgradeInProgress; then
    logMessage "Rolling back upgrade"
    stopUpgradedService all
    startPreviousNameNode -rollingUpgrade rollback
    startPreviousDataNodes -rollback
  else
    logError "Rolling upgrade not in progress, taking no further action"
    exit 1
  fi
}

if [[ ! $# -eq 2 ]]; then
  usage
  exit 1
fi

if [[ ! -d "$PREVIOUS_PLATFORM_TOOLS_DIR" || ! -d "$PREVIOUS_HADOOP_DIR" ]]; then
  logError "Directory $PREVIOUS_PLATFORM_TOOLS_DIR is not valid"
  exit 1
fi

case "$ACTION" in
  upgrade)
    prepareUpgrade
    startUpgrade
    ;;
  finalize)
    finalizeUpgrade
    ;;
  rollback)
    rollbackUpgrade
    ;;
  *)
    usage
    ;;
esac
