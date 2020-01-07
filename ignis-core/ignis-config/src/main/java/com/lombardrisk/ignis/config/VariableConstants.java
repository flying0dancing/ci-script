package com.lombardrisk.ignis.config;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class VariableConstants {

    public static final String HOSTS_COUNT = "hosts.count";
    public static final String DISTRIBUTED = "distributed";

    public static final String HOSTS = "hosts";
    public static final String HOSTS_PREFIX = "hosts.";

    //hosts settings
    public static final String ZOOKEEPER_HOSTS = "zookeeper.hosts";
    public static final String HDFS_NN_HOST = "hdfs.nn.host";
    public static final String HDFS_SN_HOST = "hdfs.sn.host";
    public static final String YARN_RM_HOST = "yarn.rm.host";
    public static final String HISTORY_SERVER_HOST = "history.server.host";
    public static final String HDFS_DN_HOSTS = "hdfs.dn.hosts";
    public static final String YARN_NM_HOSTS = "yarn.nm.hosts";
    public static final String HBASE_MASTER_HOST = "hbase.master.host";
    public static final String HBASE_BACKUP_MASTER_HOSTS = "hbase.backup.master.hosts";
    public static final String HBASE_REGIONSERVER_HOSTS = "hbase.regionserver.hosts";
    public static final String PHOENIX_SALT_BUCKETS = "phoenix.salt.bucket.count";
    public static final String PHOENIX_QS_HOSTS = "phoenix.qs.hosts";
    public static final String PHOENIX_QS_LOAD_BALANCER = "phoenix.qs.load.balancer";
    public static final String SPARK_HISTORY_SERVER_HOST = "spark.history.server.host";
    public static final String IGNIS_HOST = "ignis.host";
    public static final String JOB_USER_NAME = "job.user.name";
    public static final String JOB_USER_PASSWORD = "job.user.password";

    //database settings
    public static final String DATABASE_APP_USER = "database.app.user";
    public static final String DATABASE_APP_PASSWORD = "database.app.password";
    public static final String DATABASE_OWNER_USER = "database.owner.user";
    public static final String DATABASE_OWNER_PASSWORD = "database.owner.password";
    public static final String DATABASE_JDBC_URL = "database.jdbc.url";
    public static final String DATABASE_JDBC_DRIVER = "database.jdbc.driver";
    public static final String DATABASE_TYPE = "database.type";

    //s3 settings
    public static final String S3_REGION = "dataset.source.location.s3.region";
    public static final String SOURCE_BUCKET = "dataset.source.location.s3.bucket";
    public static final String SOURCE_PREFIX = "dataset.source.location.s3.prefix";
    public static final String ERROR_BUCKET = "dataset.error.location.s3.bucket";
    public static final String ERROR_PREFIX = "dataset.error.location.s3.prefix";

    //ports settings
    public static final String ZOOKEEPER_CLIENT_PORT = "zookeeper.client.port";
    public static final String HDFS_NN_HTTP_PORT = "hdfs.nn.http.port";
    public static final String HDFS_SN_HTTP_PORT = "hdfs.sn.http.port";
    public static final String HDFS_DN_HTTP_PORT = "hdfs.dn.http.port";
    public static final String YARN_RM_HTTP_PORT = "yarn.rm.http.port";
    public static final String YARN_NM_HTTP_PORT = "yarn.nm.http.port";
    public static final String HISTORY_SERVER_PORT = "history.server.http.port";
    public static final String HBASE_MASTER_HTTP_PORT = "hbase.master.http.port";
    public static final String HBASE_REGIONSERVER_HTTP_PORT = "hbase.regionserver.http.port";
    public static final String PHOENIX_QS_HTTP_PORT = "phoenix.qs.http.port";
    public static final String SPARK_HISTORY_SERVER_HTTP_PORT = "spark.history.server.http.port";
    public static final String IGNIS_HTTP_PORT = "server.http.port";
    public static final String IGNIS_HTTPS_PORT = "server.https.port";

    public static final String ZOOKEEPER_URL = "zookeeper.url";
    public static final String ZOOKEEPER_MYID = "zookeeper.myid";

    public static final String PHOENIX_TABLE_COMPRESSION = "phoenix.table.compression";

    public static final String KEYSTORE_FILE = "keystore.file";
    public static final String KEYSTORE_PASSWORD = "keystore.password";

    public static final String IGNIS_ADMIN_PASSWORD = "ignis.admin.password";
    public static final String IGNIS_ADMIN_PASSWORD_ENCRYPTED = "ignis.admin.password.encrypted";

    public static final String IGNIS_ADMIN_USERNAME = "admin";

    public static final String IGNIS_SERVER_CONTEXT_PATH = "server.servlet.contextPath";
}
