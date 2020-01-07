#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

. "${DIR}"/../conf/env.sh

commandToRun=$1
service=$2
extraArgs=${@:3}

usage() {
   echo "Usage: daemon.sh (start|stop|status) (hadoop|zookeeper|hbase|phoenix|spark|all)"
}

stopAll() {
  "${DIR}/ext/spark.sh" stop;
  "${DIR}/ext/phoenix.sh" stop;
  "${DIR}/ext/hbase.sh" stop;
  "${DIR}/ext/hadoop.sh" stop;
  "${DIR}/ext/zookeeper.sh" stop;
}

startAll() {
  "${DIR}/ext/zookeeper.sh" start || exitFromService "Apache ZooKeeper"
  "${DIR}/ext/hadoop.sh" start || exitFromService "Apache Hadoop"
  "${DIR}/ext/hbase.sh" start || exitFromService "Apache HBase"
  "${DIR}/ext/phoenix.sh" start || exitFromService "Apache Phoenix"
  "${DIR}/ext/spark.sh" start || exitFromService "Apache Spark"
}

exitFromService() {
  serviceName=$1
  echo "Failed to start ${serviceName}"
  exit 1
}

case "$service" in
  hadoop)
    ${DIR}/ext/hadoop.sh ${commandToRun} ${extraArgs};
    ;;
  zookeeper)
    ${DIR}/ext/zookeeper.sh ${commandToRun};
    ;;
  hbase)
    ${DIR}/ext/hbase.sh ${commandToRun};
    ;;
  phoenix)
    ${DIR}/ext/phoenix.sh ${commandToRun};
    ;;
  spark)
    ${DIR}/ext/spark.sh ${commandToRun};
    ;;
  all)
    if [[ "${commandToRun}" == "status" ]]; then
      ${DIR}/ext/check-services.sh  -m all
    else
      if [[ "${commandToRun}" == "stop" ]]; then
        stopAll
      else
        startAll
      fi
    fi
    ;;
  *)
    usage;
    ;;
esac