#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

. "${DIR}/conf/env.sh"

makeExecutableDirs() {
  for execDir in "$@"; do
    echo "Set executable permissions for all files under ${execDir}"
    chmod -R u+x "${execDir}"
  done
}

makeExecutableScripts() {
  searchDir=$1
  echo "Set executable permissions for all .sh files under ${searchDir}"

  find "${searchDir}" -iname "*.sh" | xargs chmod u+x
}

setBinPermissions() {
  makeExecutableDirs "${DIR}/bin"
  makeExecutableScripts "${DIR}/conf"
}

setZookeeperPermissions() {
  makeExecutableDirs "${ZOOKEEPER_HOME}/conf" "${ZOOKEEPER_HOME}/bin"
  makeExecutableScripts "${ZOOKEEPER_HOME}"
}

setupDistributedZookeeperConfig() {
  if [[ "${DISTRIBUTED}" == "true" ]]; then
    zk_hosts=(${ZOOKEEPER_HOSTS})
    zk_hosts_length=${#zk_hosts[@]}

    if [[ ${zk_hosts_length} -gt  1 ]]; then
      echo "Add ${zk_hosts_length} Apache ZooKeeper server ids"

      for index in ${!zk_hosts[@]}; do
        host=${zk_hosts[$index]}
        serverId=$(($index+1))
        echo "server.$serverId=$host:2888:3888" >> "${ZOOKEEPER_HOME}/conf/zoo.cfg"
      done
    fi
  fi
}

setHadoopPermissions() {
  makeExecutableScripts "${HADOOP_HOME}"
  makeExecutableDirs "${HADOOP_HOME}/sbin" "${HADOOP_HOME}/bin" "${HADOOP_HOME}/etc/hadoop"
}

setHBasePermissions() {
  makeExecutableScripts "${HBASE_HOME}"
  makeExecutableDirs "${HBASE_HOME}/conf" "${HBASE_HOME}/bin"
}

createHdfsDataDirs() {
  echo "Create Apache HDFS data dirs under ${DATA_PATH}"
  mkdir -p "${DATA_PATH}"/hdfs/datanode
  mkdir -p "${DATA_PATH}"/hdfs/namenode
  mkdir -p "${DATA_PATH}"/hdfs/scr
}

formatHdfsNameNode() {
  if [[ ! -d "${DATA_PATH}/hdfs/namenode/current" ]]; then
    echo "Format Apache HDFS namenode"
    "${HADOOP_PREFIX}/bin/hdfs" namenode -format
  fi
}

setupHBaseConfig() {
  backupPath="${HBASE_HOME}/conf/backup-masters"
  regionPath="${HBASE_HOME}/conf/regionservers"

  echo > ${backupPath}
  echo > ${regionPath}

  if [[ "${DISTRIBUTED}" == "true" ]]; then
    echo "Add Apache HBase backup hosts ${HBASE_BACKUP_MASTER_HOSTS}"
    echo "                           to ${backupPath}"
    for host in ${HBASE_BACKUP_MASTER_HOSTS}; do
      echo ${host} >> ${backupPath}
    done

    echo "Add Apache HBase region server hosts ${HBASE_REGION_SERVER_HOSTS}"
    echo "                                  to ${regionPath}"
    for host in ${HBASE_REGION_SERVER_HOSTS}; do
      echo ${host} >> ${regionPath}
    done
  else
    hostName=$(hostname)
    echo "Add host name ${hostName} as Apache HBase region server to ${regionPath}"
    echo ${hostName} > ${regionPath}
  fi
}

setPhoenixPermissions() {
  makeExecutableScripts "${PHOENIX_HOME}"
  makeExecutableDirs "${PHOENIX_HOME}/bin"
}

addPhoenixServerToHBaseLibs() {
  phoenixJar=$(find "${PHOENIX_HOME}" -iname "phoenix-*-server.jar")
  echo "Add ${phoenixJar} to Apache HBase libs"

  find "${HBASE_HOME}" -iname "phoenix-*-server.jar" | xargs rm -f
  cp "${phoenixJar}" "${HBASE_HOME}/lib"
}

setSparkPermissions() {
  makeExecutableScripts "${SPARK_HOME}"
  makeExecutableDirs "${SPARK_HOME}/conf" "${SPARK_HOME}/bin" "${SPARK_HOME}/sbin"
}

exitAndLog() {
  echo "Error while $1"
  exit 1
}

postInstall() {
  echo
  echo "$(date --iso-8601=seconds) [ INFO] Running post-install"

  setBinPermissions || exitAndLog "setting bin dir permissions"

  setZookeeperPermissions || exitAndLog "setting Apache Zookeeper permissions"
  setupDistributedZookeeperConfig || exitAndLog "configuring Apache Zookeeper"

  setHadoopPermissions || exitAndLog "setting Apache Hadoop permissions"
  formatHdfsNameNode || exitAndLog "formatting HDFS namenode"

  setHBasePermissions || exitAndLog "setting Apache HBase permissions"
  setupHBaseConfig || exitAndLog "configuring Apache HBase"
  createHdfsDataDirs || exitAndLog "creating data dirs"

  setPhoenixPermissions || exitAndLog "setting Apache Phoenix permissions"
  addPhoenixServerToHBaseLibs || exitAndLog "fixing Apache HBase/Phoenix server version"

  setSparkPermissions || exitAndLog "setting Apache Hadoop permissions"

  echo "$(date --iso-8601=seconds) [ INFO] Finished post-installation"
  echo
}

postInstall >> "${APP_HOME}/logs/install.log" 2>&1