#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

. "${DIR}"/../../conf/env.sh

usage() {
  cat << USAGE >&2
Usage:
check-services.sh [-m module] [-p | -c] [-t timeout]
    -m MODULE   Module name (hdfsMaster|yarnMaster|hbaseMaster|phoenix|zookeeper|hadoop|hbase|all)
    -p          Print module status
    -c          Check module status
    -t TIMEOUT  Timeout in seconds. Use 0 no timeout (only avaliable with -c option)
USAGE
  exit 1
}

lineFormat="|%-15s|%-30s|%-60s|%-6s|\n"
lineSep='BEGIN{OFS="-";NF=117;print}'

printLine() {
  if [[ ${PRINT} -eq 1 ]]; then
    awk ${lineSep}
  fi
}

printHeader() {
  if [[ ${PRINT} -eq 1 ]]; then
    awk ${lineSep}
    printf "${lineFormat}" "Service" "Component" "Host" "Status"
    printLine
  fi
}

printFooter() {
  return
}

checkService() {
  serviceName=$1
  serviceComponent=$2
  serviceHost=$3
  servicePort=$4
  if [[ ${PRINT} -eq 1 ]]; then
    "${DIR}"/../common/try-connect.sh "${serviceHost}:${servicePort}" -q -t 5
  else
    "${DIR}"/../common/try-connect.sh "${serviceHost}:${servicePort}" -q -t ${TIMEOUT}
  fi
  RESULT=$?
  if [[ ${PRINT} -eq 1 ]]; then
    if [[ ${RESULT} -eq 0 ]]; then
      printf "${lineFormat}" "${serviceName}" "${serviceComponent}" "${serviceHost}" "Up"
    else
      printf "${lineFormat}" "${serviceName}" "${serviceComponent}" "${serviceHost}" "Down"
    fi
  else
    if [[ ${RESULT} -eq 0 ]]; then
      echo "${serviceName}:${serviceComponent} on ${serviceHost}:${servicePort} is started"
    else
      echo "${serviceName}:${serviceComponent} on ${serviceHost}:${servicePort} not started"
    fi
  fi
  printLine
}

checkHdfsMaster() {
  checkService "Hdfs" "NameNode" ${HDFS_NAME_NODE_HOST}  ${HDFS_NAME_NODE_UI_PORT}
  checkService "Hdfs" "SecondaryNameNode" ${HDFS_SNAME_NODE_HOST}  ${HDFS_SNAME_NODE_UI_PORT}
}

checkHdfsSlaves() {
  for dnHost in ${HDFS_DATA_NODE_HOSTS}; do
    checkService "Hdfs" "DataNode" ${dnHost} ${HDFS_DATA_NODE_UI_PORT}
  done
}

checkYarnMaster() {
  checkService "Yarn" "ResourceManager" ${YARN_RESOURCE_MANAGER_HOST} ${YARN_RESOURCE_MANAGER_PORT}
  checkService "Yarn" "JobHistoryServer" ${JOB_HISTORY_SERVER_HOST} ${JOB_HISTORY_SERVER_PORT}
}

checkYarnSlaves() {
  for host in ${YARN_NODE_MANAGER_HOSTS}; do
    checkService "Yarn" "NodeManager" ${host} ${YARN_NODE_MANAGER_PORT}
  done
}

checkHadoop() {
  checkHdfsMaster
  checkHdfsSlaves
  checkYarnMaster
  checkYarnSlaves
}

checkHBaseMaster() {
  hmasterPort=${HBASE_MASTER_PORT}

  checkService "HBase" "HMaster" ${HBASE_MASTER_HOST} $hmasterPort

  if [[ "${DISTRIBUTED}" == "true" ]]; then
    for host in ${HBASE_BACKUP_MASTER_HOSTS}; do
      checkService "HBase" "HMaster" ${host} $hmasterPort
    done
  fi
}

checkHBaseSlaves() {
  for host in ${HBASE_REGION_SERVER_HOSTS}; do
    checkService "HBase" "HRegionServer" ${host} ${HBASE_REGION_SERVER_PORT}
  done
}

checkHBase() {
   checkHBaseMaster
   checkHBaseSlaves
}

checkPhoenix() {
  for host in ${PHOENIX_QUERY_SERVER_HOSTS}; do
    checkService "Phoenix" "QueryServer" ${host} ${PHOENIX_QUERY_SERVER_PORT}
  done
}

checkSpark() {
  checkService "Spark" "Spark Job History Server" ${SPARK_HISTORY_SERVER_HOST} ${SPARK_HISTORY_SERVER_PORT}
}

checkZookeeper() {
  for host in ${ZOOKEEPER_HOSTS}; do
    checkService "Zookeeper" "Zookeeper" ${host} ${ZOOKEEPER_CLIENT_PORT}
  done
}

checkAll() {
  checkZookeeper
  checkHadoop
  checkHBase
  checkPhoenix
  checkSpark
}


while [[ $# -gt 0 ]]
do
    case "$1" in
      -m)
      MODULE="$2"
      if [[ ${MODULE} == "" ]]; then break; fi
      shift 2
      ;;
      -p)
      PRINT=1
      CHECK=0
      shift 1
      ;;
      -c)
      CHECK=1
      PRINT=0
      shift 1
      ;;
      -t)
      TIMEOUT="$2"
      shift 2
      ;;
      -h)
      usage;
      exit 1
      ;;
      *)
      echo  "Error: Unknown argument: $1"
      usage
      exit 1
      ;;
    esac
done

MODULE=${MODULE:-"all"}
TIMEOUT=${TIMEOUT:-15}
CHECK=${CHECK:-0}
PRINT=${PRINT:-1}

case "${MODULE}" in
  hdfsMaster)
    printHeader;
    checkHdfsMaster;
    printFooter;
    ;;
  yarnMaster)
    printHeader;
    checkYarnMaster;
    printFooter;
    ;;
  hbaseMaster)
    printHeader;
    checkHBaseMaster;
    printFooter;
    ;;
  zookeeper)
    printHeader;
    checkZookeeper;
    printFooter;
    ;;
  hadoop)
    printHeader;
    checkHadoop;
    printFooter;
    ;;
  hbase)
    printHeader;
    checkHBase;
    printFooter;
    ;;
  phoenix)
    printHeader;
    checkPhoenix;
    printFooter;
    ;;
  spark)
    printHeader;
    checkSpark;
    printFooter;
    ;;
  all)
    printHeader;
    checkAll;
    printFooter;
    ;;
  *)
    usage;
    ;;
esac