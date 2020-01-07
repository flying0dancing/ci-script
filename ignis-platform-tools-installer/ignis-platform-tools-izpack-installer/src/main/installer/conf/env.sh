#!/usr/bin/env bash

export JAVA_HOME="%{JAVA_HOME}"
export APP_HOME="%{INSTALL_PATH}"
export SSH_OPTS="%{SSH_OPTS}"
export DATA_PATH="%{DATA_PATH}"
export DISTRIBUTED="%{distributed}"

export ZOOKEEPER_HOME="%{ZOOKEEPER_HOME}"
export ZOOKEEPER_CONF_DIR="%{ZOOKEEPER_HOME}/conf"
export ZOOKEEPER_HOSTS="%{zookeeper.hosts}"
export ZOOKEEPER_HOSTS="${ZOOKEEPER_HOSTS//,/ }"
export ZOOKEEPER_CLIENT_PORT="%{zookeeper.client.port}"

export HADOOP_HOME="%{HADOOP_HOME}"
export HADOOP_PREFIX="%{HADOOP_HOME}"
export HADOOP_CONF_DIR="%{HADOOP_HOME}/etc/hadoop"

export HDFS_NAME_NODE_HOST="%{hdfs.nn.host}"
export HDFS_NAME_NODE_UI_PORT="%{hdfs.nn.ui.port}"
export HDFS_SNAME_NODE_HOST="%{hdfs.sn.host}"
export HDFS_SNAME_NODE_UI_PORT="%{hdfs.sn.ui.port}"
export HDFS_DATA_NODE_HOSTS="%{hdfs.dn.hosts}"
export HDFS_DATA_NODE_HOSTS="${HDFS_DATA_NODE_HOSTS//,/ }"
export HDFS_DATA_NODE_UI_PORT="%{hdfs.dn.ui.port}"

export YARN_RESOURCE_MANAGER_HOST="%{yarn.rm.host}"
export YARN_RESOURCE_MANAGER_PORT="%{yarn.rm.port}"
export YARN_NODE_MANAGER_HOSTS="%{yarn.nm.hosts}"
export YARN_NODE_MANAGER_HOSTS="${YARN_NODE_MANAGER_HOSTS//,/ }"
export YARN_NODE_MANAGER_PORT="%{yarn.nm.port}"
export JOB_HISTORY_SERVER_HOST="%{history.server.host}"
export JOB_HISTORY_SERVER_PORT="%{history.server.port}"

export HBASE_HOME="%{HBASE_HOME}"
export HBASE_CONF_DIR="%{HBASE_HOME}/conf"
export HBASE_MASTER_HOST="%{hbase.master.host}"
export HBASE_MASTER_PORT="%{hbase.master.port}"
export HBASE_BACKUP_MASTER_HOSTS="%{hbase.backup.master.hosts}"
export HBASE_BACKUP_MASTER_HOSTS="${HBASE_BACKUP_MASTER_HOSTS//,/ }"
export HBASE_REGION_SERVER_HOSTS="%{hbase.regionserver.hosts}"
export HBASE_REGION_SERVER_HOSTS="${HBASE_REGION_SERVER_HOSTS//,/ }"
export HBASE_REGION_SERVER_PORT="%{hbase.regionserver.port}"

export PHOENIX_HOME="%{PHOENIX_HOME}"
export PHOENIX_CONF_DIR="%{PHOENIX_HOME}/conf"
export PHOENIX_QUERY_SERVER_HOSTS="%{phoenix.qs.hosts}"
export PHOENIX_QUERY_SERVER_HOSTS="${PHOENIX_QUERY_SERVER_HOSTS//,/ }"
export PHOENIX_QUERY_SERVER_PORT="%{phoenix.qs.port}"

export SPARK_HOME="%{SPARK_HOME}"
export SPARK_CONF_DIR="%{SPARK_HOME}/conf"
export SPARK_HISTORY_SERVER_HOST="%{spark.history.server.host}"
export SPARK_HISTORY_SERVER_PORT="%{spark.history.server.port}"